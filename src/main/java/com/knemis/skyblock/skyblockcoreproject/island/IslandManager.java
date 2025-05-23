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
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.storage.StorageException;

// Bukkit importları
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class IslandManager {

    private final SkyBlockProject plugin;
    private World skyblockWorld; // Bukkit World nesnesi
    private File schematicFile;

    private File islandsFile;
    private FileConfiguration islandsConfig;

    private Map<UUID, Island> islandsData; // Oyuncu UUID -> Island nesnesi

    private int maxHomesPerIsland;
    private final String defaultIslandNamePrefix;

    public IslandManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!schematicFile.exists()) {
            plugin.getLogger().warning("Ada şematiği bulunamadı: " + schematicFile.getPath());
            plugin.getLogger().warning("Lütfen 'plugins/" + plugin.getName() + "/island.schem' dosyasını oluşturun.");
        }

        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        this.islandsData = new HashMap<>();
        this.maxHomesPerIsland = plugin.getConfig().getInt("island.max-named-homes", 5);
    }

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
        islandsData.clear();

        if (islandsConfig.isConfigurationSection("islands")) {
            for (String uuidString : islandsConfig.getConfigurationSection("islands").getKeys(false)) {
                try {
                    UUID ownerUUID = UUID.fromString(uuidString);
                    String path = "islands." + uuidString + ".";

                    String worldName = islandsConfig.getString(path + "baseLocation.world");
                    double x = islandsConfig.getDouble(path + "baseLocation.x");
                    double y = islandsConfig.getDouble(path + "baseLocation.y");
                    double z = islandsConfig.getDouble(path + "baseLocation.z");
                    World islandWorld = Bukkit.getWorld(worldName);

                    if (islandWorld == null) {
                        plugin.getLogger().warning("Ada yüklenirken dünya bulunamadı: " + worldName + " (Oyuncu: " + uuidString + "). Ada yüklenemedi.");
                        continue;
                    }
                    Location baseLocation = new Location(islandWorld, x, y, z);

                    String islandName = islandsConfig.getString(path + "islandName", defaultIslandNamePrefix + "-" + Bukkit.getOfflinePlayer(ownerUUID).getName());
                    long creationTimestamp = islandsConfig.getLong(path + "creationDate", System.currentTimeMillis());
                    boolean isPublic = islandsConfig.getBoolean(path + "isPublic", false);
                    boolean boundariesEnforced = islandsConfig.getBoolean(path + "boundariesEnforced", true);

                    Set<UUID> members = new HashSet<>();
                    List<String> memberUUIDStrings = islandsConfig.getStringList(path + "members");
                    for (String memberUUIDString : memberUUIDStrings) {
                        try {
                            members.add(UUID.fromString(memberUUIDString));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Geçersiz üye UUID formatı: " + memberUUIDString + " (Ada Sahibi: " + uuidString + ")");
                        }
                    }

                    Set<UUID> bannedPlayers = new HashSet<>();
                    List<String> bannedPlayerUUIDStrings = islandsConfig.getStringList(path + "bannedPlayers");
                    for (String bannedUUIDString : bannedPlayerUUIDStrings) {
                        try {
                            bannedPlayers.add(UUID.fromString(bannedUUIDString));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Geçersiz yasaklı oyuncu UUID formatı: " + bannedUUIDString + " (Ada Sahibi: " + uuidString + ")");
                        }
                    }

                    Map<String, Location> namedHomes = new HashMap<>();
                    ConfigurationSection homesSection = islandsConfig.getConfigurationSection(path + "homes");
                    if (homesSection != null) {
                        for (String homeNameKey : homesSection.getKeys(false)) {
                            String homePath = path + "homes." + homeNameKey + ".";
                            String homeWorldName = islandsConfig.getString(homePath + "world");
                            World homeWorld = Bukkit.getWorld(homeWorldName);
                            if (homeWorld != null && homeWorld.equals(islandWorld)) {
                                double hx = islandsConfig.getDouble(homePath + "x");
                                double hy = islandsConfig.getDouble(homePath + "y");
                                double hz = islandsConfig.getDouble(homePath + "z");
                                float hyaw = (float) islandsConfig.getDouble(homePath + "yaw");
                                float hpitch = (float) islandsConfig.getDouble(homePath + "pitch");
                                namedHomes.put(homeNameKey.toLowerCase(), new Location(homeWorld, hx, hy, hz, hyaw, hpitch));
                            } else {
                                plugin.getLogger().warning("'" + homeNameKey + "' adlı ev yüklenirken dünya bulunamadı/eşleşmedi: " +
                                        (homeWorldName != null ? homeWorldName : "Bilinmeyen Dünya") + " (Oyuncu: " + uuidString + ")");
                            }
                        }
                    }

                    Island island = new Island(ownerUUID, islandName, baseLocation, creationTimestamp, isPublic, boundariesEnforced, members, bannedPlayers, namedHomes);
                    islandsData.put(ownerUUID, island);

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz sahip UUID formatı islands.yml içinde: " + uuidString + " - " + e.getMessage());
                } catch (Exception e) {
                    plugin.getLogger().severe("Ada yüklenirken beklenmedik bir hata oluştu (Sahip: " + uuidString + "): " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        plugin.getLogger().info(islandsData.size() + " ada verisi başarıyla yüklendi.");
    }

    public void saveAllIslandData() {
        if (islandsData.isEmpty()) {
            if (islandsFile.exists() && islandsFile.length() > 0) {
                try {
                    islandsConfig.set("islands", null);
                    islandsConfig.save(islandsFile);
                    plugin.getLogger().info("Hiç ada verisi olmadığı için islands.yml temizlendi.");
                } catch (IOException e) {
                    plugin.getLogger().severe("islands.yml temizlenirken hata: " + e.getMessage());
                }
            }
            return;
        }
        islandsConfig.set("islands", null); // Önce temizle

        for (Island island : islandsData.values()) {
            saveIslandData(island);
        }
        try {
            islandsConfig.save(islandsFile);
            plugin.getLogger().info(islandsData.size() + " ada verisi islands.yml dosyasına kaydedildi.");
        } catch (IOException e) {
            plugin.getLogger().severe("Tüm ada verileri islands.yml dosyasına kaydedilemedi: " + e.getMessage());
        }
    }

    public void saveIslandData(Island island) {
        if (island == null) return;

        String uuidString = island.getOwnerUUID().toString();
        String path = "islands." + uuidString + ".";

        islandsConfig.set(path + "islandName", island.getIslandName());
        islandsConfig.set(path + "creationDate", island.getCreationTimestamp());
        islandsConfig.set(path + "isPublic", island.isPublic());
        islandsConfig.set(path + "boundariesEnforced", island.areBoundariesEnforced());

        Location baseLoc = island.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName());
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX());
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY());
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ());
        } else {
            plugin.getLogger().warning("Ada kaydedilirken temel konum veya dünya null geldi (Sahip: " + uuidString + "). Konum kaydedilemedi.");
        }

        List<String> memberUUIDStrings = island.getMembers().stream().map(UUID::toString).collect(Collectors.toList());
        islandsConfig.set(path + "members", memberUUIDStrings);

        List<String> bannedPlayerUUIDStrings = island.getBannedPlayers().stream().map(UUID::toString).collect(Collectors.toList());
        islandsConfig.set(path + "bannedPlayers", bannedPlayerUUIDStrings);

        islandsConfig.set(path + "homes", null); // Önce eski evleri temizle
        if (island.getNamedHomes() != null && !island.getNamedHomes().isEmpty()) {
            for (Map.Entry<String, Location> homeEntry : island.getNamedHomes().entrySet()) {
                String homeName = homeEntry.getKey(); // Bu zaten küçük harfle kaydedilmiş olmalı Island sınıfında
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

    private RegionManager getWGRegionManager(World bukkitWorld) {
        if (bukkitWorld == null) {
            plugin.getLogger().severe("WorldGuard RegionManager alınırken dünya (bukkitWorld) null geldi!");
            return null;
        }
        return plugin.getRegionManager(bukkitWorld);
    }

    private String getRegionId(UUID playerUUID) {
        return "skyblock_island_" + playerUUID.toString();
    }

    public Island getIsland(Player player) {
        return islandsData.get(player.getUniqueId());
    }

    public Island getIslandByOwner(UUID ownerUUID) {
        return islandsData.get(ownerUUID);
    }

    public Island getIslandAt(Location location) {
        if (location == null || location.getWorld() == null || !location.getWorld().equals(skyblockWorld)) {
            return null;
        }
        for (Island island : islandsData.values()) {
            try {
                ProtectedRegion region = getProtectedRegion(island.getOwnerUUID());
                if (region != null && region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                    return island;
                }
            } catch (Exception e) {
                plugin.getLogger().finer("getIslandAt içinde bölge kontrolü sırasında hata: " + e.getMessage());
            }
        }
        return null;
    }

    public ProtectedRegion getProtectedRegion(UUID ownerUUID) {
        Island island = getIslandByOwner(ownerUUID);
        if (island == null || island.getBaseLocation() == null || island.getWorld() == null) {
            return null;
        }
        RegionManager regionManager = getWGRegionManager(island.getWorld());
        if (regionManager == null) {
            return null;
        }
        return regionManager.getRegion(getRegionId(ownerUUID));
    }

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
        final Location islandBaseLocation = new Location(this.skyblockWorld, actualIslandX, 100, 0);

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
                    islandsData.put(player.getUniqueId(), newIsland);
                    saveIslandData(newIsland);
                    try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Yeni ada kaydedilirken hata: " + e.getMessage());}

                    RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
                    if (regionManager != null) {
                        String regionId = getRegionId(player.getUniqueId());
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
                        protectedRegion.setPriority(plugin.getConfig().getInt("island.region-priority", 10));
                        plugin.getLogger().info("'" + regionId + "' için varsayılan ziyaretçi bayrakları ayarlanıyor...");
                        setInitialRegionFlags(protectedRegion);

                        regionManager.addRegion(protectedRegion);
                        try {
                            regionManager.saveChanges();
                            plugin.getLogger().info(player.getName() + " için WorldGuard bölgesi (" + regionId + ") oluşturuldu ve varsayılan koruma bayrakları ayarlandı.");
                        } catch (StorageException e) {
                            plugin.getLogger().severe("WorldGuard bölgeleri oluşturulurken (kayıt) hata: " + e.getMessage());
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Ada koruması kaydedilirken kritik bir hata oluştu.");
                        }
                    } else {
                        plugin.getLogger().severe("Ada için WorldGuard RegionManager alınamadı! (" + islandBaseLocation.getWorld().getName() + ") Koruma oluşturulamadı.");
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
                            if (teleportLocation.getChunk().isLoaded()) {
                                player.teleport(teleportLocation);
                                player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu ve ışınlandınız!");
                                plugin.getLogger().info(player.getName() + " için ada başarıyla oluşturuldu ve ışınlandı: " + teleportLocation);
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 1L);


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

    private void setInitialRegionFlags(ProtectedRegion region) {
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        region.setFlag(Flags.ITEM_DROP, StateFlag.State.DENY);
        region.setFlag(Flags.ITEM_PICKUP, StateFlag.State.DENY);
        region.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);
        region.setFlag(Flags.TRAMPLE_BLOCKS, StateFlag.State.DENY);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.LAVA_FLOW, StateFlag.State.DENY);
        region.setFlag(Flags.WATER_FLOW, StateFlag.State.ALLOW);
        region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        region.setFlag(Flags.LEAF_DECAY, StateFlag.State.ALLOW);
    }

    public boolean deleteIsland(Player player) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi siliniyor...");
        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) {
                plugin.getLogger().severe("Ada silinirken WorldEdit dünyası null geldi!");
                player.sendMessage(ChatColor.RED + "Ada silinirken bir dünya hatası oluştu.");
                return false;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                editSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), BlockTypes.AIR.getDefaultState());
            }
            plugin.getLogger().info(player.getName() + " adlı oyuncunun ada bölgesi (" + islandTerritory.toString() + ") başarıyla silindi (bloklar temizlendi).");

            RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                if (regionManager.hasRegion(regionId)) {
                    regionManager.removeRegion(regionId);
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(player.getName() + " için " + regionId + " ID'li WorldGuard bölgesi silindi.");
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WorldGuard bölgeleri silinirken (kayıt) hata: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için silinecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            } else {
                plugin.getLogger().severe("Ada silinirken WorldGuard RegionManager alınamadı! (" + islandBaseLocation.getWorld().getName() + ")");
            }

            removeIslandDataFromStorage(player.getUniqueId());
            try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada silindikten sonra islands.yml kaydedilirken hata: " + e.getMessage());}

            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla silindi.");
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
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
            return false;
        }
        Location islandBaseLocation = island.getBaseLocation();
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
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(islandBaseLocation.getWorld());
            if (weWorld == null) {
                plugin.getLogger().severe("Ada sıfırlanırken WorldEdit dünyası null geldi!");
                player.sendMessage(ChatColor.RED + "Ada sıfırlanırken bir dünya hatası oluştu.");
                return false;
            }
            CuboidRegion islandTerritory = getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                clearSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                clearSession.setBlocks(new CuboidRegion(weWorld, islandTerritory.getMinimumPoint(), islandTerritory.getMaximumPoint()), BlockTypes.AIR.getDefaultState());
            }
            plugin.getLogger().info(player.getName() + " için ada bölgesi temizlendi (reset).");
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
                pasteSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }
            plugin.getLogger().info(player.getName() + " için ada şematiği yeniden yapıştırıldı (reset).");
            island.getNamedHomes().clear();
            RegionManager regionManager = getWGRegionManager(islandBaseLocation.getWorld());
            if (regionManager != null) {
                String regionId = getRegionId(player.getUniqueId());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    plugin.getLogger().info("'" + regionId + "' için bayraklar sıfırlanıyor (reset)...");
                    region.getFlags().clear();
                    setInitialRegionFlags(region);
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(player.getName() + " için WorldGuard bölge bayrakları varsayılana sıfırlandı (reset).");
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WorldGuard bölge bayrakları sıfırlanırken (kayıt) hata: " + e.getMessage());
                    }
                } else {
                    plugin.getLogger().warning(player.getName() + " için sıfırlanacak/güncellenecek WorldGuard bölgesi (" + regionId + ") bulunamadı.");
                }
            }
            saveIslandData(island);
            try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada sıfırlandıktan sonra islands.yml kaydedilirken hata: " + e.getMessage());}

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
        islandsData.remove(playerUUID);
        islandsConfig.set("islands." + playerUUID.toString(), null);
        plugin.getLogger().info(Bukkit.getOfflinePlayer(playerUUID).getName() + " adlı oyuncunun tüm ada verisi islands.yml dosyasından silinmek üzere işaretlendi.");
    }

    public boolean playerHasIsland(Player player) {
        return islandsData.containsKey(player.getUniqueId());
    }
    public boolean playerHasIsland(UUID playerUUID) {
        return islandsData.containsKey(playerUUID);
    }

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

        int worldMinBuildHeight = 0; // Veya islandBaseLocation.getWorld().getMinHeight() (1.17+)
        int worldMaxBuildHeight = islandBaseLocation.getWorld().getMaxHeight() -1;

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
        return new CuboidRegion(schematicRegion.getWorld(),
                BlockVector3.at(territoryMinX, territoryMinY, territoryMinZ),
                BlockVector3.at(territoryMaxX, territoryMaxY, territoryMaxZ));
    }

    public boolean setNamedHome(Player player, String homeName, Location homeLocation) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Ev noktanı ayarlayabileceğin bir adan yok!");
            return false;
        }
        if (island.getBaseLocation() == null || island.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Bir hata oluştu: Ada temel konumu veya dünyası bulunamadı.");
            return false;
        }
        if (homeLocation == null || homeLocation.getWorld() == null || !homeLocation.getWorld().equals(island.getWorld())) {
            player.sendMessage(ChatColor.RED + "Ev noktanı sadece kendi adanın dünyasında ayarlayabilirsin!");
            return false;
        }

        String homeNameLower = homeName.toLowerCase();
        Map<String, Location> homes = island.getNamedHomes();

        if (!homes.containsKey(homeNameLower) && homes.size() >= maxHomesPerIsland) {
            player.sendMessage(ChatColor.RED + "Maksimum ev sayısına (" + maxHomesPerIsland + ") ulaştın. Yeni bir ev ayarlamak için önce birini silmelisin.");
            return false;
        }
        String homeNamePattern = plugin.getConfig().getString("island.home-name-pattern", "^[a-zA-Z0-9_]{2,16}$");
        if (!homeNameLower.matches(homeNamePattern)) {
            player.sendMessage(ChatColor.RED + "Ev adı 2-16 karakter uzunluğunda olmalı ve sadece harf, rakam veya alt çizgi içerebilir.");
            return false;
        }

        try {
            CuboidRegion islandTerritory = getIslandTerritoryRegion(island.getBaseLocation());
            if (!islandTerritory.contains(BlockVector3.at(homeLocation.getX(), homeLocation.getY(), homeLocation.getZ()))) {
                player.sendMessage(ChatColor.RED + "Ev noktanı sadece adanın (genişletilmiş) sınırları içinde ayarlayabilirsin!");
                return false;
            }

            island.setNamedHome(homeNameLower, homeLocation.clone());
            saveIslandData(island);
            try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ev noktası kaydedilirken hata: " + e.getMessage());}

            player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı ev noktan ayarlandı!");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Ada sınırları hesaplanırken hata (setNamedHome): " + e.getMessage());
            player.sendMessage(ChatColor.RED + "Ev noktan ayarlanırken bir hata oluştu (sınırlar).");
            return false;
        }
    }

    public boolean deleteNamedHome(Player player, String homeName) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Ev silebilmek için önce bir adanız olmalı!");
            return false;
        }
        String homeNameLower = homeName.toLowerCase();
        if (!island.getNamedHomes().containsKey(homeNameLower)) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adında bir ev noktanız bulunmuyor.");
            return false;
        }

        island.deleteNamedHome(homeNameLower);
        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ev noktası silinirken hata: " + e.getMessage());}

        player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı ev noktanız başarıyla silindi.");
        plugin.getLogger().info(player.getName() + " oyuncusunun '" + homeNameLower + "' adlı evi silindi.");
        return true;
    }

    public Location getNamedHomeLocation(Player player, String homeName) {
        Island island = getIsland(player);
        if (island == null) return null;
        return island.getNamedHome(homeName.toLowerCase());
    }

    public List<String> getNamedHomesList(Player player) {
        Island island = getIsland(player);
        if (island == null) {
            return new ArrayList<>();
        }
        return island.getHomeNames();
    }

    public void teleportToIsland(Player player) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Henüz bir adanız yok! Oluşturmak için /island create yazın.");
            return;
        }
        Location islandBaseLocation = island.getBaseLocation();
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

        if (teleportLocation.getChunk() != null && !teleportLocation.getChunk().isLoaded()) {
            teleportLocation.getChunk().load();
        }
        player.teleport(teleportLocation);
        player.sendMessage(ChatColor.GREEN + "Adanızın ana noktasına ışınlandınız!");
    }

    public void teleportToNamedHome(Player player, String homeName) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Önce bir ada oluşturmalısın!");
            return;
        }
        Location homeLocation = island.getNamedHome(homeName.toLowerCase());
        if (homeLocation == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adında bir ev noktan bulunmuyor. Evlerini listelemek için /island home list yaz.");
            return;
        }
        if (homeLocation.getWorld() == null || Bukkit.getWorld(homeLocation.getWorld().getName()) == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adlı evinin bulunduğu dünya yüklenemedi!");
            return;
        }
        if (homeLocation.getChunk() != null && !homeLocation.getChunk().isLoaded()) {
            homeLocation.getChunk().load();
        }
        player.teleport(homeLocation);
        player.sendMessage(ChatColor.GREEN + "'" + homeName + "' adlı evine ışınlandın!");
    }

    public StateFlag.State getIslandFlagState(UUID playerUUID, StateFlag flag) {
        ProtectedRegion region = getProtectedRegion(playerUUID);
        if (region != null) {
            return region.getFlag(flag);
        }
        plugin.getLogger().finer("getIslandFlagState: WorldGuard bölgesi bulunamadı: " + getRegionId(playerUUID));
        return null;
    }

    public boolean setIslandFlagState(UUID playerUUID, StateFlag flag, StateFlag.State newState) {
        Island island = getIslandByOwner(playerUUID); // Düzeltme: Island nesnesini al
        if (island == null || island.getWorld() == null) {
            plugin.getLogger().warning("setIslandFlagState: Bayrak ayarlanmak istenen oyuncunun adası veya ada dünyası bulunamadı: " + playerUUID);
            return false;
        }

        ProtectedRegion region = getProtectedRegion(playerUUID);
        if (region == null) {
            plugin.getLogger().warning("setIslandFlagState: Bayrak ayarlanmak istenen WorldGuard bölgesi bulunamadı: " + getRegionId(playerUUID));
            return false;
        }
        try {
            region.setFlag(flag, newState);

            // Bölgenin dünyasını Island nesnesinden al
            World bukkitWorld = island.getWorld();
            if (bukkitWorld == null) { // Ekstra güvenlik kontrolü
                plugin.getLogger().severe("setIslandFlagState: Ada dünyası (island.getWorld()) null geldi.");
                return false;
            }

            RegionManager regionManager = getWGRegionManager(bukkitWorld);
            if (regionManager == null) {
                plugin.getLogger().severe("setIslandFlagState: Bayrak ayarlanırken RegionManager alınamadı (Dünya: " + bukkitWorld.getName() + ")");
                return false;
            }
            regionManager.saveChanges();
            plugin.getLogger().info("Oyuncu " + playerUUID + " için '" + getRegionId(playerUUID) + "' bölgesinde '" + flag.getName() +
                    "' bayrağı '" + (newState != null ? newState.name() : "VARSAYILAN (kaldırıldı)") + "' olarak ayarlandı.");
            return true;
        } catch (StorageException e) {
            plugin.getLogger().severe("WorldGuard bölgesi '" + getRegionId(playerUUID) + "' kaydedilirken hata (bayrak ayarı): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("setIslandFlagState sırasında beklenmedik hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean setIslandName(Player player, String newName) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "İsmini değiştirebileceğin bir adan yok!");
            return false;
        }
        String namePattern = plugin.getConfig().getString("island.name.pattern", "^[a-zA-Z0-9_\\- ]{3,25}$");
        int minLength = plugin.getConfig().getInt("island.name.min-length", 3);
        int maxLength = plugin.getConfig().getInt("island.name.max-length", 25);

        if (newName.length() < minLength || newName.length() > maxLength || !newName.matches(namePattern)) {
            player.sendMessage(ChatColor.RED + "Ada adı " + minLength + "-" + maxLength + " karakter uzunluğunda olmalı ve sadece harf, rakam, boşluk, '_' veya '-' içerebilir.");
            return false;
        }
        island.setIslandName(newName);
        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada ismi kaydedilirken hata: " + e.getMessage());}

        player.sendMessage(ChatColor.GREEN + "Adanın yeni ismi '" + newName + "' olarak ayarlandı.");
        return true;
    }

    public boolean setIslandPublic(Player player, boolean isPublic) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Görünürlüğünü ayarlayabileceğin bir adan yok!");
            return false;
        }
        island.setPublic(isPublic);
        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada görünürlüğü kaydedilirken hata: " + e.getMessage());}

        player.sendMessage(ChatColor.GREEN + "Adanın ziyaretçi durumu " + (isPublic ? ChatColor.AQUA + "HERKESE AÇIK" : ChatColor.GOLD + "ÖZEL (Sadece Üyeler)") + ChatColor.GREEN + " olarak ayarlandı.");
        return true;
    }

    public boolean toggleIslandBoundaries(Player player) {
        Island island = getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Sınırlarını ayarlayabileceğin bir adan yok!");
            return false;
        }
        island.setBoundariesEnforced(!island.areBoundariesEnforced());
        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada sınırları kaydedilirken hata: " + e.getMessage());}
        player.sendMessage(ChatColor.GREEN + "Ada sınırları " + (island.areBoundariesEnforced() ? ChatColor.AQUA + "AKTİF" : ChatColor.GOLD + "PASİF") + ChatColor.GREEN + " olarak ayarlandı.");
        return true;
    }

    public boolean addIslandMember(Player owner, OfflinePlayer targetPlayer) {
        Island island = getIsland(owner);
        if (island == null || island.getWorld() == null) { // Düzeltme: island.getWorld() null kontrolü
            owner.sendMessage(ChatColor.RED + "Üye ekleyebileceğin bir adan veya ada dünyan yok!");
            return false;
        }
        if (owner.getUniqueId().equals(targetPlayer.getUniqueId())) {
            owner.sendMessage(ChatColor.RED + "Kendini üye olarak ekleyemezsin.");
            return false;
        }
        if (island.isMember(targetPlayer.getUniqueId())) {
            owner.sendMessage(ChatColor.RED + targetPlayer.getName() + " zaten adanın bir üyesi.");
            return false;
        }
        int maxMembers = plugin.getConfig().getInt("island.max-members", 3);
        if (island.getMembers().size() >= maxMembers) {
            owner.sendMessage(ChatColor.RED + "Maksimum üye sayısına (" + maxMembers + ") ulaştınız.");
            return false;
        }

        island.addMember(targetPlayer.getUniqueId());
        ProtectedRegion region = getProtectedRegion(owner.getUniqueId());
        if (region != null) {
            region.getMembers().addPlayer(targetPlayer.getUniqueId());
            // Bölgenin dünyasını Island nesnesinden al
            World bukkitWorld = island.getWorld();
            if (bukkitWorld != null) {
                RegionManager regionManager = getWGRegionManager(bukkitWorld);
                if (regionManager != null) {
                    try {
                        regionManager.saveChanges();
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WG üyesi eklenirken bölge kaydedilemedi: " + e.getMessage());
                    }
                }
            } else {
                plugin.getLogger().warning("WG üyesi eklenirken Bukkit dünyası (adadan) alınamadı.");
            }
        } else {
            plugin.getLogger().warning(owner.getName() + " adasına " + targetPlayer.getName() + " WG üyesi olarak eklenemedi (bölge bulunamadı).");
        }

        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada üyesi kaydedilirken hata: " + e.getMessage());}

        owner.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " başarıyla adana üye olarak eklendi.");
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            if(onlineTarget != null) {
                onlineTarget.sendMessage(ChatColor.GREEN + owner.getName() + " seni adasına üye olarak ekledi!");
            }
        }
        return true;
    }

    public boolean removeIslandMember(Player owner, OfflinePlayer targetPlayer) {
        Island island = getIsland(owner);
        if (island == null || island.getWorld() == null) { // Düzeltme: island.getWorld() null kontrolü
            owner.sendMessage(ChatColor.RED + "Üye çıkarabileceğin bir adan veya ada dünyan yok!");
            return false;
        }
        if (!island.isMember(targetPlayer.getUniqueId())) {
            owner.sendMessage(ChatColor.RED + targetPlayer.getName() + " adanın bir üyesi değil.");
            return false;
        }

        island.removeMember(targetPlayer.getUniqueId());
        ProtectedRegion region = getProtectedRegion(owner.getUniqueId());
        if (region != null) {
            region.getMembers().removePlayer(targetPlayer.getUniqueId());
            // Bölgenin dünyasını Island nesnesinden al
            World bukkitWorld = island.getWorld();
            if (bukkitWorld != null) {
                RegionManager regionManager = getWGRegionManager(bukkitWorld);
                if (regionManager != null) {
                    try {
                        regionManager.saveChanges();
                    } catch (StorageException e) {
                        plugin.getLogger().severe("WG üyesi çıkarılırken bölge kaydedilemedi: " + e.getMessage());
                    }
                }
            }
        }

        saveIslandData(island);
        try { islandsConfig.save(islandsFile); } catch (IOException e) { plugin.getLogger().severe("Ada üyesi silinirken hata: " + e.getMessage());}

        owner.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " adandan üye olarak çıkarıldı.");
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            if(onlineTarget != null) {
                onlineTarget.sendMessage(ChatColor.RED + owner.getName() + " seni adasındaki üyelikten çıkardı!");
            }
        }
        return true;
    }

    public List<OfflinePlayer> getIslandMembers(UUID ownerUUID) {
        Island island = getIslandByOwner(ownerUUID);
        if (island == null) return new ArrayList<>();
        return island.getMembers().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toList());
    }

    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) { return Bukkit.createChunkData(world); }
        @Override public Location getFixedSpawnLocation(World world, Random random) { return new Location(world, 0.0, 128, 0.0); }
    }
}