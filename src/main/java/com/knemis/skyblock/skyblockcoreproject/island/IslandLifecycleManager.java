package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.api.IslandCreateEvent;
import com.knemis.skyblock.skyblockcoreproject.api.IslandDeleteEvent;
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.WorldEditException; // Added import

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.flags.StateFlag; // Added import

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.model.data.DataMutateResult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;
import java.util.ArrayList; // Added import
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap; // Added import
import java.util.HashSet;
import java.util.Map; // Added import
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class IslandLifecycleManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandFlagManager islandFlagManager;
    private final File schematicFile;
    private final String defaultIslandNamePrefix;
    private final Economy economy;
    private final int initialMaxHomes;

    public IslandLifecycleManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandFlagManager islandFlagManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandFlagManager = islandFlagManager;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");
        this.economy = plugin.getEconomy();
        this.initialMaxHomes = plugin.getConfig().getInt("island.max-named-homes", 3);
    }

    public String getRegionId(UUID playerUUID) {
        return "skyblock_island_" + playerUUID.toString();
    }

    private CuboidRegion getPastedSchematicRegion(Location islandBaseLocation) throws IOException {
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            throw new IOException("Ada şematiği için temel konum veya dünya null geldi.");
        }
        if (!schematicFile.exists()) {
            throw new IOException("Ada şematiği bulunamadı: " + schematicFile.getPath());
        }
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            throw new IOException("Ada şematiği formatı tanınamadı: " + schematicFile.getName());
        }
        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            clipboard = reader.read();
        }
        BlockVector3 pasteOriginInSchematic = clipboard.getOrigin();
        BlockVector3 islandPastePoint = BlockVector3.at(islandBaseLocation.getBlockX(), islandBaseLocation.getBlockY(), islandBaseLocation.getBlockZ());
        BlockVector3 clipboardMinRel = clipboard.getRegion().getMinimumPoint().subtract(pasteOriginInSchematic);
        BlockVector3 clipboardMaxRel = clipboard.getRegion().getMaximumPoint().subtract(pasteOriginInSchematic);

        BlockVector3 worldMinSchematic = islandPastePoint.add(clipboardMinRel);
        BlockVector3 worldMaxSchematic = islandPastePoint.add(clipboardMaxRel);

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
        if (weWorld == null) {
            throw new IOException("Yapıştırılan şematik bölgesi için WorldEdit dünyası null geldi (adaptasyon başarısız).");
        }
        return new CuboidRegion(weWorld, worldMinSchematic, worldMaxSchematic);
    }

    public CuboidRegion getIslandTerritoryRegion(Location islandBaseLocation) throws IOException {
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            throw new IOException("Ada bölgesi için temel konum veya dünya null geldi.");
        }
        CuboidRegion schematicRegion = getPastedSchematicRegion(islandBaseLocation);

        int expansionRadiusHorizontal = plugin.getConfig().getInt("island.expansion-radius-horizontal", 50);
        int expansionRadiusVerticalBottom = plugin.getConfig().getInt("island.expansion-radius-vertical-bottom", 20);

        World world = islandBaseLocation.getWorld();
        int worldMinBuildHeight = world.getMinHeight();
        int worldMaxBuildHeight = world.getMaxHeight() -1;

        BlockVector3 schematicMin = schematicRegion.getMinimumPoint();
        BlockVector3 schematicMax = schematicRegion.getMaximumPoint();

        int territoryMinX = schematicMin.x() - expansionRadiusHorizontal;
        int territoryMaxX = schematicMax.x() + expansionRadiusHorizontal;
        int territoryMinZ = schematicMin.z() - expansionRadiusHorizontal;
        int territoryMaxZ = schematicMax.z() + expansionRadiusHorizontal;

        boolean allowBuildBelow = plugin.getConfig().getBoolean("island.allow-build-below-schematic-base", false);
        int schematicBaseY = schematicMin.y();
        int buildLimitAboveSchematicTop = plugin.getConfig().getInt("island.build-limit-above-schematic-top", 150);

        int territoryMinY = allowBuildBelow ? Math.max(worldMinBuildHeight, schematicBaseY - expansionRadiusVerticalBottom)
                : schematicBaseY;
        int territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.y() + buildLimitAboveSchematicTop);

        if (territoryMinY > territoryMaxY) {
            plugin.getLogger().warning("Hesaplanan ada bölgesi Y sınırları geçersizdi (minY > maxY): Ada: " + islandBaseLocation +
                    " MinY_calc: " + territoryMinY + " MaxY_calc: " + territoryMaxY + ". Şematik Y sınırlarına geri dönülüyor.");
            territoryMinY = Math.max(worldMinBuildHeight, schematicMin.y());
            territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.y());
            if (territoryMinY > territoryMaxY) territoryMaxY = territoryMinY;
        }
        return new CuboidRegion(schematicRegion.getWorld(),
                BlockVector3.at(territoryMinX, territoryMinY, territoryMinZ),
                BlockVector3.at(territoryMaxX, territoryMaxY, territoryMaxZ));
    }

    public void createIsland(Player player) {
        // Fire IslandCreateEvent
        // For Schematics.SchematicConfig, we use the placeholder from IslandCreateEvent for now.
        // This part will need proper integration when schematic handling is fully addressed.
        IslandCreateEvent.PlaceholderSchematicConfig tempSchematicConfig = new IslandCreateEvent.PlaceholderSchematicConfig("default"); // Or derive from actual schematic logic if simple
        IslandCreateEvent createEvent = new IslandCreateEvent(player, player.getName() + "_island", tempSchematicConfig); // Example name
        Bukkit.getServer().getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled()) {
            plugin.getLogger().info(String.format("Island creation for %s cancelled by IslandCreateEvent.", player.getName()));
            // Optionally send a message to the player if desired, e.g., player.sendMessage(Component.text("Island creation was cancelled.", NamedTextColor.RED));
            return; // Or appropriate action based on method signature if it changes
        }
        // Update island name and schematic if changed by the event
        String islandNameFromEvent = createEvent.getIslandName() != null ? createEvent.getIslandName() : defaultIslandNamePrefix + "-" + player.getUniqueId().toString(); // Changed to use player UUID for default
        // Note: The actual schematic application logic later in the method doesn't use this 'tempSchematicConfig' directly yet.
        // The 'newIslandName' variable later should use 'islandNameFromEvent'.

        plugin.getLogger().info(String.format("Attempting to create island for player %s (UUID: %s)", player.getName(), player.getUniqueId()));
        if (islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(Component.text("Zaten bir adanız var!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Island creation failed for %s: Player already has an island.", player.getName()));
            return;
        }
        World skyblockWorld = islandDataHandler.getSkyblockWorld();
        if (skyblockWorld == null) {
            player.sendMessage(Component.text("Skyblock dünyası henüz yüklenmedi. Lütfen bir yetkiliye bildirin.", NamedTextColor.RED));
            plugin.getLogger().severe("createIsland called but skyblockWorld (from IslandDataHandler) was null!");
            plugin.getLogger().warning(String.format("Island creation failed for %s: Skyblock world is not loaded.", player.getName()));
            return;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(Component.text("Ada şematiği bulunamadı ('" + schematicFile.getPath() + "'). Lütfen bir yetkiliye bildirin.", NamedTextColor.RED));
            plugin.getLogger().severe("Island schematic file not found at: " + schematicFile.getPath());
            plugin.getLogger().warning(String.format("Island creation failed for %s: Schematic file missing.", player.getName()));
            return;
        }

        String newIslandName = islandNameFromEvent;

        double creationCost = plugin.getConfig().getDouble("island.creation-cost", 0.0);
        if (this.economy != null && creationCost > 0) {
            if (economy.getBalance(player) < creationCost) {
                player.sendMessage(Component.text("Ada oluşturmak için yeterli paran yok! Gereken: " + economy.format(creationCost), NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Island creation failed for %s: Insufficient funds. Needed: %s, Has: %s",
                        player.getName(), economy.format(creationCost), economy.format(economy.getBalance(player))));
                return;
            }
            EconomyResponse r = economy.withdrawPlayer(player, creationCost);
            if (r.transactionSuccess()) {
                player.sendMessage(Component.text(economy.format(creationCost) + " ada oluşturma ücreti olarak hesabından çekildi.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Ada oluşturma ücreti çekilirken bir hata oluştu: " + r.errorMessage, NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Island creation failed for %s: Economy transaction error: %s", player.getName(), r.errorMessage));
                return;
            }
        }

        player.sendMessage(Component.text("Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.", NamedTextColor.YELLOW));
        plugin.getLogger().info(String.format("Proceeding with island creation for %s. Cost: %s. Name: %s", player.getName(), creationCost, newIslandName));
        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        final Location islandBaseLocation = new Location(skyblockWorld, actualIslandX, 100, 0);
        plugin.getLogger().info(String.format("Calculated island base location for %s: X=%d, Y=100, Z=0", player.getName(), actualIslandX));

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                player.sendMessage(Component.text("Ada oluşturulurken bir hata oluştu. (Şematik Formatı tanınamadı)", NamedTextColor.RED));
                plugin.getLogger().severe("Schematic format could not be determined for file: " + schematicFile.getName());
                plugin.getLogger().warning(String.format("Island creation failed for %s: Invalid schematic format.", player.getName()));
                return;
            }

            Clipboard clipboard;
            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(skyblockWorld);
            if (adaptedWorld == null) {
                player.sendMessage(Component.text("Ada oluşturulurken dünya adaptasyonunda bir hata oluştu.", NamedTextColor.RED));
                plugin.getLogger().severe("createIsland: Could not adapt Skyblock world to WorldEdit world.");
                plugin.getLogger().warning(String.format("Island creation failed for %s: WorldEdit world adaptation failed.", player.getName()));
                return;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                plugin.getLogger().info(String.format("Pasting schematic for %s at %s", player.getName(), islandBaseLocation.toString()));
                editSession.setReorderMode(com.sk89q.worldedit.EditSession.ReorderMode.MULTI_STAGE);
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            Island newIsland = new Island(player.getUniqueId(), islandBaseLocation, newIslandName, this.initialMaxHomes);
            islandDataHandler.addOrUpdateIslandData(newIsland);

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);

                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                }
                ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(
                        regionId, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()
                );
                protectedRegion.getOwners().addPlayer(player.getUniqueId());
                protectedRegion.setPriority(plugin.getConfig().getInt("island.region-priority", 10));

                if (islandFlagManager != null) {
                    islandFlagManager.applyDefaultFlagsToRegion(protectedRegion);
                } else {
                    plugin.getLogger().severe("IslandFlagManager null! Ada oluşturulurken varsayılan bayraklar uygulanamadı.");
                }

                regionManager.addRegion(protectedRegion);
                try {
                    regionManager.saveChanges();
                    plugin.getLogger().info(player.getName() + " için WorldGuard bölgesi (" + regionId + ") oluşturuldu ve bayraklar ayarlandı.");
                    grantOwnerBypassPermissions(player, islandBaseLocation.getWorld().getName(), regionId);
                } catch (StorageException e) {
                    plugin.getLogger().log(Level.SEVERE, "WorldGuard bölgeleri oluşturulurken (kayıt) hata: " + regionId, e);
                    player.sendMessage(Component.text("Ada koruması kaydedilirken kritik bir hata oluştu.", NamedTextColor.RED));
                }
            } else {
                plugin.getLogger().severe("Ada için WorldGuard RegionManager alınamadı! Koruma oluşturulamadı.");
                player.sendMessage(Component.text("Adanız oluşturuldu ancak koruma sağlanırken bir sorun oluştu.", NamedTextColor.RED));
            }

            double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
            double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
            double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
            Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
            teleportLocation.setYaw(0f);
            teleportLocation.setPitch(0f);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    if (!teleportLocation.getChunk().isLoaded()) {
                        teleportLocation.getChunk().load();
                    }
                    player.teleport(teleportLocation);
                    player.sendMessage(Component.text("Adanız başarıyla oluşturuldu ve ışınlandınız!", NamedTextColor.GREEN));
                    plugin.getLogger().info(String.format("Successfully created island for player %s (UUID: %s), Island ID: %s, at Location: %s. Player teleported.",
                            player.getName(), player.getUniqueId(), newIsland.getRegionId(), islandBaseLocation.toString()));
                }
            }.runTaskLater(plugin, 1L);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred during island creation for player " + player.getName() + " (UUID: " + player.getUniqueId() + ")", e);
            player.sendMessage(Component.text("Ada oluşturulurken çok beklenmedik bir hata oluştu. Lütfen yetkililere bildirin.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Island creation failed for %s due to an exception. Cost %f might need refunding.", player.getName(), creationCost));
            return;
        }
        islandDataHandler.saveChangesToDisk();
        plugin.getLogger().info("Island data saved to disk after island creation for " + player.getName());
    }

    private void clearEntitiesInRegion(World world, CuboidRegion islandTerritory) {
        if (world == null || islandTerritory == null) {
            plugin.getLogger().warning("Cannot clear entities: World or island territory is null.");
            return;
        }

        // Get Bukkit min/max corners for entity iteration
        org.bukkit.Location minCorner = new org.bukkit.Location(world, islandTerritory.getMinimumPoint().x(), islandTerritory.getMinimumPoint().y(), islandTerritory.getMinimumPoint().z());
        org.bukkit.Location maxCorner = new org.bukkit.Location(world, islandTerritory.getMaximumPoint().x(), islandTerritory.getMaximumPoint().y(), islandTerritory.getMaximumPoint().z());

        int removedEntitiesCount = 0;
        // Iterate over all loaded chunks that intersect the island territory.
        // This is more efficient than iterating all entities in the world.
        int minChunkX = minCorner.getBlockX() >> 4;
        int maxChunkX = maxCorner.getBlockX() >> 4;
        int minChunkZ = minCorner.getBlockZ() >> 4;
        int maxChunkZ = maxCorner.getBlockZ() >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (world.isChunkLoaded(cx, cz)) {
                    for (Entity entity : world.getChunkAt(cx, cz).getEntities()) {
                        // Check if entity is within the precise island territory
                        BlockVector3 entityPos = BlockVector3.at(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
                        if (islandTerritory.contains(entityPos)) {
                            // Do not remove players. Item frames and armor stands are common.
                            // You might want to be more specific or broader here.
                            if (!(entity instanceof Player)) {
                                entity.remove();
                                removedEntitiesCount++;
                            }
                        }
                    }
                }
            }
        }
        if (removedEntitiesCount > 0) {
            plugin.getLogger().info("Removed " + removedEntitiesCount + " entities from island region in world " + world.getName());
        }
    }

    public boolean deleteIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Silebileceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s attempted to delete island but has none.", player.getName()));
            return false;
        }

        // Fire IslandDeleteEvent
        IslandDeleteEvent deleteEvent = new IslandDeleteEvent(island, player);
        Bukkit.getServer().getPluginManager().callEvent(deleteEvent);
        if (deleteEvent.isCancelled()) {
            plugin.getLogger().info(String.format("Island deletion for %s (IslandID: %s) cancelled by IslandDeleteEvent.", player.getName(), island.getRegionId()));
            // Optionally send message to player
            return false;
        }

        Location islandBaseLocation = island.getBaseLocation();
        String islandId = island.getRegionId(); // islandId is defined here, but the log requested island.getRegionId(), which is fine.
        // plugin.getLogger().info(String.format("Attempting to delete island %s for player %s (UUID: %s)", islandId, player.getName(), player.getUniqueId())); // Original log, replaced by detailed trace

        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(Component.text("Adanın konumu veya dünyası bulunamadı (silme işlemi için).", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("[DELETE_TRACE] deleteIsland: Island base location or world is null for player %s (IslandID: %s). Cannot proceed.", player.getName(), island.getRegionId()));
            return false;
        }
        String worldName = islandBaseLocation.getWorld().getName();
        String regionId = getRegionId(player.getUniqueId()); // This is the WG regionId, which should match island.getRegionId()

        plugin.getLogger().info(String.format("[DELETE_TRACE] Initiating delete for player %s (IslandID: %s, IslandName: %s). Using baseLocation: %s (World: %s, X: %.2f, Y: %.2f, Z: %.2f).",
            player.getName(),
            island.getRegionId(),
            island.getIslandName(),
            (islandBaseLocation != null ? islandBaseLocation.toString() : "NULL_BASE_LOCATION_OBJECT"),
            (islandBaseLocation != null && islandBaseLocation.getWorld() != null ? islandBaseLocation.getWorld().getName() : "NULL_WORLD_IN_BASE_LOCATION"),
            (islandBaseLocation != null ? islandBaseLocation.getX() : 0.0),
            (islandBaseLocation != null ? islandBaseLocation.getY() : 0.0),
            (islandBaseLocation != null ? islandBaseLocation.getZ() : 0.0)
        ));

        player.sendMessage(Component.text("Adanız ve tüm bölgesi siliniyor...", NamedTextColor.YELLOW));
        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            if (islandTerritory != null) {
                plugin.getLogger().info(String.format("[DELETE_TRACE] Calculated islandTerritory for deletion: Min(%s), Max(%s) in world %s",
                    islandTerritory.getMinimumPoint().toString(),
                    islandTerritory.getMaximumPoint().toString(),
                    (islandTerritory.getWorld() != null ? islandTerritory.getWorld().getName() : "NULL_WORLD_IN_TERRITORY_REGION")
                ));
            } else {
                plugin.getLogger().severe("[DELETE_TRACE] islandTerritory is NULL after call to getIslandTerritoryRegion for island " + island.getRegionId() + ". Deletion of blocks will likely fail.");
                // Not returning false here, to allow other cleanup like WG region removal and data removal.
                // But WorldEdit operations will likely fail or do nothing.
            }

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) {
                plugin.getLogger().severe(String.format("[DELETE_TRACE] Could not adapt world for island deletion (IslandID: %s, Player: %s)", island.getRegionId(), player.getName()));
                player.sendMessage(Component.text("Ada silinirken bir dünya hatası oluştu.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("[DELETE_TRACE] Island deletion failed for %s (IslandID: %s): WorldEdit world adaptation failed.", player.getName(), island.getRegionId()));
                return false;
            }

            if (islandTerritory != null) { // Only attempt to clear if territory was calculated
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    CuboidRegion regionToClear = new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint());
                    Set<BlockVector3> positions = new HashSet<>();
                    // The null check for regionToClear is technically redundant if islandTerritory is not null, but harmless.
                    if (regionToClear != null) {
                        for (BlockVector3 vector : regionToClear) {
                            positions.add(vector);
                        }
                    }
                    if (!positions.isEmpty()) {
                        editSession.setBlocks(positions, BlockTypes.AIR);
                    } else {
                        plugin.getLogger().warning("[DELETE_TRACE] No positions to clear for island " + island.getRegionId() + " (region might be empty or territory calculation failed).");
                    }
                } catch (com.sk89q.worldedit.WorldEditException weException) {
                    plugin.getLogger().log(Level.SEVERE, String.format("[DELETE_TRACE] WorldEditException during island REGION CLEARANCE for %s (IslandID: %s): %s", player.getName(), island.getRegionId(), weException.getMessage()), weException);
                    player.sendMessage(Component.text("Adanız silinirken bölge temizliğinde bir WorldEdit hatası oluştu! Detaylar için konsolu kontrol edin.", NamedTextColor.RED));
                    // Not returning false here to allow other cleanup.
                }
            } else {
                 plugin.getLogger().severe("[DELETE_TRACE] Skipping WorldEdit block clearing for island " + island.getRegionId() + " because islandTerritory was null.");
            }
            plugin.getLogger().info(String.format("[DELETE_TRACE] Island region %s for player %s blocks cleared (or skipped if territory was null).", island.getRegionId(), player.getName()));

            // Add this call:
            clearEntitiesInRegion(islandBaseLocation.getWorld(), islandTerritory);
            plugin.getLogger().info(String.format("Entities cleared for island %s.", islandId));

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                    try { regionManager.saveChanges(); } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "Error saving WorldGuard region changes after removing " + regionId, e);
                    }
                } else {
                    plugin.getLogger().warning(String.format("WorldGuard region %s for player %s not found for deletion.", regionId, player.getName()));
                }
            } else {
                plugin.getLogger().severe(String.format("Could not get WorldGuard RegionManager for world %s during island deletion for %s (Island ID: %s).",
                        islandBaseLocation.getWorld().getName(), player.getName(), islandId));
            }

            islandDataHandler.removeIslandData(player.getUniqueId());
            islandDataHandler.saveChangesToDisk();

            revokeOwnerBypassPermissions(player, worldName, regionId);
            player.sendMessage(Component.text("Adanız başarıyla silindi.", NamedTextColor.GREEN));
            plugin.getLogger().info(String.format("Successfully deleted island %s for player %s.", islandId, player.getName()));
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, String.format("An unexpected error occurred during island deletion for player %s (Island ID: %s)", player.getName(), islandId), e);
            player.sendMessage(Component.text("Adanız silinirken çok beklenmedik bir hata oluştu.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Island deletion failed for %s (Island ID: %s) due to an exception.", player.getName(), islandId));
            return false;
        }
    }

    public boolean resetIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Sıfırlayacak adan yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s tried to reset island but has none.", player.getName()));
            return false;
        }
        Location islandBaseLocation = island.getBaseLocation();
        String islandId = island.getRegionId();

        plugin.getLogger().info(String.format("[RESET_TRACE] Initiating reset for player %s (IslandID: %s, IslandName: %s). Using baseLocation: %s (World: %s, X: %.2f, Y: %.2f, Z: %.2f).",
            player.getName(),
            island.getRegionId(),
            island.getIslandName(),
            (islandBaseLocation != null ? islandBaseLocation.toString() : "NULL_BASE_LOCATION_OBJECT"),
            (islandBaseLocation != null && islandBaseLocation.getWorld() != null ? islandBaseLocation.getWorld().getName() : "NULL_WORLD_IN_BASE_LOCATION"),
            (islandBaseLocation != null ? islandBaseLocation.getX() : 0.0),
            (islandBaseLocation != null ? islandBaseLocation.getY() : 0.0),
            (islandBaseLocation != null ? islandBaseLocation.getZ() : 0.0)
        ));

        // Store homes before any modification
        Map<String, Location> homesToPreserve = new HashMap<>(island.getNamedHomes());
        plugin.getLogger().info(String.format("[RESET_TRACE] Preserving %d homes for island %s.", homesToPreserve.size(), islandId));

        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(Component.text("Adanın konumu/dünyası bulunamadı.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Island reset failed for %s (Island ID: %s): Island base location or world is null.", player.getName(), islandId));
            return false;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(Component.text("Ada şematiği bulunamadı.", NamedTextColor.RED));
            plugin.getLogger().severe("Island schematic file not found at: " + schematicFile.getPath() + " during reset for " + islandId);
            plugin.getLogger().warning(String.format("Island reset failed for %s (Island ID: %s): Schematic file missing.", player.getName(), islandId));
            return false;
        }

        player.sendMessage(Component.text("Adanız ve tüm bölgesi sıfırlanıyor... Lütfen bekleyin.", NamedTextColor.YELLOW));
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) {
                player.sendMessage(Component.text("Dünya hatası (WE).", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Island reset failed for %s (Island ID: %s): WorldEdit world adaptation failed.", player.getName(), islandId));
                return false;
            }

            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                CuboidRegion regionToClear = new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint());
                Set<BlockVector3> positions = new HashSet<>();
                if (regionToClear != null) {
                    for (BlockVector3 vector : regionToClear) {
                        positions.add(vector);
                    }
                }
                if (!positions.isEmpty()) {
                    clearSession.setBlocks(positions, BlockTypes.AIR);
                } else {
                    plugin.getLogger().warning("[RESET_TRACE] No positions to clear for island " + islandId + " during reset (region might be empty or calculation failed).");
                }
            } catch (com.sk89q.worldedit.WorldEditException weException) {
                plugin.getLogger().log(Level.SEVERE, String.format("[RESET_TRACE] WorldEditException during island REGION CLEARANCE for %s (IslandID: %s): %s", player.getName(), islandId, weException.getMessage()), weException);
                player.sendMessage(Component.text("Adanız sıfırlanırken bölge temizliğinde bir WorldEdit hatası oluştu! Detaylar için konsolu kontrol edin.", NamedTextColor.RED));
                // Not returning false here to allow rest of the reset (like permission/data reset) to proceed if desired.
                // Depending on severity, could return false.
            }
            plugin.getLogger().info(String.format("Island region %s for player %s cleared (reset).", islandId, player.getName()));

            // Add this call:
            clearEntitiesInRegion(islandBaseLocation.getWorld(), islandTerritory);
            plugin.getLogger().info(String.format("Entities cleared for island %s during reset.", islandId));

            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                player.sendMessage(Component.text("Şematik format hatası.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Island reset failed for %s (Island ID: %s): Schematic format error.", player.getName(), islandId));
                return false;
            }
            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }
            try (EditSession pasteSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                plugin.getLogger().info(String.format("[RESET_TRACE] Pasting schematic for island %s (Player: %s) at %s for reset.", islandId, player.getName(), islandBaseLocation.toString()));
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            } catch (com.sk89q.worldedit.WorldEditException weException) {
                plugin.getLogger().log(Level.SEVERE, String.format("[RESET_TRACE] WorldEditException during island SCHEMATIC PASTE for %s (IslandID: %s): %s", player.getName(), islandId, weException.getMessage()), weException);
                player.sendMessage(Component.text("Adanız sıfırlanırken şematik yapıştırılırken bir WorldEdit hatası oluştu! Detaylar için konsolu kontrol edin.", NamedTextColor.RED));
                // Not returning false here.
            }
            plugin.getLogger().info(player.getName() + " ("+island.getOwnerUUID()+") için ada şematiği yeniden yapıştırıldı (reset).");

            // island.getNamedHomes().clear(); // Do not clear homes, they will be restored or overwritten if new ones with same name
            island.setCurrentBiome(null);
            island.setWelcomeMessage(null);
            island.setMaxHomesLimit(plugin.getConfig().getInt("island.max-named-homes", 3));

            // Restore homes
            if (!homesToPreserve.isEmpty()) {
                plugin.getLogger().info("Restoring " + homesToPreserve.size() + " named homes for island " + islandId);
                for (Map.Entry<String, Location> homeEntry : homesToPreserve.entrySet()) {
                    island.setNamedHome(homeEntry.getKey(), homeEntry.getValue());
                    plugin.getLogger().fine("Restored home: " + homeEntry.getKey() + " for island " + islandId);
                }
            }

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    region.getFlags().clear();
                    if (islandFlagManager != null) {
                        islandFlagManager.applyDefaultFlagsToRegion(region);
                    } else {
                        plugin.getLogger().severe("IslandFlagManager null! Ada sıfırlanırken varsayılan bayraklar uygulanamadı.");
                    }
                    try { regionManager.saveChanges(); } catch (StorageException e) { plugin.getLogger().log(Level.SEVERE, "WG region sıfırlanırken kayıt hatası", e); }
                } else {
                    plugin.getLogger().warning(player.getName() + " için sıfırlanacak/güncellenecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            }
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getIslandTeleportManager().teleportPlayerToIslandSpawn(player);
                        player.sendMessage(Component.text("Adanız başarıyla sıfırlandı ve ışınlandınız!", NamedTextColor.GREEN));
                    }
                }
            }.runTaskLater(plugin, 20L);

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, player.getName() + " ("+island.getOwnerUUID()+") için ada sıfırlanırken genel hata: ", e);
            player.sendMessage(Component.text("Adanız sıfırlanırken çok beklenmedik bir hata oluştu.", NamedTextColor.RED));
            return false;
        }
    }
    private void grantOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) {
            plugin.getLogger().warning("LuckPerms API not available. Cannot grant bypass permissions for " + owner.getName());
            return;
        }

        List<StateFlag> manageableFlags = plugin.getIslandFlagManager().getManagableFlags();
        if (manageableFlags == null || manageableFlags.isEmpty()) {
            plugin.getLogger().info("No manageable flags found. No bypass permissions to grant for " + owner.getName());
            return;
        }

        UUID playerUUID = owner.getUniqueId();
        // Load the user object
        lpApi.getUserManager().loadUser(playerUUID).thenAcceptAsync(user -> {
            if (user == null) {
                plugin.getLogger().warning("Could not load LuckPerms user for " + owner.getName() + " (UUID: " + playerUUID + ")");
                return;
            }

            final List<Node> nodesToAdd = new ArrayList<>();
            for (StateFlag flagToBypass : manageableFlags) {
                String flagNameForPerm = flagToBypass.getName().toLowerCase();
                String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
                nodesToAdd.add(PermissionNode.builder(flagSpecificBypassNode).value(true).build());
                if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().info("Preparing to add LuckPerms flag-specific bypass permission for " + owner.getName() + ": " + flagSpecificBypassNode);
                }
            }

            if (nodesToAdd.isEmpty()) {
                plugin.getLogger().info("No bypass permission nodes to add for " + owner.getName() + " on region " + regionId);
                return;
            }

            nodesToAdd.forEach(node -> {
                DataMutateResult addResult = user.data().add(node);
                if (!addResult.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms permission (" + node.getKey() + ") could not be added for player " + owner.getName() + " as expected: " + addResult.name());
                }
            });

            // Save the changes
            lpApi.getUserManager().saveUser(user).thenRunAsync(() -> {
                if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", true)) { // Default to true for this important log
                    plugin.getLogger().info("Successfully added and saved LuckPerms bypass permissions for " + owner.getName() + " on region " + regionId);
                }
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));

        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error loading LuckPerms user or adding bypass permissions for " + owner.getName() + " on region " + regionId, ex);
            return null;
        });
    }

    private void revokeOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) {
            plugin.getLogger().warning("LuckPerms API not available. Cannot revoke bypass permissions for " + owner.getName());
            return;
        }

        List<StateFlag> manageableFlags = plugin.getIslandFlagManager().getManagableFlags();
        if (manageableFlags == null || manageableFlags.isEmpty()) {
            plugin.getLogger().info("No manageable flags found. No bypass permissions to revoke for " + owner.getName());
            return;
        }

        UUID playerUUID = owner.getUniqueId();
        // Load the user object
        lpApi.getUserManager().loadUser(playerUUID).thenAcceptAsync(user -> {
            if (user == null) {
                plugin.getLogger().warning("Could not load LuckPerms user for " + owner.getName() + " (UUID: " + playerUUID + ") for permission revocation.");
                return;
            }

            final List<Node> nodesToRemove = new ArrayList<>();
            for (StateFlag flagToBypass : manageableFlags) {
                String flagNameForPerm = flagToBypass.getName().toLowerCase();
                String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
                // For removal, just the key is enough, but building it ensures consistency if other parts of the node mattered.
                nodesToRemove.add(PermissionNode.builder(flagSpecificBypassNode).build());
                if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().info("Preparing to remove LuckPerms flag-specific bypass permission for " + owner.getName() + ": " + flagSpecificBypassNode);
                }
            }

            if (nodesToRemove.isEmpty()) {
                plugin.getLogger().info("No bypass permission nodes to remove for " + owner.getName() + " on region " + regionId);
                return;
            }

            nodesToRemove.forEach(node -> {
                DataMutateResult removeResult = user.data().remove(node);
                if (!removeResult.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms permission (" + node.getKey() + ") could not be removed for player " + owner.getName() + " as expected: " + removeResult.name());
                }
            });

            // Save the changes
            lpApi.getUserManager().saveUser(user).thenRunAsync(() -> {
                 if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", true)) { // Default to true for this important log
                    plugin.getLogger().info("Successfully removed and saved LuckPerms bypass permissions for " + owner.getName() + " on region " + regionId);
                }
            }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));

        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error loading LuckPerms user or removing bypass permissions for " + owner.getName() + " on region " + regionId, ex);
            return null;
        });
    }
}