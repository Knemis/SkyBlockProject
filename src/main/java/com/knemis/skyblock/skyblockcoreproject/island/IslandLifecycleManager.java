package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager; // Varsayılan bayrakları uygulamak için


import com.sk89q.worldguard.protection.flags.StateFlag; // "Cannot resolve symbol 'StateFlag'" ve dolayısıyla '.getName()' için
import java.util.Arrays;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.regions.CuboidRegion; // Bu import gerekli olacak
import com.sk89q.worldedit.world.block.BlockTypes;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.managers.storage.StorageException;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class IslandLifecycleManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler; // Ada verilerini yönetmek için
    private final IslandFlagManager islandFlagManager; // Varsayılan bayrakları uygulamak için
    private final File schematicFile;
    private final String defaultIslandNamePrefix;
    private final List<StateFlag> criticalFlagsForOwnerBypass; // IslandManager'dan taşındı

    public IslandLifecycleManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandFlagManager islandFlagManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandFlagManager = islandFlagManager;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem"); // IslandManager'dan alındı
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");

        // Sahip bypass için kritik bayrakların listesi
        this.criticalFlagsForOwnerBypass = Arrays.asList(
                com.sk89q.worldguard.protection.flags.Flags.BUILD,
                com.sk89q.worldguard.protection.flags.Flags.INTERACT,
                com.sk89q.worldguard.protection.flags.Flags.CHEST_ACCESS,
                com.sk89q.worldguard.protection.flags.Flags.USE,
                com.sk89q.worldguard.protection.flags.Flags.ITEM_DROP,
                com.sk89q.worldguard.protection.flags.Flags.ITEM_PICKUP,
                com.sk89q.worldguard.protection.flags.Flags.TRAMPLE_BLOCKS,
                com.sk89q.worldguard.protection.flags.Flags.RIDE
        );
    }

    public String getRegionId(UUID playerUUID) {
        return "skyblock_island_" + playerUUID.toString();
    }

    // getPastedSchematicRegion ve getIslandTerritoryRegion metodları IslandManager'dan buraya taşındı
    // ve IslandDataHandler'dan skyblockWorld'ü alacak şekilde düzenlendi.
    private CuboidRegion getPastedSchematicRegion(Location islandBaseLocation) throws IOException {
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            throw new IOException("Pasted schematic region için ada temel konumu veya dünyası null geldi.");
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
            throw new IOException("Pasted schematic region için WorldEdit dünyası null geldi.");
        }
        return new CuboidRegion(weWorld, worldMinSchematic, worldMaxSchematic);
    }

    public CuboidRegion getIslandTerritoryRegion(Location islandBaseLocation) throws IOException {
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            throw new IOException("Island territory region için ada temel konumu veya dünyası null geldi.");
        }
        CuboidRegion schematicRegion = getPastedSchematicRegion(islandBaseLocation);

        int expansionRadiusHorizontal = plugin.getConfig().getInt("island.expansion-radius-horizontal", 50);
        int expansionRadiusVerticalTop = plugin.getConfig().getInt("island.expansion-radius-vertical-top", 50);
        int expansionRadiusVerticalBottom = plugin.getConfig().getInt("island.expansion-radius-vertical-bottom", 20);

        World world = islandBaseLocation.getWorld(); // Dünyayı buradan al
        int worldMinBuildHeight = 0;
        int worldMaxBuildHeight = world.getMaxHeight() -1;

        BlockVector3 schematicMin = schematicRegion.getMinimumPoint();
        BlockVector3 schematicMax = schematicRegion.getMaximumPoint();

        int territoryMinX = schematicMin.getBlockX() - expansionRadiusHorizontal;
        int territoryMaxX = schematicMax.getBlockX() + expansionRadiusHorizontal;
        int territoryMinZ = schematicMin.getBlockZ() - expansionRadiusHorizontal;
        int territoryMaxZ = schematicMax.getBlockZ() + expansionRadiusHorizontal;
        int territoryMinY = Math.max(worldMinBuildHeight, schematicMin.getBlockY() - expansionRadiusVerticalBottom);
        int territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY() + expansionRadiusVerticalTop);

        if (territoryMinY > territoryMaxY) {
            plugin.getLogger().warning("Hesaplanan ada bölgesi Y sınırları geçersizdi (minY > maxY). Şematik Y sınırlarına geri dönülüyor. Ada: " + islandBaseLocation);
            territoryMinY = Math.max(worldMinBuildHeight, schematicMin.getBlockY());
            territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY());
            if (territoryMinY > territoryMaxY) {
                territoryMaxY = territoryMinY;
            }
        }
        // schematicRegion.getWorld() WorldEdit dünyasını döndürür, bu doğru.
        return new CuboidRegion(schematicRegion.getWorld(),
                BlockVector3.at(territoryMinX, territoryMinY, territoryMinZ),
                BlockVector3.at(territoryMaxX, territoryMaxY, territoryMaxZ));
    }


    public void createIsland(Player player) {
        if (islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Zaten bir adanız var!");
            return;
        }
        World skyblockWorld = islandDataHandler.getSkyblockWorld();
        if (skyblockWorld == null) {
            player.sendMessage(ChatColor.RED + "Skyblock dünyası henüz yüklenmedi. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().severe("createIsland çağrıldığında skyblockWorld (IslandDataHandler'dan) null idi!");
            return;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı. Lütfen bir yetkiliye bildirin.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.");
        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        final Location islandBaseLocation = new Location(skyblockWorld, actualIslandX, 100, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    if (format == null) { /* ... hata ... */ return; }
                    Clipboard clipboard;
                    try (FileInputStream fis = new FileInputStream(schematicFile);
                         ClipboardReader reader = format.getReader(fis)) {
                        clipboard = reader.read();
                    }
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(skyblockWorld))) {
                        editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                                .ignoreAirBlocks(false)
                                .build();
                        Operations.complete(operation);
                    }

                    String newIslandName = defaultIslandNamePrefix + "-" + player.getName();
                    Island newIsland = new Island(player.getUniqueId(), islandBaseLocation, newIslandName);
                    islandDataHandler.addOrUpdateIslandData(newIsland); // Veri yöneticisi üzerinden kaydet
                    // islandDataHandler.saveChangesToDisk(); // Eğer her oluşturmada diske yazılsın isteniyorsa

                    RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld()); // SkyBlockProject üzerinden al
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
                            plugin.getLogger().severe("IslandFlagManager null! Varsayılan bayraklar uygulanamadı.");
                        }

                        regionManager.addRegion(protectedRegion);
                        try {
                            regionManager.saveChanges();
                            plugin.getLogger().info(player.getName() + " için WorldGuard bölgesi (" + regionId + ") oluşturuldu ve bayraklar ayarlandı.");
                            grantOwnerBypassPermissions(player, islandBaseLocation.getWorld().getName(), regionId);
                        } catch (StorageException e) {
                            plugin.getLogger().log(Level.SEVERE, "WorldGuard bölgeleri oluşturulurken (kayıt) hata: ", e);
                            player.sendMessage(ChatColor.RED + "Ada koruması kaydedilirken kritik bir hata oluştu.");
                        }
                    } else {
                        plugin.getLogger().severe("Ada için WorldGuard RegionManager alınamadı!");
                        player.sendMessage(ChatColor.RED + "Adanız oluşturuldu ancak koruma sağlanırken bir sorun oluştu.");
                    }

                    Location teleportLocation = islandBaseLocation.clone().add(0.5, 1.5, 0.5); // Config'den okunabilir
                    player.teleport(teleportLocation);
                    player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu ve ışınlandınız!");

                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Ada oluşturma sırasında genel bir hata oluştu (Oyuncu: " + player.getName() + ")", e);
                    player.sendMessage(ChatColor.RED + "Ada oluşturulurken çok beklenmedik bir hata oluştu.");
                }
            }
        }.runTask(plugin); // WorldEdit işlemleri senkron olmalı
    }

    public boolean deleteIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }
        String worldName = islandBaseLocation.getWorld().getName();
        String regionId = getRegionId(player.getUniqueId());

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi siliniyor...");
        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) { /* ... hata ... */ return false;}

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                editSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), BlockTypes.AIR.getDefaultState());
            }
            plugin.getLogger().info(player.getName() + " adlı oyuncunun ada bölgesi temizlendi.");

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(player.getName() + " için " + regionId + " ID'li WorldGuard bölgesi silindi.");
                    } catch (StorageException e) { /* ... hata ... */ }
                }
            }

            islandDataHandler.removeIslandData(player.getUniqueId());
            islandDataHandler.saveChangesToDisk(); // Değişiklikleri diske yaz

            revokeOwnerBypassPermissions(player, worldName, regionId);

            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla silindi.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, player.getName() + " için ada silinirken genel hata: ", e);
            player.sendMessage(ChatColor.RED + "Adanız silinirken çok beklenmedik bir hata oluştu.");
            return false;
        }
    }

    public boolean resetIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Başlangıç ada şematiği bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız sıfırlanıyor...");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) { /* hata */ return false; }

            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                clearSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), BlockTypes.AIR.getDefaultState());
            }

            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) { /* hata */ return false; }
            try (FileInputStream fis = new FileInputStream(schematicFile); ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }
            try (EditSession pasteSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false).build();
                Operations.complete(operation);
            }

            island.getNamedHomes().clear();
            island.setCurrentBiome(null);
            island.setWelcomeMessage(null);
            // Üyeler ve yasaklılar sıfırlanmıyor, isteğe bağlı eklenebilir.

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(getRegionId(player.getUniqueId()));
                if (region != null) {
                    region.getFlags().clear();
                    if (islandFlagManager != null) {
                        islandFlagManager.applyDefaultFlagsToRegion(region);
                    }
                    try {
                        regionManager.saveChanges();
                    } catch (StorageException e) { /* hata */ }
                }
            }
            islandDataHandler.addOrUpdateIslandData(island); // Güncellenmiş ada verisini kaydet
            islandDataHandler.saveChangesToDisk();

            player.teleport(islandBaseLocation.clone().add(0.5, 1.5, 0.5)); // TeleportManager'a taşınabilir
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla sıfırlandı!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, player.getName() + " için ada sıfırlanırken genel hata: ", e);
            player.sendMessage(ChatColor.RED + "Adanız sıfırlanırken bir hata oluştu.");
            return false;
        }
    }

    private void grantOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) {
            plugin.getLogger().warning("LuckPerms API bulunamadığı için " + owner.getName() + " adlı oyuncuya otomatik bypass izni verilemedi.");
            return;
        }

        UUID playerUUID = owner.getUniqueId();
        final List<Node> nodesToAdd = new ArrayList<>();

        // 1. Genel Bölge Bypass İzni (Bu iznin işe yaradığını teyit etmiştik, ama çok genel olabilir)
        // String generalBypassNode = "worldguard.region.bypass." + worldName + "." + regionId;
        // nodesToAdd.add(PermissionNode.builder(generalBypassNode).value(true).build());
        // plugin.getLogger().info(owner.getName() + " için LuckPerms genel bypass izni ekleniyor: " + generalBypassNode);

        // 2. Kritik Bayraklar İçin Özel Bayrak Bazlı Bypass İzinleri (BUNLARIN ÇALIŞMASI HEDEFLENİYOR)
        for (StateFlag criticalFlag : criticalFlagsForOwnerBypass) {
            String flagNameForPerm = criticalFlag.getName().toLowerCase().replace("_", "-");
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToAdd.add(PermissionNode.builder(flagSpecificBypassNode).value(true).build());
            plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni ekleniyor: " + flagSpecificBypassNode);
        }
        // Ek olarak, belki de sadece "worldguard.region.bypass.<world>" yetkisi yeterli olabilir.
        // Veya "worldguard.build.bypass.<world>.<region>" gibi daha da spesifikler.
        // Şimdilik yukarıdakileri deneyelim.

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToAdd.forEach(node -> {
                DataMutateResult addResult = user.data().add(node);
                if (!addResult.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için eklenirken beklenen sonuç alınamadı: " + addResult.name());
                }
            });
        }).thenRunAsync(() -> {
                    plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla eklendi ve kaydedilmesi istendi.");
                }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable) // Bukkit ana thread'inde çalıştır
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, owner.getName() + " için LuckPerms izinleri kaydedilirken hata oluştu.", ex);
            return null;
        });
    }

    private void revokeOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) {
            plugin.getLogger().warning("LuckPerms API bulunamadığı için " + owner.getName() + " adlı oyuncunun bypass izinleri geri ALINAMADI.");
            return;
        }

        UUID playerUUID = owner.getUniqueId();
        final List<Node> nodesToRemove = new ArrayList<>();

        // String generalBypassNode = "worldguard.region.bypass." + worldName + "." + regionId;
        // nodesToRemove.add(PermissionNode.builder(generalBypassNode).build());
        // plugin.getLogger().info(owner.getName() + " için LuckPerms genel bypass izni kaldırılıyor: " + generalBypassNode);

        for (StateFlag criticalFlag : criticalFlagsForOwnerBypass) {
            String flagNameForPerm = criticalFlag.getName().toLowerCase().replace("_", "-");
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToRemove.add(PermissionNode.builder(flagSpecificBypassNode).build());
            plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni kaldırılıyor: " + flagSpecificBypassNode);
        }

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToRemove.forEach(node -> {
                DataMutateResult result = user.data().remove(node);
                if (!result.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için kaldırılırken beklenen sonuç alınamadı: " + result.name());
                }
            });
        }).thenRunAsync(() -> {
                    plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla kaldırıldı ve kaydedilmesi istendi.");
                }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable)
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, owner.getName() + " için LuckPerms izinleri kaldırılırken hata oluştu.", ex);
            return null;
        });
    }

    // IslandManager'dan taşınan diğer metodlar buraya eklenecek veya ilgili yeni yöneticilere dağıtılacak.
    // Şimdilik bu kadar.
}
