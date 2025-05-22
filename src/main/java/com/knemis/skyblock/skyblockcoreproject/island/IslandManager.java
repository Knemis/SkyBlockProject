package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;

// WorldEdit importları
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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;

// WorldGuard importları
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;         // Standart bayrak sabitleri
import com.sk89q.worldguard.protection.flags.StateFlag;    // ALLOW/DENY durumlu bayraklar
import com.sk89q.worldguard.protection.managers.storage.StorageException; // regionManager.save() için

// Bukkit importları
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

// Java importları
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class IslandManager {

    private final SkyBlockProject plugin;
    private World skyblockWorld; // Bukkit World nesnesi
    private File schematicFile;

    private File islandsFile;
    private FileConfiguration islandsConfig;
    private Map<UUID, Location> playerIslands;
    private Map<UUID, Map<String, Location>> playerNamedHomes;
    private int maxHomesPerIsland;

    public IslandManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!schematicFile.exists()) {
            plugin.getLogger().warning("Ada şematiği bulunamadı: " + schematicFile.getPath());
            plugin.getLogger().warning("Lütfen 'plugins/" + plugin.getName() + "/island.schem' dosyasını oluşturun.");
        }

        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        this.playerIslands = new HashMap<>();
        this.playerNamedHomes = new HashMap<>();
        this.maxHomesPerIsland = plugin.getConfig().getInt("island.max-named-homes", 5);
        // loadIslands() çağrısı SkyBlockProject.onEnable() içinden, loadSkyblockWorld() sonrası yapılacak.
    }

    /**
     * Skyblock adalarının bulunacağı dünyayı yükler veya oluşturur.
     */
    public void loadSkyblockWorld() {
        String worldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        this.skyblockWorld = Bukkit.getWorld(worldName);

        if (this.skyblockWorld == null) {
            plugin.getLogger().info(worldName + " dünyası bulunamadı, oluşturuluyor...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.generator(new EmptyWorldGenerator());
            this.skyblockWorld = wc.createWorld();
            if (this.skyblockWorld != null) {
                plugin.getLogger().info(worldName + " dünyası başarıyla oluşturuldu.");
            } else {
                plugin.getLogger().severe(worldName + " dünyası oluşturulamadı!");
            }
        } else {
            plugin.getLogger().info(this.skyblockWorld.getName() + " dünyası başarıyla yüklendi.");
        }
    }

    /**
     * Kayıtlı adaları ve isimlendirilmiş evleri islands.yml dosyasından yükler.
     */
    public void loadIslands() {
        if (!islandsFile.exists()) {
            try {
                islandsFile.createNewFile();
                plugin.getLogger().info("islands.yml oluşturuldu.");
            } catch (IOException e) {
                plugin.getLogger().severe("islands.yml oluşturulamadı: " + e.getMessage());
                return;
            }
        }
        islandsConfig = YamlConfiguration.loadConfiguration(islandsFile);
        playerIslands.clear();
        playerNamedHomes.clear();

        if (islandsConfig.isConfigurationSection("islands")) {
            for (String uuidString : islandsConfig.getConfigurationSection("islands").getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuidString);
                    String worldName = islandsConfig.getString("islands." + uuidString + ".world");
                    double x = islandsConfig.getDouble("islands." + uuidString + ".x");
                    double y = islandsConfig.getDouble("islands." + uuidString + ".y");
                    double z = islandsConfig.getDouble("islands." + uuidString + ".z");
                    World islandWorld = Bukkit.getWorld(worldName);

                    if (islandWorld != null) {
                        playerIslands.put(playerUUID, new Location(islandWorld, x, y, z));

                        Map<String, Location> homes = new HashMap<>();
                        String homesPath = "islands." + uuidString + ".homes";
                        if (islandsConfig.isConfigurationSection(homesPath)) {
                            for (String homeName : islandsConfig.getConfigurationSection(homesPath).getKeys(false)) {
                                String homeWorldName = islandsConfig.getString(homesPath + "." + homeName + ".world");
                                World homeWorld = Bukkit.getWorld(homeWorldName);
                                if (homeWorld != null && homeWorld.equals(islandWorld)) {
                                    double homeX = islandsConfig.getDouble(homesPath + "." + homeName + ".x");
                                    double homeY = islandsConfig.getDouble(homesPath + "." + homeName + ".y");
                                    double homeZ = islandsConfig.getDouble(homesPath + "." + homeName + ".z");
                                    float homeYaw = (float) islandsConfig.getDouble(homesPath + "." + homeName + ".yaw");
                                    float homePitch = (float) islandsConfig.getDouble(homesPath + "." + homeName + ".pitch");
                                    homes.put(homeName.toLowerCase(), new Location(homeWorld, homeX, homeY, homeZ, homeYaw, homePitch));
                                } else {
                                    plugin.getLogger().warning("'" + homeName + "' adlı ev yüklenirken dünya bulunamadı/eşleşmedi: " + (homeWorldName != null ? homeWorldName : "Bilinmeyen Dünya") + " (Oyuncu: " + uuidString + ")");
                                }
                            }
                        }
                        if (!homes.isEmpty()) {
                            playerNamedHomes.put(playerUUID, homes);
                        }
                    } else {
                        plugin.getLogger().warning("Ana ada yüklenirken dünya bulunamadı: " + worldName + " (Oyuncu: " + uuidString + ")");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz UUID formatı islands.yml içinde: " + uuidString + " - " + e.getMessage());
                }
            }
        }
        plugin.getLogger().info(playerIslands.size() + " ana ada ve " + playerNamedHomes.values().stream().mapToInt(Map::size).sum() + " toplam isimlendirilmiş ev konumu başarıyla yüklendi.");
    }

    /**
     * Verilen Bukkit dünyası için WorldGuard RegionManager'ını alır.
     */
    private RegionManager getWGRegionManager(World bukkitWorld) {
        if (bukkitWorld == null) {
            plugin.getLogger().severe("WorldGuard RegionManager alınırken dünya (bukkitWorld) null geldi!");
            return null;
        }
        return plugin.getRegionManager(bukkitWorld);
    }

    /**
     * Oyuncunun WorldGuard bölgesi için benzersiz bir ID oluşturur.
     */
    private String getRegionId(UUID playerUUID) {
        return "skyblock_island_" + playerUUID.toString();
    }

    /**
     * Oyuncu için yeni bir Skyblock adası oluşturur, şematiği yapıştırır ve WorldGuard bölgesini tanımlar.
     */
    public void createIsland(Player player) {
        if (playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Zaten bir adanız var!");
            return;
        }
        if (this.skyblockWorld == null) {
            player.sendMessage(ChatColor.RED + "Skyblock dünyası henüz yüklenmedi. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().severe("createIsland çağrıldığında skyblockWorld null idi!");
            return;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().severe("createIsland çağrıldığında schematicFile bulunamadı: " + schematicFile.getPath());
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.");

        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        plugin.getLogger().info("[DEBUG] IslandManager - createIsland: Ada için kullanılacak X koordinatı: " + actualIslandX);
        final Location islandBaseLocation = new Location(this.skyblockWorld, actualIslandX, 100, 0);
        plugin.getLogger().info("[DEBUG] IslandManager - createIsland: islandBaseLocation oluşturuldu: X=" + islandBaseLocation.getBlockX() + ", Y=" + islandBaseLocation.getBlockY() + ", Z=" + islandBaseLocation.getBlockZ());

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    if (format == null) {
                        player.sendMessage(ChatColor.RED + "Ada oluşturulurken bir hata oluştu. (Şematik Formatı)");
                        plugin.getLogger().severe("Şematik formatı tanınamadı: " + schematicFile.getName());
                        return;
                    }

                    Clipboard clipboard;
                    try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
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

                    registerNewIsland(player, islandBaseLocation);

                    // --- WorldGuard Bölgesi Oluşturma ve Bayrak Ayarlama ---
                    RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
                    if (regionManager != null) {
                        String regionId = getRegionId(player.getUniqueId());
                        try {
                            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);

                            if (regionManager.hasRegion(regionId)) {
                                plugin.getLogger().info("Mevcut WorldGuard bölgesi '" + regionId + "' güncellenmek üzere siliniyor.");
                                regionManager.removeRegion(regionId);
                            }

                            ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(
                                    regionId,
                                    islandTerritory.getMinimumPoint(),
                                    islandTerritory.getMaximumPoint()
                            );

                            protectedRegion.getOwners().addPlayer(player.getUniqueId());
                            protectedRegion.setPriority(10);

                            // Varsayılan Bayraklar: Ziyaretçiler için DENY, Çevreseller için özel ayar
                            // Etkileşim Bayrakları (Sahip bypass etmeli, ziyaretçiler için DENY)
                            plugin.getLogger().info("'" + regionId + "' için varsayılan bayraklar ayarlanıyor...");
                            protectedRegion.setFlag(Flags.BUILD, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.INTERACT, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.USE, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.ITEM_DROP, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.ITEM_PICKUP, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);

                            protectedRegion.setFlag(Flags.TRAMPLE_BLOCKS, StateFlag.State.DENY);

                            // Çevresel Bayraklar (Ada genelini etkiler, sahip dahil)
                            protectedRegion.setFlag(Flags.PVP, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.TNT, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.LAVA_FLOW, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.WATER_FLOW, StateFlag.State.ALLOW);
                            protectedRegion.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
                            protectedRegion.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
                            protectedRegion.setFlag(Flags.LEAF_DECAY, StateFlag.State.ALLOW);
                            // Diğer çevresel bayraklar (örneğin, hayvanlara hasar) eklenebilir.
                            // protectedRegion.setFlag(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);


                            regionManager.addRegion(protectedRegion);
                            try {
                                regionManager.save(); // Değişiklikleri diske kaydet!
                                plugin.getLogger().info(player.getName() + " için WorldGuard bölgesi (" + regionId + ") oluşturuldu ve varsayılan koruma bayrakları ayarlandı.");
                            } catch (StorageException e) {
                                plugin.getLogger().severe("WorldGuard bölgeleri oluşturulurken (kayıt) hata oluştu: " + e.getMessage());
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED + "Ada koruması kaydedilirken kritik bir hata oluştu.");
                            }
                        } catch (IOException e) {
                            plugin.getLogger().severe("Ada için bölge sınırları hesaplanırken hata (WG): " + e.getMessage());
                            player.sendMessage(ChatColor.RED + "Adanız oluşturuldu ancak koruma sınırları belirlenirken bir sorun oluştu.");
                        }
                    } else {
                        plugin.getLogger().severe("Ada için WorldGuard RegionManager alınamadı! (" + islandBaseLocation.getWorld().getName() + ") Koruma oluşturulamadı.");
                        player.sendMessage(ChatColor.RED + "Adanız oluşturuldu ancak koruma sağlanırken bir sorun oluştu.");
                    }

                    double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
                    double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
                    double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
                    Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
                    player.teleport(teleportLocation);
                    player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu ve ışınlandınız!");
                    plugin.getLogger().info(player.getName() + " için ada başarıyla oluşturuldu (Taban: X=" + islandBaseLocation.getBlockX() +
                            ", Y=" + islandBaseLocation.getBlockY() + ", Z=" + islandBaseLocation.getBlockZ() +
                            "), ışınlandı: " + teleportLocation);

                } catch (IOException | WorldEditException e) {
                    plugin.getLogger().severe("Ada oluşturulurken bir WorldEdit/IO hatası oluştu: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Ada oluşturulurken beklenmedik bir hata oluştu.");
                } catch (Exception e) {
                    plugin.getLogger().severe("Ada oluşturma sırasında genel bir hata oluştu: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Ada oluşturulurken çok beklenmedik bir hata oluştu.");
                }
            }
        }.runTask(plugin);
    }

    public boolean deleteIsland(Player player) {
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = playerIslands.get(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Ada şematiği (boyutları belirlemek için) bulunamadı. Silme işlemi yapılamıyor.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi siliniyor...");
        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                editSession.setBlocks(islandTerritory, BlockTypes.AIR.getDefaultState());
            }
            plugin.getLogger().info(player.getName() + " adlı oyuncunun ada bölgesi (" + islandTerritory.toString() + ") başarıyla silindi (bloklar temizlendi).");

            RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                    try {
                        regionManager.save(); // Değişiklikleri diske kaydet
                        plugin.getLogger().info(player.getName() + " için " + regionId + " ID'li WorldGuard bölgesi silindi.");
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WorldGuard bölgeleri silinirken (kayıt) hata oluştu: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için silinecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            } else {
                plugin.getLogger().severe("Ada silinirken WorldGuard RegionManager alınamadı! (" + islandBaseLocation.getWorld().getName() + ")");
            }

            removeIslandDataFromStorage(player.getUniqueId());
            return true;

        } catch (IOException | WorldEditException e) {
            plugin.getLogger().severe(player.getName() + " için ada silinirken hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Adanız silinirken bir hata oluştu.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe(player.getName() + " için ada silinirken genel bir hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Adanız silinirken çok beklenmedik bir hata oluştu.");
            return false;
        }
    }

    public boolean resetIsland(Player player) {
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = playerIslands.get(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı. Bir sorun var!");
            return false;
        }
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Başlangıç ada şematiği bulunamadı. Sıfırlama işlemi yapılamıyor.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi sıfırlanıyor... Lütfen bekleyin.");
        try {
            // 1. Mevcut Ada Alanını Temizle
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                clearSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                clearSession.setBlocks(islandTerritory, BlockTypes.AIR.getDefaultState());
            }
            plugin.getLogger().info(player.getName() + " için ada bölgesi temizlendi (reset).");

            // 2. Başlangıç Şematiğini Yeniden Yapıştır
            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().severe("Ada sıfırlanırken şematik formatı tanınamadı: " + schematicFile.getName());
                player.sendMessage(ChatColor.RED + "Ada sıfırlanırken bir hata oluştu. (Şematik Formatı)");
                return false;
            }
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }
            try (EditSession pasteSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                pasteSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }
            plugin.getLogger().info(player.getName() + " için ada şematiği yeniden yapıştırıldı (reset).");

            // 3. Oyuncunun tüm isimlendirilmiş evlerini sil
            clearNamedHomesForPlayer(player.getUniqueId());

            // 4. WorldGuard bölge bayraklarını varsayılana sıfırla
            RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    plugin.getLogger().info("'" + regionId + "' için bayraklar sıfırlanıyor (reset)...");
                    // Etkileşim Bayrakları
                    region.setFlag(Flags.BUILD, StateFlag.State.DENY);
                    region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
                    region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
                    region.setFlag(Flags.USE, StateFlag.State.DENY);
                    region.setFlag(Flags.ITEM_DROP, StateFlag.State.DENY);
                    region.setFlag(Flags.ITEM_PICKUP, StateFlag.State.DENY);
                    region.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);

                    region.setFlag(Flags.TRAMPLE_BLOCKS, StateFlag.State.DENY);
                    // Çevresel Bayraklar
                    region.setFlag(Flags.PVP, StateFlag.State.DENY);
                    region.setFlag(Flags.TNT, StateFlag.State.DENY);
                    region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
                    region.setFlag(Flags.LAVA_FLOW, StateFlag.State.DENY);
                    region.setFlag(Flags.WATER_FLOW, StateFlag.State.ALLOW);
                    region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
                    region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
                    region.setFlag(Flags.LEAF_DECAY, StateFlag.State.ALLOW);
                    // Sahiplik aynı kalır.
                    try {
                        regionManager.save();
                        plugin.getLogger().info(player.getName() + " için WorldGuard bölge bayrakları varsayılana sıfırlandı (reset).");
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WorldGuard bölge bayrakları sıfırlanırken (kayıt) hata: " + e.getMessage());
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için sıfırlanacak/güncellenecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            }

            // 5. Oyuncuyu adasının varsayılan spawn noktasına ışınla
            teleportToIsland(player);
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla sıfırlandı!");
            return true;

        } catch (IOException | WorldEditException e) {
            plugin.getLogger().severe(player.getName() + " için ada sıfırlanırken WorldEdit/IO hatası: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Adanız sıfırlanırken bir hata oluştu.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe(player.getName() + " için ada sıfırlanırken genel bir hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Adanız sıfırlanırken çok beklenmedik bir hata oluştu.");
            return false;
        }
    }

    public void removeIslandDataFromStorage(UUID playerUUID) {
        if (playerIslands.containsKey(playerUUID)) {
            playerIslands.remove(playerUUID);
        }
        if (playerNamedHomes.containsKey(playerUUID)) {
            playerNamedHomes.remove(playerUUID);
        }
        String playerPath = "islands." + playerUUID.toString();
        if (islandsConfig.isConfigurationSection(playerPath)) {
            islandsConfig.set(playerPath, null);
            try {
                islandsConfig.save(islandsFile);
                plugin.getLogger().info(Bukkit.getOfflinePlayer(playerUUID).getName() + " adlı oyuncunun tüm ada verisi islands.yml dosyasından silindi.");
            } catch (IOException e) {
                plugin.getLogger().severe("islands.yml güncellenirken hata (ada silme): " + e.getMessage());
            }
        }
    }

    public void registerNewIsland(Player player, Location islandLocation) {
        playerIslands.put(player.getUniqueId(), islandLocation);
        String uuidString = player.getUniqueId().toString();
        String basePath = "islands." + uuidString + ".";
        islandsConfig.set(basePath + "world", islandLocation.getWorld().getName());
        islandsConfig.set(basePath + "x", islandLocation.getBlockX());
        islandsConfig.set(basePath + "y", islandLocation.getBlockY());
        islandsConfig.set(basePath + "z", islandLocation.getBlockZ());
        islandsConfig.createSection(basePath + "homes"); // Başlangıçta boş bir homes bölümü oluştur
        try {
            islandsConfig.save(islandsFile);
            plugin.getLogger().info(player.getName() + " için ada bilgisi (boş homes ile) islands.yml dosyasına kaydedildi.");
        } catch (IOException e) {
            plugin.getLogger().severe(player.getName() + " için ada bilgisi islands.yml dosyasına kaydedilemedi: " + e.getMessage());
        }
    }

    public boolean playerHasIsland(Player player) {
        return playerIslands.containsKey(player.getUniqueId());
    }

    private CuboidRegion getPastedSchematicRegion(Location islandBaseLocation) throws IOException {
        if (!schematicFile.exists()) {
            throw new IOException("Ada şematiği bulunamadı: " + schematicFile.getPath());
        }
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            throw new IOException("Ada şematiği formatı tanınamadı: " + schematicFile.getName());
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            clipboard = reader.read();
        }
        BlockVector3 pasteOriginInSchematic = clipboard.getOrigin();
        BlockVector3 islandPastePoint = BlockVector3.at(islandBaseLocation.getBlockX(), islandBaseLocation.getBlockY(), islandBaseLocation.getBlockZ());
        BlockVector3 clipboardMinRel = clipboard.getRegion().getMinimumPoint();
        BlockVector3 clipboardMaxRel = clipboard.getRegion().getMaximumPoint();
        BlockVector3 worldMinSchematic = islandPastePoint.add(clipboardMinRel.subtract(pasteOriginInSchematic));
        BlockVector3 worldMaxSchematic = islandPastePoint.add(clipboardMaxRel.subtract(pasteOriginInSchematic));
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
        return new CuboidRegion(weWorld, worldMinSchematic, worldMaxSchematic);
    }

    private CuboidRegion getIslandTerritoryRegion(Location islandBaseLocation) throws IOException {
        CuboidRegion schematicRegion = getPastedSchematicRegion(islandBaseLocation);
        int expansionRadius = plugin.getConfig().getInt("island.expansion-radius-horizontal", 50);
        boolean allowBuildBelowBase = plugin.getConfig().getBoolean("island.allow-build-below-schematic-base", false);
        int buildHeightAboveTop = plugin.getConfig().getInt("island.build-limit-above-schematic-top", 150);

        int worldMinBuildHeight = 0;
        int worldMaxBuildHeight = islandBaseLocation.getWorld().getMaxHeight() - 1;

        BlockVector3 schematicMin = schematicRegion.getMinimumPoint();
        BlockVector3 schematicMax = schematicRegion.getMaximumPoint();

        int territoryMinX = schematicMin.getBlockX() - expansionRadius;
        int territoryMaxX = schematicMax.getBlockX() + expansionRadius;
        int territoryMinZ = schematicMin.getBlockZ() - expansionRadius;
        int territoryMaxZ = schematicMax.getBlockZ() + expansionRadius;

        int territoryMinY = allowBuildBelowBase ? worldMinBuildHeight : schematicMin.getBlockY();
        int territoryMaxY = Math.min(worldMaxBuildHeight, schematicMax.getBlockY() + buildHeightAboveTop);

        if (territoryMinY > territoryMaxY) {
            territoryMinY = schematicMin.getBlockY();
            territoryMaxY = Math.max(territoryMinY, schematicMax.getBlockY());
            plugin.getLogger().warning("Hesaplanan ada bölgesi Y sınırları geçersizdi (minY > maxY). Şematik Y sınırlarına geri dönüldü. Ada konumu: " + islandBaseLocation.toString());
        }

        return new CuboidRegion(schematicRegion.getWorld(),
                BlockVector3.at(territoryMinX, territoryMinY, territoryMinZ),
                BlockVector3.at(territoryMaxX, territoryMaxY, territoryMaxZ));
    }

    public boolean setNamedHome(Player player, String homeName, Location homeLocation) {
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Ev noktanı ayarlayabileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = playerIslands.get(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Bir hata oluştu: Ada temel konumu veya dünyası bulunamadı.");
            return false;
        }
        if (!homeLocation.getWorld().equals(islandBaseLocation.getWorld())) {
            player.sendMessage(ChatColor.RED + "Ev noktanı sadece kendi adanın dünyasında ayarlayabilirsin!");
            return false;
        }

        String homeNameLower = homeName.toLowerCase();
        Map<String, Location> homes = playerNamedHomes.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        if (!homes.containsKey(homeNameLower) && homes.size() >= maxHomesPerIsland) {
            player.sendMessage(ChatColor.RED + "Maksimum ev sayısına (" + maxHomesPerIsland + ") ulaştın. Yeni bir ev ayarlamak için önce birini silmelisin.");
            return false;
        }
        if (homeNameLower.length() < 2 || homeNameLower.length() > 16 || !homeNameLower.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(ChatColor.RED + "Ev adı 2-16 karakter uzunluğunda olmalı ve sadece harf, rakam veya alt çizgi içerebilir.");
            return false;
        }

        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            if (!islandTerritory.contains(BlockVector3.at(homeLocation.getX(), homeLocation.getY(), homeLocation.getZ()))) {
                player.sendMessage(ChatColor.RED + "Ev noktanı sadece adanın (genişletilmiş) sınırları içinde ayarlayabilirsin!");
                return false;
            }

            homes.put(homeNameLower, homeLocation.clone());
            // playerNamedHomes.put(player.getUniqueId(), homes); // computeIfAbsent zaten map'i günceller

            String uuidString = player.getUniqueId().toString();
            String homePath = "islands." + uuidString + ".homes." + homeNameLower + ".";
            islandsConfig.set(homePath + "world", homeLocation.getWorld().getName());
            islandsConfig.set(homePath + "x", homeLocation.getX());
            islandsConfig.set(homePath + "y", homeLocation.getY());
            islandsConfig.set(homePath + "z", homeLocation.getZ());
            islandsConfig.set(homePath + "yaw", homeLocation.getYaw());
            islandsConfig.set(homePath + "pitch", homeLocation.getPitch());
            try {
                islandsConfig.save(islandsFile);
                player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı ev noktan ayarlandı!");
                return true;
            } catch (IOException e) {
                plugin.getLogger().severe(player.getName() + " için '" + homeName + "' evi kaydedilemedi: " + e.getMessage());
                player.sendMessage(ChatColor.RED + "Ev noktan kaydedilirken bir hata oluştu.");
                return false;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Ada sınırları hesaplanırken hata (setNamedHome): " + e.getMessage());
            player.sendMessage(ChatColor.RED + "Ev noktan ayarlanırken bir hata oluştu (sınırlar).");
            return false;
        }
    }

    public boolean deleteNamedHome(Player player, String homeName) {
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Ev silebilmek için önce bir adanız olmalı!");
            return false;
        }
        UUID playerUUID = player.getUniqueId();
        String homeNameLower = homeName.toLowerCase();
        Map<String, Location> homes = playerNamedHomes.get(playerUUID);

        if (homes == null || !homes.containsKey(homeNameLower)) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adında bir ev noktanız bulunmuyor.");
            return false;
        }
        homes.remove(homeNameLower);
        if (homes.isEmpty()) {
            playerNamedHomes.remove(playerUUID);
        }
        islandsConfig.set("islands." + playerUUID.toString() + ".homes." + homeNameLower, null);
        try {
            islandsConfig.save(islandsFile);
            player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı ev noktanız başarıyla silindi.");
            plugin.getLogger().info(player.getName() + " oyuncusunun '" + homeNameLower + "' adlı evi silindi.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe(player.getName() + " için '" + homeNameLower + "' evi silinirken islands.yml kaydedilemedi: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "Ev noktanız silinirken bir dosya hatası oluştu.");
            return false;
        }
    }

    public Location getNamedHomeLocation(Player player, String homeName) {
        if (!playerHasIsland(player)) return null;
        Map<String, Location> homes = playerNamedHomes.get(player.getUniqueId());
        if (homes == null) return null;
        return homes.get(homeName.toLowerCase());
    }

    public List<String> getNamedHomesList(Player player) {
        if (!playerHasIsland(player) || !playerNamedHomes.containsKey(player.getUniqueId())) {
            return new ArrayList<>();
        }
        return new ArrayList<>(playerNamedHomes.get(player.getUniqueId()).keySet());
    }

    public void teleportToIsland(Player player) { // Ana ada spawn
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Henüz bir adanız yok! Oluşturmak için /island create yazın.");
            return;
        }
        Location islandBaseLocation = playerIslands.get(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanızın konumu veya dünyası bulunamadı.");
            return;
        }
        double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
        double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
        double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
        Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
        teleportLocation.setYaw(player.getLocation().getYaw());
        teleportLocation.setPitch(player.getLocation().getPitch());
        player.teleport(teleportLocation);
        player.sendMessage(ChatColor.GREEN + "Adanızın ana noktasına ışınlandınız!");
    }

    public void teleportToNamedHome(Player player, String homeName) {
        if (!playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Önce bir ada oluşturmalısın!");
            return;
        }
        Location homeLocation = getNamedHomeLocation(player, homeName);
        if (homeLocation == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adında bir ev noktan bulunmuyor. Evlerini listelemek için /island home list yaz.");
            return;
        }
        if (homeLocation.getWorld() == null || Bukkit.getWorld(homeLocation.getWorld().getName()) == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adlı evinin bulunduğu dünya yüklenemedi!");
            return;
        }
        player.teleport(homeLocation);
        player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı evine ışınlandın!");
    }

    public void clearNamedHomesForPlayer(UUID playerUUID) {
        if (playerNamedHomes.containsKey(playerUUID)) {
            playerNamedHomes.remove(playerUUID);
            plugin.getLogger().info(Bukkit.getOfflinePlayer(playerUUID).getName() + " için tüm isimlendirilmiş evler hafızadan silindi (reset).");
        }
        String homesPath = "islands." + playerUUID.toString() + ".homes";
        if (islandsConfig.isConfigurationSection(homesPath)) {
            islandsConfig.set(homesPath, null);
            try {
                islandsConfig.save(islandsFile);
                plugin.getLogger().info(Bukkit.getOfflinePlayer(playerUUID).getName() + " için tüm isimlendirilmiş evler islands.yml dosyasından silindi (reset).");
            } catch (IOException e) {
                plugin.getLogger().severe("islands.yml güncellenirken hata (named homes silme - reset): " + e.getMessage());
            }
        }
    }

    /**
     * Belirtilen oyuncunun adasındaki belirli bir StateFlag'ın durumunu alır.
     * @param playerUUID Oyuncunun UUID'si
     * @param flag Durumu alınacak StateFlag (örn: Flags.BUILD)
     * @return Bayrağın durumu (ALLOW, DENY) veya ayarlanmamışsa null.
     */
    public StateFlag.State getIslandFlagState(UUID playerUUID, StateFlag flag) {
        if (!playerIslands.containsKey(playerUUID)) {
            return null; // Oyuncunun adası yok
        }
        Location islandBaseLocation = playerIslands.get(playerUUID);
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            plugin.getLogger().warning("getIslandFlagState: Ada konumu veya dünyası null. UUID: " + playerUUID);
            return null;
        }

        RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
        if (regionManager == null) {
            plugin.getLogger().warning("getIslandFlagState: RegionManager null. Dünya: " + islandBaseLocation.getWorld().getName() + ", UUID: " + playerUUID);
            return null;
        }

        String regionId = getRegionId(playerUUID);
        ProtectedRegion region = regionManager.getRegion(regionId); // regionId kullanılmalıydı
        if (region != null) {
            return region.getFlag(flag);
        } else {
            plugin.getLogger().warning("getIslandFlagState: WorldGuard bölgesi bulunamadı: " + regionId);
        }
        return null;
    }

    /**
     * Belirtilen oyuncunun adasındaki belirli bir StateFlag'ın durumunu ayarlar.
     * @param playerUUID Oyuncunun UUID'si.
     * @param flag Durumu ayarlanacak StateFlag (örn: Flags.BUILD).
     * @param newState Bayrağın yeni durumu (ALLOW, DENY) veya null (bayrağı temizlemek/varsayılana döndürmek için).
     * @return İşlem başarılıysa true, değilse false.
     */
    public boolean setIslandFlagState(UUID playerUUID, StateFlag flag, StateFlag.State newState) {
        if (!playerIslands.containsKey(playerUUID)) {
            plugin.getLogger().warning("setIslandFlagState: Bayrak ayarlanmaya çalışılan oyuncunun adası bulunamadı: " + playerUUID);
            return false;
        }
        Location islandBaseLocation = playerIslands.get(playerUUID);
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            plugin.getLogger().severe("setIslandFlagState: Bayrak ayarlanırken ada konumu veya dünyası null geldi: " + playerUUID);
            return false;
        }

        RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
        if (regionManager == null) {
            plugin.getLogger().severe("setIslandFlagState: Bayrak ayarlanırken RegionManager alınamadı (Dünya: " + islandBaseLocation.getWorld().getName() + ")");
            return false;
        }

        String regionId = getRegionId(playerUUID);
        ProtectedRegion region = regionManager.getRegion(regionId); // regionId kullanılmalıydı

        if (region != null) {
            try {
                region.setFlag(flag, newState);
                regionManager.save(); // Değişiklikleri kaydet
                plugin.getLogger().info("Oyuncu " + playerUUID + " için '" + regionId + "' bölgesinde '" + flag.getName() +
                        "' bayrağı '" + (newState != null ? newState.name() : "VARSAYILAN (kaldırıldı)") + "' olarak ayarlandı.");
                return true;
            } catch (StorageException e) {
                plugin.getLogger().severe("WorldGuard bölgesi '" + regionId + "' kaydedilirken hata (bayrak ayarı): " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            plugin.getLogger().warning("setIslandFlagState: Bayrak ayarlanmak istenen WorldGuard bölgesi bulunamadı: " + regionId);
            return false;
        }
    }

    // Skyblock dünyası için boş bir dünya üreticisi
    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) { return Bukkit.createChunkData(world); }
        @Override public Location getFixedSpawnLocation(World world, Random random) { return new Location(world, 0.5, 128, 0.5); }
    }
}