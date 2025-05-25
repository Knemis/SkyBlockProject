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
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");
        this.defaultInitialMaxHomes = plugin.getConfig().getInt("island.max-named-homes", 3); // YENİ
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().severe("Plugin veri klasörü oluşturulamadı: " + plugin.getDataFolder().getPath());
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
                plugin.getLogger().info(islandsFile.getName() + " oluşturuldu ve varsayılan yapıyla kaydedildi.");
                dataChangedSinceLastSave = false;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " oluşturulamadı veya kaydedilemedi!", e);
            }
        } else {
            try {
                islandsConfig.load(islandsFile);
                dataChangedSinceLastSave = false;
                plugin.getLogger().info(islandsFile.getName() + " başarıyla yüklendi.");
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " bulunamadı!", e);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " okunurken bir G/Ç hatası oluştu!", e);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " geçersiz bir YAML formatına sahip!", e);
            }
        }
    }


    public void loadSkyblockWorld() {
        String worldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        this.skyblockWorld = Bukkit.getWorld(worldName);

        if (this.skyblockWorld == null) {
            plugin.getLogger().info(worldName + " dünyası bulunamadı, oluşturuluyor...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.generator(new EmptyWorldGenerator());
            try {
                this.skyblockWorld = wc.createWorld();
                if (this.skyblockWorld != null) {
                    plugin.getLogger().info(worldName + " dünyası başarıyla oluşturuldu (IslandDataHandler).");
                } else {
                    plugin.getLogger().severe(worldName + " dünyası oluşturulamadı! Eklenti düzgün çalışmayabilir.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, worldName + " dünyası oluşturulurken kritik bir hata oluştu!", e);
            }
        } else {
            plugin.getLogger().info(this.skyblockWorld.getName() + " dünyası başarıyla yüklendi (IslandDataHandler).");
        }
    }

    public void loadIslandsFromConfig() {
        if (this.skyblockWorld == null) {
            plugin.getLogger().severe("Skyblock dünyası yüklenmeden ada verileri yüklenemez! Lütfen önce loadSkyblockWorld() metodunu çağırın.");
            return;
        }
        if (this.islandsConfig == null) {
            plugin.getLogger().warning("Islands config null, yeniden yüklenmeye çalışılıyor.");
            loadIslandsFile();
            if (this.islandsConfig == null) {
                plugin.getLogger().severe("Islands config yüklenemedi, ada verileri okunamıyor.");
                return;
            }
        }

        islandsData.clear();
        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands");
        if (islandsSection == null) {
            plugin.getLogger().info("Config dosyasında 'islands' bölümü bulunamadı veya boş. Hiçbir ada yüklenmedi.");
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
                plugin.getLogger().warning("Geçersiz sahip UUID formatı '" + uuidString + "' config dosyasında bulundu. Bu ada atlanıyor.");
                continue;
            }

            String path = "islands." + uuidString + ".";
            String worldName = islandsConfig.getString(path + "baseLocation.world");
            if (worldName == null || worldName.isEmpty()) {
                plugin.getLogger().warning("Ada (Sahip: " + uuidString + ") için dünya adı eksik veya boş. Bu ada atlanıyor.");
                continue;
            }

            World islandWorld = Bukkit.getWorld(worldName);
            if (islandWorld == null) {
                if (this.skyblockWorld != null && worldName.equals(this.skyblockWorld.getName())) { // skyblockWorld null kontrolü eklendi
                    islandWorld = this.skyblockWorld;
                } else {
                    plugin.getLogger().warning("Ada (Sahip: " + uuidString + ") için dünya '" + worldName + "' bulunamadı. Bu ada atlanıyor.");
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
            int maxHomesLimit = islandsConfig.getInt(path + "maxHomesLimit", defaultInitialMaxHomes); // YENİ: maxHomesLimit yükle

            Set<UUID> members = new HashSet<>();
            islandsConfig.getStringList(path + "members").forEach(memberStr -> {
                try { members.add(UUID.fromString(memberStr)); } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ada (Sahip: " + uuidString + ") için geçersiz üye UUID'si: " + memberStr);
                }
            });

            // Set<UUID> bannedPlayers = new HashSet<>(); // bannedPlayers yüklemesi kaldırıldı
            // List<String> bannedPlayerUUIDStrings = islandsConfig.getStringList(path + "bannedPlayers"); // kaldırıldı
            // bannedPlayerUUIDStrings.forEach(bannedStr -> { // kaldırıldı
            // try { bannedPlayers.add(UUID.fromString(bannedStr)); } catch (IllegalArgumentException ignored) { // kaldırıldı
            // plugin.getLogger().warning("Geçersiz yasaklı oyuncu UUID'si (" + bannedStr + ") bulundu (Ada Sahibi: " + uuidString + ")."); // kaldırıldı
            // } // kaldırıldı
            // }); // kaldırıldı

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
                        plugin.getLogger().warning("'" + homeNameKey + "' adlı ev (Sahip: " + uuidString + ") için dünya bulunamadı: " + homeWorldName);
                    }
                }
            }

            Island island = new Island(ownerUUID, islandName, baseLocation, creationTimestamp,
                    isPublic, boundariesEnforced, members, namedHomes,
                    currentBiome, welcomeMessage, maxHomesLimit); // maxHomesLimit constructor'a eklendi
            islandsData.put(ownerUUID, island);
            successfullyLoaded++;
        }
        plugin.getLogger().info(successfullyLoaded + " ada verisi config'den başarıyla yüklendi (IslandDataHandler).");
    }

    public void saveAllIslandsToDisk() {
        plugin.getLogger().info("Tüm ada verileri kaydediliyor...");
        islandsConfig.set("islands", null);
        if (islandsData.isEmpty()) {
            plugin.getLogger().info("Kaydedilecek aktif ada verisi bulunmuyor.");
        } else {
            for (Island island : islandsData.values()) {
                writeIslandToConfigInternal(island);
            }
            plugin.getLogger().info(islandsData.size() + " ada verisi config nesnesine yazıldı.");
        }
        dataChangedSinceLastSave = true;
        saveChangesToDisk();
    }

    private void writeIslandToConfigInternal(Island island) {
        if (island == null) {
            plugin.getLogger().warning("Null bir ada nesnesi config'e yazılamaz.");
            return;
        }
        if (islandsConfig == null) {
            plugin.getLogger().severe("Islands config null, ada config'e yazılamıyor: " + island.getOwnerUUID());
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
        islandsConfig.set(path + "maxHomesLimit", island.getMaxHomesLimit()); // YENİ: maxHomesLimit kaydet

        Location baseLoc = island.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName());
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX());
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY());
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ());
        } else {
            plugin.getLogger().warning("Ada (Sahip: " + uuidString + ") için temel konum veya dünya bilgisi eksik. Konum kaydedilemedi.");
        }

        islandsConfig.set(path + "members", island.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
        // DÜZELTME: bannedPlayers kaydetme satırı kaldırıldı.
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
            plugin.getLogger().warning("Null ada verisi eklenemez/güncellenemez.");
            return;
        }
        islandsData.put(island.getOwnerUUID(), island);
        writeIslandToConfigInternal(island);
        dataChangedSinceLastSave = true;
        plugin.getLogger().fine("Ada verisi güncellendi (bellek ve config): " + island.getOwnerUUID());
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
            plugin.getLogger().info(playerName + " adlı oyuncunun ada verisi bellekten ve config nesnesinden silindi.");
        } else {
            plugin.getLogger().warning(playerName + " için silinecek ada verisi bellekte bulunamadı, config'den silinmek üzere işaretlendi.");
        }
    }

    public void saveChangesToDisk() {
        if (islandsConfig == null || islandsFile == null) {
            plugin.getLogger().severe("Islands config veya islandsFile null, değişiklikler diske yazılamadı!");
            return;
        }
        if (dataChangedSinceLastSave) {
            try {
                islandsConfig.save(islandsFile);
                dataChangedSinceLastSave = false;
                plugin.getLogger().info("Ada verilerindeki değişiklikler " + islandsFile.getName() + " dosyasına kaydedildi.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " dosyasına kaydedilirken G/Ç hatası oluştu!", e);
            }
        } else {
            plugin.getLogger().fine("Ada verilerinde değişiklik olmadığı için diske yazma işlemi atlandı.");
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
            plugin.getLogger().warning("Skyblock dünyası (skyblockWorld) IslandDataHandler içinde null! Muhtemelen loadSkyblockWorld() düzgün çağrılmadı veya dünya yüklenemedi.");
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
                plugin.getLogger().warning("getIslandAt: Ada " + island.getOwnerUUID() + " için RegionManager alınamadı (Dünya: " + island.getWorld().getName() + "). Bölge kontrolü yapılamıyor.");
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