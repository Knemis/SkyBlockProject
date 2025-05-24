package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.math.BlockVector3; // getIslandAt için eklendi
import com.sk89q.worldguard.protection.managers.RegionManager; // getIslandAt için eklendi
import com.sk89q.worldguard.protection.regions.ProtectedRegion; // getIslandAt için eklendi
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
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

    public IslandDataHandler(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml"); // [cite: 660]
        this.islandsData = new HashMap<>(); // [cite: 660]
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada"); // [cite: 660]
        if (!islandsFile.getParentFile().exists()) { // [cite: 661]
            islandsFile.getParentFile().mkdirs(); // [cite: 661]
        }
        if (!islandsFile.exists()) { // [cite: 662]
            try {
                islandsFile.createNewFile(); // [cite: 662]
                plugin.getLogger().info("islands.yml oluşturuldu."); // [cite: 663]
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "islands.yml oluşturulamadı!", e); // [cite: 663]
            }
        }
        this.islandsConfig = YamlConfiguration.loadConfiguration(islandsFile); // [cite: 664]
    }

    public void loadSkyblockWorld() {
        String worldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world"); // [cite: 666]
        this.skyblockWorld = Bukkit.getWorld(worldName); // [cite: 667]

        if (this.skyblockWorld == null) { // [cite: 667]
            plugin.getLogger().info(worldName + " dünyası bulunamadı, oluşturuluyor..."); // [cite: 667]
            WorldCreator wc = new WorldCreator(worldName); // [cite: 668]
            wc.generator(new EmptyWorldGenerator()); // [cite: 668]
            this.skyblockWorld = wc.createWorld(); // [cite: 668]
            if (this.skyblockWorld != null) { // [cite: 669]
                plugin.getLogger().info(worldName + " dünyası başarıyla oluşturuldu (IslandDataHandler)."); // [cite: 669]
            } else {
                plugin.getLogger().severe(worldName + " dünyası oluşturulamadı (IslandDataHandler)!"); // [cite: 670]
            }
        } else {
            plugin.getLogger().info(this.skyblockWorld.getName() + " dünyası başarıyla yüklendi (IslandDataHandler)."); // [cite: 671]
        }
    }

    public void loadIslandsFromConfig() {
        if (this.islandsConfig == null) { // [cite: 673]
            this.islandsConfig = YamlConfiguration.loadConfiguration(islandsFile); // [cite: 673]
        }
        islandsData.clear(); // [cite: 674]
        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands"); // [cite: 674]
        if (islandsSection != null) { // [cite: 675]
            for (String uuidString : islandsSection.getKeys(false)) { // [cite: 675]
                try {
                    UUID ownerUUID = UUID.fromString(uuidString); // [cite: 675]
                    String path = "islands." + uuidString + "."; // [cite: 676]

                    String worldName = islandsConfig.getString(path + "baseLocation.world"); // [cite: 676]
                    if (worldName == null) { // [cite: 677]
                        plugin.getLogger().warning("Ada yüklenirken (UUID: " + uuidString + ") dünya adı null geldi. Bu ada atlanıyor."); // [cite: 677]
                        continue; // [cite: 678]
                    }
                    World islandWorld = Bukkit.getWorld(worldName); // [cite: 678]
                    if (islandWorld == null) { // [cite: 679]
                        if (this.skyblockWorld != null && worldName.equals(this.skyblockWorld.getName())) { // [cite: 679]
                            islandWorld = this.skyblockWorld; // [cite: 680]
                            plugin.getLogger().info("Ada (UUID: " + uuidString + ") için dünya '" + worldName + "' ana skyblock dünyası olarak ayarlandı."); // [cite: 680]
                        } else {
                            plugin.getLogger().warning("Ada yüklenirken (UUID: " + uuidString + ") dünya '" + worldName + "' bulunamadı. Bu ada atlanıyor."); // [cite: 681]
                            continue; // [cite: 682]
                        }
                    }

                    double x = islandsConfig.getDouble(path + "baseLocation.x"); // [cite: 682]
                    double y = islandsConfig.getDouble(path + "baseLocation.y"); // [cite: 683]
                    double z = islandsConfig.getDouble(path + "baseLocation.z"); // [cite: 683]
                    Location baseLocation = new Location(islandWorld, x, y, z); // [cite: 683]
                    String islandName = islandsConfig.getString(path + "islandName", defaultIslandNamePrefix + "-" + Bukkit.getOfflinePlayer(ownerUUID).getName()); // [cite: 684]
                    long creationTimestamp = islandsConfig.getLong(path + "creationDate", System.currentTimeMillis()); // [cite: 684]
                    boolean isPublic = islandsConfig.getBoolean(path + "isPublic", false); // [cite: 685]
                    boolean boundariesEnforced = islandsConfig.getBoolean(path + "boundariesEnforced", true); // [cite: 685]
                    String currentBiome = islandsConfig.getString(path + "currentBiome", null); // [cite: 686]
                    String welcomeMessage = islandsConfig.getString(path + "welcomeMessage", null); // [cite: 686]

                    Set<UUID> members = new HashSet<>(); // [cite: 686]
                    List<String> memberUUIDStrings = islandsConfig.getStringList(path + "members"); // [cite: 687]
                    memberUUIDStrings.forEach(memberStr -> { // [cite: 687]
                        try { members.add(UUID.fromString(memberStr)); } catch (IllegalArgumentException ignored) { // [cite: 687]
                            plugin.getLogger().warning("Geçersiz üye UUID'si (" + memberStr + ") bulundu (Ada Sahibi: " + uuidString + ")."); // [cite: 687]
                        }
                    });
                    Set<UUID> bannedPlayers = new HashSet<>(); // [cite: 689]
                    List<String> bannedPlayerUUIDStrings = islandsConfig.getStringList(path + "bannedPlayers"); // [cite: 689]
                    bannedPlayerUUIDStrings.forEach(bannedStr -> { // [cite: 690]
                        try { bannedPlayers.add(UUID.fromString(bannedStr)); } catch (IllegalArgumentException ignored) { // [cite: 690]
                            plugin.getLogger().warning("Geçersiz yasaklı oyuncu UUID'si (" + bannedStr + ") bulundu (Ada Sahibi: " + uuidString + ")."); // [cite: 690]
                        }
                    });
                    Map<String, Location> namedHomes = new HashMap<>(); // [cite: 692]
                    ConfigurationSection homesCfgSection = islandsConfig.getConfigurationSection(path + "homes"); // [cite: 692]
                    if (homesCfgSection != null) { // [cite: 693]
                        for (String homeNameKey : homesCfgSection.getKeys(false)) { // [cite: 693]
                            String homePath = path + "homes." + homeNameKey + "."; // [cite: 694]
                            String homeWorldName = islandsConfig.getString(homePath + "world"); // [cite: 694]
                            World homeWorld = Bukkit.getWorld(homeWorldName); // [cite: 694]
                            if (homeWorld != null && homeWorld.equals(islandWorld)) { // [cite: 695]
                                double hx = islandsConfig.getDouble(homePath + "x"); // [cite: 695]
                                double hy = islandsConfig.getDouble(homePath + "y"); // [cite: 696]
                                double hz = islandsConfig.getDouble(homePath + "z"); // [cite: 696]
                                float hyaw = (float) islandsConfig.getDouble(homePath + "yaw"); // [cite: 696]
                                float hpitch = (float) islandsConfig.getDouble(homePath + "pitch"); // [cite: 697]
                                namedHomes.put(homeNameKey.toLowerCase(), new Location(homeWorld, hx, hy, hz, hyaw, hpitch)); // [cite: 697]
                            } else {
                                plugin.getLogger().warning("'" + homeNameKey + "' adlı ev (Sahip: " + uuidString + ") için dünya (" + homeWorldName + ") ada dünyasıyla ("+ (islandWorld != null ? islandWorld.getName() : "null") +") eşleşmiyor veya bulunamadı."); // [cite: 698]
                            }
                        }
                    }

                    Island island = new Island( // [cite: 699]
                            ownerUUID, islandName, baseLocation, creationTimestamp, // [cite: 699]
                            isPublic, boundariesEnforced, members, bannedPlayers, // [cite: 700]
                            namedHomes, currentBiome, welcomeMessage // [cite: 701]
                    );
                    islandsData.put(ownerUUID, island); // [cite: 701]

                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Ada yüklenirken beklenmedik bir hata oluştu (UUID Dizesi: " + uuidString + ")", e); // [cite: 701]
                }
            }
        }
        plugin.getLogger().info(islandsData.size() + " ada verisi config'den başarıyla yüklendi (IslandDataHandler)."); // [cite: 702]
    }

    public void saveAllIslandsToDisk() {
        if (islandsConfig == null) { // [cite: 703]
            islandsConfig = YamlConfiguration.loadConfiguration(islandsFile); // [cite: 703]
        }
        islandsConfig.set("islands", null); // [cite: 704]
        if (islandsData.isEmpty()) { // [cite: 705]
            plugin.getLogger().info("Kaydedilecek ada verisi bulunmuyor (IslandDataHandler)."); // [cite: 705]
        } else {
            for (Island island : islandsData.values()) { // [cite: 706]
                writeIslandToConfig(island); // [cite: 706]
            }
            plugin.getLogger().info(islandsData.size() + " ada verisi config nesnesine yazıldı (IslandDataHandler)."); // [cite: 707]
        }

        try {
            islandsConfig.save(islandsFile); // [cite: 708]
            plugin.getLogger().info("Tüm ada verileri islands.yml dosyasına başarıyla kaydedildi (IslandDataHandler)."); // [cite: 709]
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Tüm ada verileri islands.yml dosyasına kaydedilemedi!", e); // [cite: 709]
        }
    }

    private void writeIslandToConfig(Island island) {
        if (island == null || islandsConfig == null) return; // [cite: 710]
        String uuidString = island.getOwnerUUID().toString(); // [cite: 711]
        String path = "islands." + uuidString + "."; // [cite: 711]

        islandsConfig.set(path + "islandName", island.getIslandName()); // [cite: 711]
        islandsConfig.set(path + "creationDate", island.getCreationTimestamp()); // [cite: 711]
        islandsConfig.set(path + "isPublic", island.isPublic()); // [cite: 712]
        islandsConfig.set(path + "boundariesEnforced", island.areBoundariesEnforced()); // [cite: 712]
        islandsConfig.set(path + "currentBiome", island.getCurrentBiome()); // [cite: 712]
        islandsConfig.set(path + "welcomeMessage", island.getWelcomeMessage()); // [cite: 712]

        Location baseLoc = island.getBaseLocation(); // [cite: 712]
        if (baseLoc != null && baseLoc.getWorld() != null) { // [cite: 713]
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName()); // [cite: 713]
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX()); // [cite: 714]
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY()); // [cite: 714]
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ()); // [cite: 714]
        }

        islandsConfig.set(path + "members", island.getMembers().stream().map(UUID::toString).collect(Collectors.toList())); // [cite: 715]
        islandsConfig.set(path + "bannedPlayers", island.getBannedPlayers().stream().map(UUID::toString).collect(Collectors.toList())); // [cite: 715]

        islandsConfig.set(path + "homes", null); // [cite: 715]
        if (island.getNamedHomes() != null && !island.getNamedHomes().isEmpty()) { // [cite: 716]
            for (Map.Entry<String, Location> homeEntry : island.getNamedHomes().entrySet()) { // [cite: 716]
                String homeName = homeEntry.getKey(); // [cite: 716]
                Location homeLoc = homeEntry.getValue(); // [cite: 717]
                String homePath = path + "homes." + homeName + "."; // [cite: 717]
                if (homeLoc != null && homeLoc.getWorld() != null) { // [cite: 718]
                    islandsConfig.set(homePath + "world", homeLoc.getWorld().getName()); // [cite: 718]
                    islandsConfig.set(homePath + "x", homeLoc.getX()); // [cite: 719]
                    islandsConfig.set(homePath + "y", homeLoc.getY()); // [cite: 720]
                    islandsConfig.set(homePath + "z", homeLoc.getZ()); // [cite: 720]
                    islandsConfig.set(homePath + "yaw", homeLoc.getYaw()); // [cite: 720]
                    islandsConfig.set(homePath + "pitch", homeLoc.getPitch()); // [cite: 720]
                }
            }
        }
    }

    public void addOrUpdateIslandData(Island island) {
        if (island == null) return; // [cite: 721]
        islandsData.put(island.getOwnerUUID(), island); // [cite: 722]
        writeIslandToConfig(island); // [cite: 722]
    }

    public void removeIslandData(UUID ownerUUID) {
        islandsData.remove(ownerUUID); // [cite: 722]
        if (islandsConfig != null) { // [cite: 723]
            islandsConfig.set("islands." + ownerUUID.toString(), null); // [cite: 723]
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID); // [cite: 724]
            plugin.getLogger().info((offlinePlayer.getName() != null ? offlinePlayer.getName() : ownerUUID.toString()) + " adlı oyuncunun ada verisi bellekten ve config'den silindi."); // [cite: 725]
        }
    }

    public void saveChangesToDisk() {
        if (islandsConfig == null || islandsFile == null) { // [cite: 726]
            plugin.getLogger().severe("islandsConfig veya islandsFile null, değişiklikler diske yazılamadı!"); // [cite: 726]
            return; // [cite: 727]
        }
        try {
            islandsConfig.save(islandsFile); // [cite: 727]
            plugin.getLogger().info("Ada verilerindeki değişiklikler islands.yml dosyasına kaydedildi (IslandDataHandler)."); // [cite: 728]
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "islands.yml dosyasına kaydedilirken hata oluştu!", e); // [cite: 728]
        }
    }

    public Island getIslandByOwner(UUID ownerUUID) {
        return islandsData.get(ownerUUID); // [cite: 729]
    }

    public boolean playerHasIsland(UUID playerUUID) {
        return islandsData.containsKey(playerUUID); // [cite: 730]
    }

    public Map<UUID, Island> getAllIslandsDataView() {
        return Collections.unmodifiableMap(islandsData); // [cite: 731]
    }

    public World getSkyblockWorld() {
        if (this.skyblockWorld == null) { // [cite: 732]
            plugin.getLogger().warning("Skyblock dünyası (skyblockWorld) IslandDataHandler içinde null! Muhtemelen loadSkyblockWorld() çağrılmadı."); // [cite: 732]
        }
        return this.skyblockWorld; // [cite: 734]
    }

    // --- YENİ EKLENEN METOD ---
    private String getRegionIdString(UUID ownerUUID) {
        // Bu metod, IslandLifecycleManager'daki getRegionId ile aynı ID'yi üretmelidir.
        // Ya da IslandLifecycleManager bu bilgiyi sağlamalıdır.
        // Şimdilik direkt oluşturuyoruz.
        return "skyblock_island_" + ownerUUID.toString();
    }

    /**
     * Verilen konumda bulunan adayı döndürür.
     * WorldGuard bölgelerini kontrol eder.
     * @param location Kontrol edilecek konum.
     * @return Konumda bir ada varsa Island nesnesi, yoksa null.
     */
    public Island getIslandAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        // Sadece skyblock dünyasındaki adaları kontrol et
        if (this.skyblockWorld == null || !location.getWorld().equals(this.skyblockWorld)) {
            return null;
        }

        for (Island island : islandsData.values()) {
            // Adanın dünyası null olmamalı ve aranan konumun dünyasıyla aynı olmalı.
            // (skyblockWorld kontrolü yukarıda yapıldığı için bu ek kontrol gereksiz olabilir ama güvende olmak iyidir)
            if (island.getWorld() == null || !island.getWorld().equals(location.getWorld())) {
                continue;
            }

            RegionManager regionManager = plugin.getRegionManager(island.getWorld());
            if (regionManager == null) {
                plugin.getLogger().warning("getIslandAt: Ada " + island.getOwnerUUID() + " için RegionManager alınamadı (Dünya: " + island.getWorld().getName() + ")");
                continue;
            }

            String regionId = getRegionIdString(island.getOwnerUUID());
            ProtectedRegion region = regionManager.getRegion(regionId);

            if (region != null) {
                // WorldGuard koordinatları için BlockVector3 kullan
                if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                    return island;
                }
            }
        }
        return null; // Belirtilen konumda ada bulunamadı
    }
    // --- YENİ EKLENEN METOD SONU ---


    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return Bukkit.createChunkData(world); // [cite: 735]
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.5, 128, 0.5); // [cite: 737]
        }
    }
}