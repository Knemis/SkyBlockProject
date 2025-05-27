package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandDataHandler {

    private final SkyBlockProject plugin;
    private final File islandsFile;
    private FileConfiguration islandsConfig;
    private final Map<UUID, Island> islandsData;
    private final String defaultIslandNamePrefix;
    private World skyblockWorld;
    private final int defaultInitialMaxHomes;
    private boolean dataChangedSinceLastSave = false;

    public IslandDataHandler(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        this.islandsData = new HashMap<>();
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Island"); // "Ada" changed to "Island" for default
        this.defaultInitialMaxHomes = plugin.getConfig().getInt("island.max-named-homes", 3); // NEW
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().severe("Plugin data folder could not be created: " + plugin.getDataFolder().getPath());
            }
        }
        this.islandsConfig = new YamlConfiguration();
        loadIslandsFile();
    }

    private void loadIslandsFile() {
        if (!islandsFile.exists()) {
            try {
                islandsConfig.createSection("islands");
                islandsConfig.save(islandsFile);
                plugin.getLogger().info(islandsFile.getName() + " created and saved with default structure.");
                dataChangedSinceLastSave = false;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " could not be created or saved!", e);
            }
        } else {
            try {
                islandsConfig.load(islandsFile);
                dataChangedSinceLastSave = false;
                plugin.getLogger().info(islandsFile.getName() + " successfully loaded.");
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " not found!", e);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " an I/O error occurred while reading!", e);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " has an invalid YAML format!", e);
            }
        }
    }


    public void loadSkyblockWorld() {
        String worldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        this.skyblockWorld = Bukkit.getWorld(worldName);

        if (this.skyblockWorld == null) {
            plugin.getLogger().info(worldName + " world not found, creating...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.generator(new EmptyWorldGenerator());
            try {
                this.skyblockWorld = wc.createWorld();
                if (this.skyblockWorld != null) {
                    plugin.getLogger().info(worldName + " world successfully created (IslandDataHandler).");
                } else {
                    plugin.getLogger().severe(worldName + " world could not be created! Plugin may not work correctly.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, worldName + " a critical error occurred while creating world!", e);
            }
        } else {
            plugin.getLogger().info(this.skyblockWorld.getName() + " world successfully loaded (IslandDataHandler).");
        }
    }

    public void loadIslandsFromConfig() {
        if (this.skyblockWorld == null) {
            plugin.getLogger().severe("Island data cannot be loaded before the skyblock world is loaded! Please call loadSkyblockWorld() first.");
            return;
        }
        if (this.islandsConfig == null) {
            plugin.getLogger().warning("Islands config is null, attempting to reload.");
            loadIslandsFile();
            if (this.islandsConfig == null) {
                plugin.getLogger().severe("Islands config could not be loaded, island data cannot be read.");
                return;
            }
        }

        islandsData.clear();
        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands");
        if (islandsSection == null) {
            plugin.getLogger().info("'islands' section not found in config file or is empty. No islands loaded.");
            islandsConfig.createSection("islands");
            dataChangedSinceLastSave = true;
            return;
        }

        int successfullyLoaded = 0;
        for (String uuidString : islandsSection.getKeys(false)) {
            UUID ownerUUID = null;
            try {
                ownerUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid owner UUID format '" + uuidString + "' found in config file. This island is being skipped.");
                continue;
            }

            String path = "islands." + uuidString + ".";
            String worldName = islandsConfig.getString(path + "baseLocation.world");
            if (worldName == null || worldName.isEmpty()) {
                plugin.getLogger().warning("World name for island (Owner: " + uuidString + ") is missing or empty. This island is being skipped.");
                continue;
            }

            World islandWorld = Bukkit.getWorld(worldName);
            if (islandWorld == null) {
                if (this.skyblockWorld != null && worldName.equals(this.skyblockWorld.getName())) { // Added skyblockWorld null check
                    islandWorld = this.skyblockWorld;
                } else {
                    plugin.getLogger().warning("World '" + worldName + "' for island (Owner: " + uuidString + ") not found. This island is being skipped.");
                    continue;
                }
            }

            double x = islandsConfig.getDouble(path + "baseLocation.x");
            double y = islandsConfig.getDouble(path + "baseLocation.y");
            double z = islandsConfig.getDouble(path + "baseLocation.z");
            Location baseLocation = new Location(islandWorld, x, y, z);

            String islandName = islandsConfig.getString(path + "islandName", defaultIslandNamePrefix + "-" + Bukkit.getOfflinePlayer(ownerUUID).getName());
            long creationTimestamp = islandsConfig.getLong(path + "creationDate", System.currentTimeMillis());
            boolean isPublic = islandsConfig.getBoolean(path + "isPublic", false);
            boolean boundariesEnforced = islandsConfig.getBoolean(path + "boundariesEnforced", true);
            String currentBiome = islandsConfig.getString(path + "currentBiome", null);
            String welcomeMessage = islandsConfig.getString(path + "welcomeMessage", "");
            int maxHomesLimit = islandsConfig.getInt(path + "maxHomesLimit", defaultInitialMaxHomes); // NEW: load maxHomesLimit
            double islandWorth = islandsConfig.getDouble(path + "islandWorth", 0.0); // NEW
            int islandLevel = islandsConfig.getInt(path + "islandLevel", 1);

            Set<UUID> members = new HashSet<>();
            islandsConfig.getStringList(path + "members").forEach(memberStr -> {
                try { members.add(UUID.fromString(memberStr)); } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid member UUID for island (Owner: " + uuidString + "): " + memberStr);
                }
            });

            // Set<UUID> bannedPlayers = new HashSet<>(); // loading bannedPlayers removed
            // List<String> bannedPlayerUUIDStrings = islandsConfig.getStringList(path + "bannedPlayers"); // removed
            // bannedPlayerUUIDStrings.forEach(bannedStr -> { // removed
            // try { bannedPlayers.add(UUID.fromString(bannedStr)); } catch (IllegalArgumentException ignored) { // removed
            // plugin.getLogger().warning("Invalid banned player UUID (" + bannedStr + ") found (Island Owner: " + uuidString + ")."); // removed
            // } // removed
            // }); // removed

            Map<String, Location> namedHomes = new HashMap<>();
            ConfigurationSection homesCfgSection = islandsConfig.getConfigurationSection(path + "homes");
            if (homesCfgSection != null) {
                for (String homeNameKey : homesCfgSection.getKeys(false)) {
                    String homePath = path + "homes." + homeNameKey + ".";
                    String homeWorldName = homesCfgSection.getString(homePath + "world");
                    World homeWorld = Bukkit.getWorld(homeWorldName != null ? homeWorldName : worldName);
                    if (homeWorld != null) {
                        namedHomes.put(homeNameKey.toLowerCase(), new Location(homeWorld,
                                homesCfgSection.getDouble(homePath + "x"),
                                homesCfgSection.getDouble(homePath + "y"),
                                homesCfgSection.getDouble(homePath + "z"),
                                (float) homesCfgSection.getDouble(homePath + "yaw"),
                                (float) homesCfgSection.getDouble(homePath + "pitch")));
                    } else {
                        plugin.getLogger().warning("World for home named '" + homeNameKey + "' (Owner: " + uuidString + ") not found: " + homeWorldName);
                    }
                }
            }

            Island island = new Island(ownerUUID, islandName, baseLocation, creationTimestamp,
                    isPublic, boundariesEnforced, members, namedHomes,
                    currentBiome, welcomeMessage, maxHomesLimit, islandWorth, islandLevel); // This call should match the constructor above
            islandsData.put(ownerUUID, island);
            successfullyLoaded++;
        }
        plugin.getLogger().info(successfullyLoaded + " island data successfully loaded from config (IslandDataHandler).");
    }

    public void saveAllIslandsToDisk() {
        plugin.getLogger().info("Saving all island data...");
        islandsConfig.set("islands", null);
        if (islandsData.isEmpty()) {
            plugin.getLogger().info("No active island data to save.");
        } else {
            for (Island island : islandsData.values()) {
                writeIslandToConfigInternal(island);
            }
            plugin.getLogger().info(islandsData.size() + " island data written to config object.");
        }
        dataChangedSinceLastSave = true;
        saveChangesToDisk();
    }

    private void writeIslandToConfigInternal(Island island) {
        if (island == null) {
            plugin.getLogger().warning("A null island object cannot be written to config.");
            return;
        }
        if (islandsConfig == null) {
            plugin.getLogger().severe("Islands config is null, island cannot be written to config: " + island.getOwnerUUID());
            return;
        }

        String uuidString = island.getOwnerUUID().toString();
        String path = "islands." + uuidString + ".";

        islandsConfig.set(path + "islandName", island.getIslandName());
        islandsConfig.set(path + "creationDate", island.getCreationTimestamp());
        islandsConfig.set(path + "isPublic", island.isPublic());
        islandsConfig.set(path + "boundariesEnforced", island.areBoundariesEnforced());
        islandsConfig.set(path + "currentBiome", island.getCurrentBiome());
        islandsConfig.set(path + "welcomeMessage", island.getWelcomeMessage());
        islandsConfig.set(path + "maxHomesLimit", island.getMaxHomesLimit()); // NEW: save maxHomesLimit
        islandsConfig.set(path + "islandWorth", island.getIslandWorth());     // NEW
        islandsConfig.set(path + "islandLevel", island.getIslandLevel());
        Location baseLoc = island.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName());
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX());
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY());
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ());
        } else {
            plugin.getLogger().warning("Base location or world information for island (Owner: " + uuidString + ") is missing. Location not saved.");
        }

        islandsConfig.set(path + "members", island.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
        // FIX: Saving bannedPlayers line removed.
        // islandsConfig.set(path + "bannedPlayers", island.getBannedPlayers().stream().map(UUID::toString).collect(Collectors.toList()));

        islandsConfig.set(path + "homes", null);
        if (island.getNamedHomes() != null && !island.getNamedHomes().isEmpty()) {
            for (Map.Entry<String, Location> homeEntry : island.getNamedHomes().entrySet()) {
                String homeName = homeEntry.getKey();
                Location homeLoc = homeEntry.getValue();
                String homePath = path + "homes." + homeName + ".";
                if (homeLoc != null && homeLoc.getWorld() != null) {
                    islandsConfig.set(homePath + "world", homeLoc.getWorld().getName());
                    islandsConfig.set(homePath + "x", homeLoc.getX());
                    islandsConfig.set(homePath + "y", homeLoc.getY());
                    islandsConfig.set(homePath + "z", homeLoc.getZ());
                    islandsConfig.set(homePath + "yaw", homeLoc.getYaw());
                    islandsConfig.set(homePath + "pitch", homeLoc.getPitch());
                }
            }
        }
    }

    public void addOrUpdateIslandData(Island island) {
        if (island == null) {
            plugin.getLogger().warning("Null island data cannot be added/updated.");
            return;
        }
        islandsData.put(island.getOwnerUUID(), island);
        writeIslandToConfigInternal(island);
        dataChangedSinceLastSave = true;
        plugin.getLogger().fine("Island data updated (memory and config): " + island.getOwnerUUID());
    }

    public void removeIslandData(UUID ownerUUID) {
        if (ownerUUID == null) return;
        Island removedIsland = islandsData.remove(ownerUUID);
        if (islandsConfig != null) {
            islandsConfig.set("islands." + ownerUUID.toString(), null);
        }
        dataChangedSinceLastSave = true;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : ownerUUID.toString();
        if (removedIsland != null) {
            plugin.getLogger().info("Island data for player " + playerName + " deleted from memory and config object.");
        } else {
            plugin.getLogger().warning("Island data to delete for " + playerName + " not found in memory, marked for deletion from config.");
        }
    }

    public void saveChangesToDisk() {
        if (islandsConfig == null || islandsFile == null) {
            plugin.getLogger().severe("Islands config or islandsFile is null, changes could not be written to disk!");
            return;
        }
        if (dataChangedSinceLastSave) {
            try {
                islandsConfig.save(islandsFile);
                dataChangedSinceLastSave = false;
                plugin.getLogger().info("Changes to island data saved to " + islandsFile.getName() + " file.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "I/O error occurred while saving to " + islandsFile.getName() + " file!", e);
            }
        } else {
            plugin.getLogger().fine("No changes in island data, disk write operation skipped.");
        }
    }

    public Island getIslandByOwner(UUID ownerUUID) {
        return islandsData.get(ownerUUID);
    }

    public boolean playerHasIsland(UUID playerUUID) {
        return islandsData.containsKey(playerUUID);
    }

    public Map<UUID, Island> getAllIslandsDataView() {
        return Collections.unmodifiableMap(islandsData);
    }

    public World getSkyblockWorld() {
        if (this.skyblockWorld == null) {
            plugin.getLogger().warning("Skyblock world (skyblockWorld) is null in IslandDataHandler! Probably loadSkyblockWorld() was not called properly or the world could not be loaded.");
        }
        return this.skyblockWorld;
    }

    private static String getRegionIdString(UUID ownerUUID) {
        return "skyblock_island_" + ownerUUID.toString();
    }

    public Island getIslandAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (this.skyblockWorld == null || !location.getWorld().equals(this.skyblockWorld)) {
            return null;
        }

        for (Island island : islandsData.values()) {
            if (island.getWorld() == null || !island.getWorld().equals(location.getWorld())) {
                continue;
            }

            RegionManager regionManager = plugin.getRegionManager(island.getWorld());
            if (regionManager == null) {
                plugin.getLogger().warning("getIslandAt: Could not get RegionManager for island " + island.getOwnerUUID() + " (World: " + island.getWorld().getName() + "). Region check cannot be performed.");
                continue;
            }

            String regionId = getRegionIdString(island.getOwnerUUID());
            ProtectedRegion region = regionManager.getRegion(regionId);

            if (region != null) {
                if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                    return island;
                }
            }
        }
        return null;
    }

    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return Bukkit.createChunkData(world);
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.5, 128, 0.5);
        }
    }
}