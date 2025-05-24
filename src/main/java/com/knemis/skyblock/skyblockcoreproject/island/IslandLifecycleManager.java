package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
// import com.sk89q.worldedit.WorldEditException; // Doğrudan fırlatılmıyor, Exception ile yakalanıyor
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
import com.sk89q.worldedit.world.block.BlockState; // BlockTypes.AIR kontrolü için
import com.sk89q.worldedit.world.block.BlockTypes;

import com.sk89q.worldguard.protection.flags.Flags; // criticalFlagsForOwnerBypass için (orijinal kodda vardı, tam yoluyla belirtilmiş)
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
// import org.bukkit.OfflinePlayer; // Kullanılmıyor, kaldırıldı
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class IslandLifecycleManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandFlagManager islandFlagManager;
    private final File schematicFile;
    private final String defaultIslandNamePrefix;
    private final List<StateFlag> criticalFlagsForOwnerBypass;

    public IslandLifecycleManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandFlagManager islandFlagManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandFlagManager = islandFlagManager;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");
        this.criticalFlagsForOwnerBypass = Arrays.asList(
                Flags.BUILD, Flags.INTERACT, Flags.CHEST_ACCESS, Flags.USE,
                Flags.ITEM_DROP, Flags.ITEM_PICKUP, Flags.TRAMPLE_BLOCKS, Flags.RIDE
        );
    }

    public String getRegionId(UUID playerUUID) { // public yapıldı, IslandMemberManager tarafından kullanılabilir
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
        // "weWorld == null" kontrolü, BukkitAdapter.adapt null dönebileceği için güvenlik amaçlı kalmalı.
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
        int expansionRadiusVerticalTop = plugin.getConfig().getInt("island.expansion-radius-vertical-top", 50); // Config'den okunabilir
        int expansionRadiusVerticalBottom = plugin.getConfig().getInt("island.expansion-radius-vertical-bottom", 20); // Config'den okunabilir

        World world = islandBaseLocation.getWorld();
        int worldMinBuildHeight = world.getMinHeight(); // Bukkit 1.16+ için dünya min yüksekliği
        int worldMaxBuildHeight = world.getMaxHeight() -1;

        BlockVector3 schematicMin = schematicRegion.getMinimumPoint();
        BlockVector3 schematicMax = schematicRegion.getMaximumPoint();

        int territoryMinX = schematicMin.getBlockX() - expansionRadiusHorizontal;
        int territoryMaxX = schematicMax.getBlockX() + expansionRadiusHorizontal;
        int territoryMinZ = schematicMin.getBlockZ() - expansionRadiusHorizontal;
        int territoryMaxZ = schematicMax.getBlockZ() + expansionRadiusHorizontal;
        // Dikey genişlemeyi, şematik tabanının altına izin verilip verilmediğine göre ayarla
        boolean allowBuildBelow = plugin.getConfig().getBoolean("island.allow-build-below-schematic-base", false);
        int schematicBaseY = schematicMin.getBlockY(); // Şematiğin en alt Y seviyesi
        int buildLimitAboveSchematicTop = plugin.getConfig().getInt("island.build-limit-above-schematic-top", 150);


        int territoryMinY = allowBuildBelow ? Math.max(worldMinBuildHeight, schematicBaseY - expansionRadiusVerticalBottom)
                : schematicBaseY;
        int territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY() + buildLimitAboveSchematicTop);


        if (territoryMinY > territoryMaxY) {
            plugin.getLogger().warning("Hesaplanan ada bölgesi Y sınırları geçersizdi (minY > maxY): Ada: " + islandBaseLocation +
                    " MinY_calc: " + territoryMinY + " MaxY_calc: " + territoryMaxY + ". Şematik Y sınırlarına geri dönülüyor.");
            territoryMinY = Math.max(worldMinBuildHeight, schematicMin.getBlockY());
            territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY());
            if (territoryMinY > territoryMaxY) territoryMaxY = territoryMinY; // En kötü durum
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
            player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı ('" + schematicFile.getPath() + "'). Lütfen bir yetkiliye bildirin.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.");
        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        final Location islandBaseLocation = new Location(skyblockWorld, actualIslandX, 100, 0); // Y=100 sabit, şematik merkezine göre ayarlanmalı

        // WorldEdit işlemleri ana thread'de yapılmalı, ancak uzun sürerse BukkitRunnable task içinde yapılabilir.
        // Şimdilik direkt ana thread'de yapıyoruz, ancak çok büyük şematikler için dikkatli olunmalı.
        // BukkitRunnable kullanımı WorldEdit async işlemleri için değil, Bukkit'in kendi task scheduler'ı içindir.
        // WorldEdit operasyonları genellikle kendi içinde senkron çalışır.
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
            if (adaptedWorld == null) { // Güvenlik kontrolü
                player.sendMessage(ChatColor.RED + "Ada oluşturulurken dünya adaptasyonunda bir hata oluştu.");
                plugin.getLogger().severe("createIsland: Skyblock dünyası WorldEdit'e adapte edilemedi.");
                return;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE); // Daha iyi performans için
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false) // Şematikteki hava bloklarını da yapıştır
                        .build();
                Operations.complete(operation);
            }

            String newIslandName = defaultIslandNamePrefix + "-" + player.getName();
            Island newIsland = new Island(player.getUniqueId(), islandBaseLocation, newIslandName);
            islandDataHandler.addOrUpdateIslandData(newIsland);

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation); // Bu IOException fırlatabilir

                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId); // Varsa eski bölgeyi kaldır
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

            // Teleportasyon
            double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
            double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
            double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
            Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
            teleportLocation.setYaw(0f); // Varsayılan bakış açısı
            teleportLocation.setPitch(0f);

            // Teleport before message, ensure chunk is loaded
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!teleportLocation.getChunk().isLoaded()) {
                        teleportLocation.getChunk().load();
                    }
                    player.teleport(teleportLocation);
                    player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu ve ışınlandınız!");
                    plugin.getLogger().info(player.getName() + " için ada başarıyla oluşturuldu ve ışınlandı: " + teleportLocation);
                }
            }.runTaskLater(plugin, 1L); // Kısa bir gecikme, chunk yüklenmesine zaman tanır


        } catch (Exception e) { // IOException ve WorldEditException dahil
            plugin.getLogger().log(Level.SEVERE, "Ada oluşturma sırasında genel bir hata oluştu (Oyuncu: " + player.getName() + ")", e);
            player.sendMessage(ChatColor.RED + "Ada oluşturulurken çok beklenmedik bir hata oluştu. Lütfen yetkililere bildirin.");
        }
        // dataChangedSinceLastSave, addOrUpdateIslandData içinde true yapılıyor.
        // Ada oluşturma sonrası hemen diske yazmak iyi olabilir.
        islandDataHandler.saveChangesToDisk();
    }

    public boolean deleteIsland(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return false;
        }
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
            if (weWorld == null) { // Güvenlik kontrolü
                plugin.getLogger().severe("Ada silinirken WorldEdit dünyası null geldi (adaptasyon başarısız)! Ada: " + island.getOwnerUUID());
                player.sendMessage(ChatColor.RED + "Ada silinirken bir dünya hatası oluştu.");
                return false;
            }

            // NPEx uyarısı için düzeltme:
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
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(player.getName() + " için " + regionId + " ID'li WorldGuard bölgesi silindi.");
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "WorldGuard bölgeleri silinirken (kayıt) hata: " + regionId, e);
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için silinecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            } else {
                plugin.getLogger().severe("Ada silinirken WorldGuard RegionManager alınamadı! (Dünya: " + islandBaseLocation.getWorld().getName() + ")");
            }

            islandDataHandler.removeIslandData(player.getUniqueId());
            islandDataHandler.saveChangesToDisk(); // Değişiklikleri diske yaz

            revokeOwnerBypassPermissions(player, worldName, regionId);
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla silindi.");
            return true;

        } catch (Exception e) { // IOException, WorldEditException vb.
            plugin.getLogger().log(Level.SEVERE, player.getName() + " ("+island.getOwnerUUID()+") için ada silinirken genel hata: ", e);
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
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı. Bir sorun var!");
            plugin.getLogger().warning("resetIsland: " + player.getName() + " için ada temel konumu veya dünyası null.");
            return false;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Başlangıç ada şematiği bulunamadı ('" + schematicFile.getPath() + "'). Sıfırlama işlemi yapılamıyor.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi sıfırlanıyor... Lütfen bekleyin.");
        try {
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) { // Güvenlik kontrolü
                plugin.getLogger().severe("Ada sıfırlanırken WorldEdit dünyası null geldi (adaptasyon başarısız)! Ada: " + island.getOwnerUUID());
                player.sendMessage(ChatColor.RED + "Ada sıfırlanırken bir dünya hatası oluştu.");
                return false;
            }

            // NPEx uyarısı için düzeltme:
            BlockState airState = BlockTypes.AIR != null ? BlockTypes.AIR.getDefaultState() : null;
            if (airState == null) {
                plugin.getLogger().severe("BlockTypes.AIR null veya getDefaultState çağrılamıyor! WorldEdit düzgün yüklenmemiş olabilir. Ada sıfırlama işlemi durduruldu.");
                player.sendMessage(ChatColor.RED + "Ada sıfırlanırken kritik bir WorldEdit hatası oluştu.");
                return false;
            }

            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                clearSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), airState);
            }
            plugin.getLogger().info(player.getName() + " ("+island.getOwnerUUID()+") için ada bölgesi temizlendi (reset).");

            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().severe("Ada sıfırlanırken şematik formatı tanınamadı: " + schematicFile.getName());
                player.sendMessage(ChatColor.RED + "Ada sıfırlanırken bir hata oluştu. (Şematik Formatı)");
                return false;
            }
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

            // Ada özelliklerini sıfırla
            island.getNamedHomes().clear();
            island.setCurrentBiome(null); // Biyom ada oluşturulduğundaki varsayılana döner (veya şematikten gelir)
            island.setWelcomeMessage(null); // Karşılama mesajı temizlenir
            // Üyeler ve yasaklılar genellikle sıfırlanmaz, ancak istenirse eklenebilir.

            RegionManager regionManager = plugin.getRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    plugin.getLogger().info("'" + regionId + "' için bayraklar sıfırlanıyor (reset)...");
                    region.getFlags().clear(); // Tüm özel bayrakları temizle
                    if (islandFlagManager != null) {
                        islandFlagManager.applyDefaultFlagsToRegion(region); // Varsayılan bayrakları uygula
                    } else {
                        plugin.getLogger().severe("IslandFlagManager null! Ada sıfırlanırken varsayılan bayraklar uygulanamadı.");
                    }
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(player.getName() + " için WorldGuard bölge bayrakları varsayılana sıfırlandı (reset).");
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "WorldGuard bölge bayrakları sıfırlanırken (kayıt) hata: " + regionId, e);
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için sıfırlanacak/güncellenecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            }
            islandDataHandler.addOrUpdateIslandData(island); // Güncellenmiş ada verisini kaydet
            islandDataHandler.saveChangesToDisk();

            // Oyuncuyu adasına ışınla (IslandTeleportManager kullanılabilir veya direkt burada)
            plugin.getIslandTeleportManager().teleportPlayerToIslandSpawn(player);
            //player.sendMessage(ChatColor.GREEN + "Adanız başarıyla sıfırlandı!"); // teleportPlayerToIslandSpawn zaten mesaj gönderiyor olabilir

            return true;

        } catch (Exception e) { // IOException, WorldEditException vb.
            plugin.getLogger().log(Level.SEVERE, player.getName() + " ("+island.getOwnerUUID()+") için ada sıfırlanırken genel hata: ", e);
            player.sendMessage(ChatColor.RED + "Adanız sıfırlanırken çok beklenmedik bir hata oluştu.");
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
        for (StateFlag criticalFlag : criticalFlagsForOwnerBypass) {
            String flagNameForPerm = criticalFlag.getName().toLowerCase().replace("_", "-");
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToAdd.add(PermissionNode.builder(flagSpecificBypassNode).value(true).build());
            plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni ekleniyor: " + flagSpecificBypassNode);
        }

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToAdd.forEach(node -> {
                DataMutateResult addResult = user.data().add(node);
                if (!addResult.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için eklenirken beklenen sonuç alınamadı: " + addResult.name());
                }
            });
            // Lambda ifadeye dönüştürüldü
        }).thenRunAsync(() -> plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla eklendi ve kaydedilmesi istendi."),
                runnable -> Bukkit.getScheduler().runTask(plugin, runnable) // Bukkit ana thread'inde çalıştır
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
        for (StateFlag criticalFlag : criticalFlagsForOwnerBypass) {
            String flagNameForPerm = criticalFlag.getName().toLowerCase().replace("_", "-");
            String flagSpecificBypassNode = "worldguard.bypass.flag." + flagNameForPerm + "." + worldName + "." + regionId;
            nodesToRemove.add(PermissionNode.builder(flagSpecificBypassNode).build()); // value belirtmeye gerek yok kaldırırken
            plugin.getLogger().info(owner.getName() + " için LuckPerms bayrak-özel bypass izni kaldırılıyor: " + flagSpecificBypassNode);
        }

        lpApi.getUserManager().modifyUser(playerUUID, user -> {
            nodesToRemove.forEach(node -> {
                DataMutateResult result = user.data().remove(node);
                if (!result.wasSuccessful() && plugin.getConfig().getBoolean("logging.detailed-luckperms-changes", false)) {
                    plugin.getLogger().warning("LuckPerms izni (" + node.getKey() + ") oyuncu " + owner.getName() + " için kaldırılırken beklenen sonuç alınamadı: " + result.name());
                }
            });
            // Lambda ifadeye dönüştürüldü
        }).thenRunAsync(() -> plugin.getLogger().info(owner.getName() + " için LuckPerms bypass izinleri başarıyla kaldırıldı ve kaydedilmesi istendi."),
                runnable -> Bukkit.getScheduler().runTask(plugin, runnable)
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, owner.getName() + " için LuckPerms izinleri kaldırılırken hata oluştu.", ex);
            return null;
        });
    }
}