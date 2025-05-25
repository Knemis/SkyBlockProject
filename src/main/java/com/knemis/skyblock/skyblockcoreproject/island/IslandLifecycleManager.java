package com.knemis.skyblock.skyblockcoreproject.island;

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
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import com.sk89q.worldguard.protection.flags.StateFlag;
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

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

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
        this.economy = plugin.getEconomy(); // SkyBlockProject'ten ekonomi nesnesini al
        this.initialMaxHomes = plugin.getConfig().getInt("island.max-named-homes", 3); // Başlangıç ev limitini al
    }

    // ... (getRegionId, getPastedSchematicRegion, getIslandTerritoryRegion metodları aynı kalacak) ...
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

        int territoryMinX = schematicMin.getBlockX() - expansionRadiusHorizontal;
        int territoryMaxX = schematicMax.getBlockX() + expansionRadiusHorizontal;
        int territoryMinZ = schematicMin.getBlockZ() - expansionRadiusHorizontal;
        int territoryMaxZ = schematicMax.getBlockZ() + expansionRadiusHorizontal;

        boolean allowBuildBelow = plugin.getConfig().getBoolean("island.allow-build-below-schematic-base", false);
        int schematicBaseY = schematicMin.getBlockY();
        int buildLimitAboveSchematicTop = plugin.getConfig().getInt("island.build-limit-above-schematic-top", 150);

        int territoryMinY = allowBuildBelow ? Math.max(worldMinBuildHeight, schematicBaseY - expansionRadiusVerticalBottom)
                : schematicBaseY;
        int territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY() + buildLimitAboveSchematicTop);


        if (territoryMinY > territoryMaxY) {
            plugin.getLogger().warning("Hesaplanan ada bölgesi Y sınırları geçersizdi (minY > maxY): Ada: " + islandBaseLocation +
                    " MinY_calc: " + territoryMinY + " MaxY_calc: " + territoryMaxY + ". Şematik Y sınırlarına geri dönülüyor.");
            territoryMinY = Math.max(worldMinBuildHeight, schematicMin.getBlockY());
            territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY());
            if (territoryMinY > territoryMaxY) territoryMaxY = territoryMinY;
        }
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
            player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı ('" + schematicFile.getPath() + "'). Lütfen bir yetkiliye bildirin.");
            return;
        }

        // DÜZELTME: newIslandName metodun başında tanımlanıyor.
        String newIslandName = defaultIslandNamePrefix + "-" + player.getName();

        // DÜZELTME: Ekonomik işlem ada oluşturulmadan önce yapılıyor.
        double creationCost = plugin.getConfig().getDouble("island.creation-cost", 0.0);
        if (this.economy != null && creationCost > 0) {
            if (economy.getBalance(player) < creationCost) {
                player.sendMessage(ChatColor.RED + "Ada oluşturmak için yeterli paran yok! Gereken: " + economy.format(creationCost));
                return; // Para yoksa işlemi burada sonlandır
            }
            EconomyResponse r = economy.withdrawPlayer(player, creationCost);
            if (r.transactionSuccess()) {
                player.sendMessage(ChatColor.GREEN + economy.format(creationCost) + " ada oluşturma ücreti olarak hesabından çekildi.");
            } else {
                player.sendMessage(ChatColor.RED + "Ada oluşturma ücreti çekilirken bir hata oluştu: " + r.errorMessage);
                return; // Para çekilemezse işlemi burada sonlandır
            }
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.");
        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        final Location islandBaseLocation = new Location(skyblockWorld, actualIslandX, 100, 0);

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                player.sendMessage(ChatColor.RED + "Ada oluşturulurken bir hata oluştu. (Şematik Formatı tanınamadı)");
                plugin.getLogger().severe("Şematik formatı tanınamadı: " + schematicFile.getName());
                return;
            }

            Clipboard clipboard;
            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(skyblockWorld);
            if (adaptedWorld == null) {
                player.sendMessage(ChatColor.RED + "Ada oluşturulurken dünya adaptasyonunda bir hata oluştu.");
                plugin.getLogger().severe("createIsland: Skyblock dünyası WorldEdit'e adapte edilemedi.");
                return;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            // DÜZELTME: Island nesnesi burada, doğru constructor ile BİR KERE oluşturuluyor.
            // initialMaxHomes sınıf üyesi olarak constructor'da atanmıştı.
            Island newIsland = new Island(player.getUniqueId(), islandBaseLocation, newIslandName, this.initialMaxHomes);
            islandDataHandler.addOrUpdateIslandData(newIsland); // Ve BİR KERE kaydediliyor.

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);

                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId); // Önceki bölge varsa kaldır
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
                    player.sendMessage(ChatColor.RED + "Ada koruması kaydedilirken kritik bir hata oluştu.");
                }
            } else {
                plugin.getLogger().severe("Ada için WorldGuard RegionManager alınamadı! Koruma oluşturulamadı.");
                player.sendMessage(ChatColor.RED + "Adanız oluşturuldu ancak koruma sağlanırken bir sorun oluştu.");
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
                    if (!player.isOnline()) return; // Oyuncu offline ise ışınlama
                    if (!teleportLocation.getChunk().isLoaded()) {
                        teleportLocation.getChunk().load();
                    }
                    player.teleport(teleportLocation);
                    player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu ve ışınlandınız!");
                    plugin.getLogger().info(player.getName() + " için ada başarıyla oluşturuldu ve ışınlandı: " + teleportLocation);
                }
            }.runTaskLater(plugin, 1L);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ada oluşturma sırasında genel bir hata oluştu (Oyuncu: " + player.getName() + ")", e);
            player.sendMessage(ChatColor.RED + "Ada oluşturulurken çok beklenmedik bir hata oluştu. Lütfen yetkililere bildirin.");
            // Eğer bir hata oluşursa, daha önce çekilen parayı iade etmeyi düşünebilirsin.
            // Şimdilik bu eklenmedi.
            return; // Hata durumunda metoddan çık.
        }
        // DÜZELTME: Ada verisi zaten try bloğu içinde kaydedildi, disk kaydı da orada veya sonda bir kere yapılmalı.
        // Tekrar addOrUpdateIslandData ve gereksiz Island nesnesi oluşturma kaldırıldı.
        islandDataHandler.saveChangesToDisk(); // Tüm işlemler başarılıysa en sonda disk'e kaydet.
    }

    // ... (deleteIsland, resetIsland, grantOwnerBypassPermissions, revokeOwnerBypassPermissions metodları aynı kalacak) ...
    public boolean deleteIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) { player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!"); return false; }
        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı (silme işlemi için).");
            plugin.getLogger().warning("deleteIsland: " + player.getName() + " için ada temel konumu veya dünyası null.");
            return false;
        }
        String worldName = islandBaseLocation.getWorld().getName();
        String regionId = getRegionId(player.getUniqueId());

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi siliniyor...");
        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) {
                plugin.getLogger().severe("Ada silinirken WorldEdit dünyası null geldi (adaptasyon başarısız)! Ada: " + island.getOwnerUUID());
                player.sendMessage(ChatColor.RED + "Ada silinirken bir dünya hatası oluştu.");
                return false;
            }

            BlockState airState = BlockTypes.AIR != null ? BlockTypes.AIR.getDefaultState() : null;
            if (airState == null) {
                plugin.getLogger().severe("BlockTypes.AIR null veya getDefaultState çağrılamıyor! WorldEdit düzgün yüklenmemiş olabilir. Ada silme işlemi durduruldu.");
                player.sendMessage(ChatColor.RED + "Ada silinirken kritik bir WorldEdit hatası oluştu.");
                return false;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                editSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), airState);
            }
            plugin.getLogger().info(player.getName() + " adlı oyuncunun ("+island.getOwnerUUID()+") ada bölgesi temizlendi.");

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                    try { regionManager.saveChanges(); } catch (StorageException e) { plugin.getLogger().log(Level.SEVERE, "WG region silinirken kayıt hatası", e); }
                } else {
                    plugin.getLogger().warning(player.getName() + " için silinecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            } else {
                plugin.getLogger().severe("Ada silinirken WorldGuard RegionManager alınamadı! (Dünya: " + islandBaseLocation.getWorld().getName() + ")");
            }

            islandDataHandler.removeIslandData(player.getUniqueId());
            islandDataHandler.saveChangesToDisk();

            revokeOwnerBypassPermissions(player, worldName, regionId);
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla silindi.");
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, player.getName() + " ("+island.getOwnerUUID()+") için ada silinirken genel hata: ", e);
            player.sendMessage(ChatColor.RED + "Adanız silinirken çok beklenmedik bir hata oluştu.");
            return false;
        }
    }

    public boolean resetIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) { player.sendMessage(ChatColor.RED + "Sıfırlayacak adan yok."); return false;}
        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) { player.sendMessage(ChatColor.RED + "Adanın konumu/dünyası bulunamadı."); return false;}
        if (!schematicFile.exists()) { player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı."); return false;}

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi sıfırlanıyor... Lütfen bekleyin.");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) { player.sendMessage(ChatColor.RED + "Dünya hatası (WE)."); return false; }

            BlockState airState = BlockTypes.AIR != null ? BlockTypes.AIR.getDefaultState() : null;
            if (airState == null) { player.sendMessage(ChatColor.RED + "Blok hatası (WE)."); return false; }

            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                clearSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), airState);
            }
            plugin.getLogger().info(player.getName() + " ("+island.getOwnerUUID()+") için ada bölgesi temizlendi (reset).");

            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) { player.sendMessage(ChatColor.RED + "Şematik format hatası."); return false; }
            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }
            try (EditSession pasteSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }
            plugin.getLogger().info(player.getName() + " ("+island.getOwnerUUID()+") için ada şematiği yeniden yapıştırıldı (reset).");

            // Reset specific island properties
            island.getNamedHomes().clear(); // Clear all homes
            island.setCurrentBiome(null); // Reset biome to default (or null to let it be whatever the schematic sets)
            island.setWelcomeMessage(null); // Clear welcome message
            // maxHomesLimit'i başlangıç değerine döndür
            island.setMaxHomesLimit(plugin.getConfig().getInt("island.max-named-homes", 3));


            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    region.getFlags().clear(); // Clear all flags
                    if (islandFlagManager != null) {
                        islandFlagManager.applyDefaultFlagsToRegion(region); // Apply default flags
                    } else {
                        plugin.getLogger().severe("IslandFlagManager null! Ada sıfırlanırken varsayılan bayraklar uygulanamadı.");
                    }
                    try { regionManager.saveChanges(); } catch (StorageException e) { plugin.getLogger().log(Level.SEVERE, "WG region sıfırlanırken kayıt hatası", e); }
                } else {
                    plugin.getLogger().warning(player.getName() + " için sıfırlanacak/güncellenecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            }
            islandDataHandler.addOrUpdateIslandData(island); // Save changes to island object
            islandDataHandler.saveChangesToDisk();

            // Teleport player after a short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getIslandTeleportManager().teleportPlayerToIslandSpawn(player);
                        player.sendMessage(ChatColor.GREEN + "Adanız başarıyla sıfırlandı ve ışınlandınız!");
                    }
                }
            }.runTaskLater(plugin, 20L); // 1 saniye sonra ışınla (chunk yüklenmesi için)

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, player.getName() + " ("+island.getOwnerUUID()+") için ada sıfırlanırken genel hata: ", e);
            player.sendMessage(ChatColor.RED + "Adanız sıfırlanırken çok beklenmedik bir hata oluştu.");
            return false;
        }
    }
    private void grantOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) { return; }

        List<StateFlag> manageableFlags = plugin.getIslandFlagManager().getManagableFlags();
        if (manageableFlags == null || manageableFlags.isEmpty()) { return; }

        UUID playerUUID = owner.getUniqueId();
        final List<Node> nodesToAdd = new ArrayList<>();

        for (StateFlag flagToBypass : manageableFlags) {
            String flagNameForPerm = flagToBypass.getName().toLowerCase();
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToAdd.add(PermissionNode.builder(flagSpecificBypassNode).value(true).build());
            if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni ekleniyor: " + flagSpecificBypassNode);
            }
        }
        if (nodesToAdd.isEmpty()){ return; }

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToAdd.forEach(node -> {
                DataMutateResult addResult = user.data().add(node);
                if (!addResult.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için eklenirken beklenen sonuç alınamadı: " + addResult.name());
                }
            });
        }).thenRunAsync(() -> plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla eklendi ve kaydedilmesi istendi."),
                runnable -> Bukkit.getScheduler().runTask(plugin, runnable)
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, owner.getName() + " için LuckPerms izinleri kaydedilirken hata oluştu.", ex);
            return null;
        });
    }

    private void revokeOwnerBypassPermissions(Player owner, String worldName, String regionId) {
        LuckPerms lpApi = plugin.getLuckPermsApi();
        if (lpApi == null) { return; }

        List<StateFlag> manageableFlags = plugin.getIslandFlagManager().getManagableFlags();
        if (manageableFlags == null || manageableFlags.isEmpty()) { return; }

        UUID playerUUID = owner.getUniqueId();
        final List<Node> nodesToRemove = new ArrayList<>();
        for (StateFlag flagToBypass : manageableFlags) {
            String flagNameForPerm = flagToBypass.getName().toLowerCase();
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToRemove.add(PermissionNode.builder(flagSpecificBypassNode).build());
            if (plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni kaldırılıyor: " + flagSpecificBypassNode);
            }
        }
        if (nodesToRemove.isEmpty()){ return; }

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToRemove.forEach(node -> {
                DataMutateResult result = user.data().remove(node);
                if (!result.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için kaldırılırken beklenen sonuç alınamadı: " + result.name());
                }
            });
        }).thenRunAsync(() -> plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla kaldırıldı ve kaydedilmesi istendi."),
                runnable -> Bukkit.getScheduler().runTask(plugin, runnable)
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, owner.getName() + " için LuckPerms izinleri kaldırılırken hata oluştu.", ex);
            return null;
        });
    }
}