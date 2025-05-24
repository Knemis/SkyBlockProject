package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator; // loadSkyblockWorld için eklendi
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator; // EmptyWorldGenerator için eklendi

import java.io.File;
import java.io.IOException;
import java.util.ArrayList; // EmptyWorldGenerator içinde kullanılmıyor ama genel bir import
import java.util.Collections; // EKLENDİ
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random; // EmptyWorldGenerator için eklendi
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
// import java.time.Instant; // Island sınıfında kullanılıyor, burada doğrudan değil

public class IslandDataHandler {

    private final SkyBlockProject plugin;
    private final File islandsFile;
    private FileConfiguration islandsConfig;
    private final Map<UUID, Island> islandsData;
    private final String defaultIslandNamePrefix;
    private World skyblockWorld; // EKLENDİ: Ana Skyblock dünyasını tutacak alan

    public IslandDataHandler(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        this.islandsData = new HashMap<>();
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Ada");

        if (!islandsFile.getParentFile().exists()) {
            islandsFile.getParentFile().mkdirs();
        }
        if (!islandsFile.exists()) {
            try {
                islandsFile.createNewFile();
                plugin.getLogger().info("islands.yml oluşturuldu.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "islands.yml oluşturulamadı!", e);
            }
        }
        this.islandsConfig = YamlConfiguration.loadConfiguration(islandsFile);
        // Ada yüklemesi artık SkyBlockProject.onEnable() içinden loadSkyblockWorld() sonrası çağrılacak.
    }

    /**
     * Ana Skyblock dünyasını yükler veya oluşturur.
     * Bu metod, loadIslandsFromConfig'ten ÖNCE çağrılmalıdır.
     */
    public void loadSkyblockWorld() {
        String worldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        this.skyblockWorld = Bukkit.getWorld(worldName);

        if (this.skyblockWorld == null) {
            plugin.getLogger().info(worldName + " dünyası bulunamadı, oluşturuluyor...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.generator(new EmptyWorldGenerator()); // EmptyWorldGenerator'ı kullan
            this.skyblockWorld = wc.createWorld();
            if (this.skyblockWorld != null) {
                plugin.getLogger().info(worldName + " dünyası başarıyla oluşturuldu (IslandDataHandler).");
            } else {
                plugin.getLogger().severe(worldName + " dünyası oluşturulamadı (IslandDataHandler)!");
            }
        } else {
            plugin.getLogger().info(this.skyblockWorld.getName() + " dünyası başarıyla yüklendi (IslandDataHandler).");
        }
    }

    /**
     * islandsConfig nesnesinden ada verilerini islandsData map'ine yükler.
     * loadSkyblockWorld() metodu çağrıldıktan sonra çalıştırılmalıdır.
     */
    public void loadIslandsFromConfig() {
        if (this.islandsConfig == null) { // Config yüklenmemişse bir şey yapma
            this.islandsConfig = YamlConfiguration.loadConfiguration(islandsFile);
        }
        islandsData.clear();
        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands");
        if (islandsSection != null) {
            for (String uuidString : islandsSection.getKeys(false)) {
                try {
                    UUID ownerUUID = UUID.fromString(uuidString);
                    String path = "islands." + uuidString + ".";

                    String worldName = islandsConfig.getString(path + "baseLocation.world");
                    if (worldName == null) {
                        plugin.getLogger().warning("Ada yüklenirken (UUID: " + uuidString + ") dünya adı null geldi. Bu ada atlanıyor.");
                        continue;
                    }
                    World islandWorld = Bukkit.getWorld(worldName);
                    if (islandWorld == null) {
                        // Eğer skyblockWorld null değilse ve worldName skyblockWorld ile aynıysa, onu kullanmayı dene
                        if (this.skyblockWorld != null && worldName.equals(this.skyblockWorld.getName())) {
                            islandWorld = this.skyblockWorld;
                            plugin.getLogger().info("Ada (UUID: " + uuidString + ") için dünya '" + worldName + "' ana skyblock dünyası olarak ayarlandı.");
                        } else {
                            plugin.getLogger().warning("Ada yüklenirken (UUID: " + uuidString + ") dünya '" + worldName + "' bulunamadı. Bu ada atlanıyor.");
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
                    String welcomeMessage = islandsConfig.getString(path + "welcomeMessage", null);

                    Set<UUID> members = new HashSet<>();
                    List<String> memberUUIDStrings = islandsConfig.getStringList(path + "members");
                    memberUUIDStrings.forEach(memberStr -> {
                        try { members.add(UUID.fromString(memberStr)); } catch (IllegalArgumentException ignored) {
                            plugin.getLogger().warning("Geçersiz üye UUID'si (" + memberStr + ") bulundu (Ada Sahibi: " + uuidString + ").");
                        }
                    });

                    Set<UUID> bannedPlayers = new HashSet<>();
                    List<String> bannedPlayerUUIDStrings = islandsConfig.getStringList(path + "bannedPlayers");
                    bannedPlayerUUIDStrings.forEach(bannedStr -> {
                        try { bannedPlayers.add(UUID.fromString(bannedStr)); } catch (IllegalArgumentException ignored) {
                            plugin.getLogger().warning("Geçersiz yasaklı oyuncu UUID'si (" + bannedStr + ") bulundu (Ada Sahibi: " + uuidString + ").");
                        }
                    });

                    Map<String, Location> namedHomes = new HashMap<>();
                    ConfigurationSection homesCfgSection = islandsConfig.getConfigurationSection(path + "homes");
                    if (homesCfgSection != null) {
                        for (String homeNameKey : homesCfgSection.getKeys(false)) {
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
                                plugin.getLogger().warning("'" + homeNameKey + "' adlı ev (Sahip: " + uuidString + ") için dünya (" + homeWorldName + ") ada dünyasıyla ("+ (islandWorld != null ? islandWorld.getName() : "null") +") eşleşmiyor veya bulunamadı.");
                            }
                        }
                    }

                    Island island = new Island(
                            ownerUUID, islandName, baseLocation, creationTimestamp,
                            isPublic, boundariesEnforced, members, bannedPlayers,
                            namedHomes, currentBiome, welcomeMessage
                    );
                    islandsData.put(ownerUUID, island);

                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Ada yüklenirken beklenmedik bir hata oluştu (UUID Dizesi: " + uuidString + ")", e);
                }
            }
        }
        plugin.getLogger().info(islandsData.size() + " ada verisi config'den başarıyla yüklendi (IslandDataHandler).");
    }

    public void saveAllIslandsToDisk() {
        if (islandsConfig == null) {
            islandsConfig = YamlConfiguration.loadConfiguration(islandsFile); // Emin olmak için yükle
        }
        islandsConfig.set("islands", null);

        if (islandsData.isEmpty()) {
            plugin.getLogger().info("Kaydedilecek ada verisi bulunmuyor (IslandDataHandler).");
        } else {
            for (Island island : islandsData.values()) {
                writeIslandToConfig(island);
            }
            plugin.getLogger().info(islandsData.size() + " ada verisi config nesnesine yazıldı (IslandDataHandler).");
        }

        try {
            islandsConfig.save(islandsFile);
            plugin.getLogger().info("Tüm ada verileri islands.yml dosyasına başarıyla kaydedildi (IslandDataHandler).");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Tüm ada verileri islands.yml dosyasına kaydedilemedi!", e);
        }
    }

    private void writeIslandToConfig(Island island) {
        if (island == null || islandsConfig == null) return;

        String uuidString = island.getOwnerUUID().toString();
        String path = "islands." + uuidString + ".";

        islandsConfig.set(path + "islandName", island.getIslandName());
        islandsConfig.set(path + "creationDate", island.getCreationTimestamp());
        islandsConfig.set(path + "isPublic", island.isPublic());
        islandsConfig.set(path + "boundariesEnforced", island.areBoundariesEnforced());
        islandsConfig.set(path + "currentBiome", island.getCurrentBiome());
        islandsConfig.set(path + "welcomeMessage", island.getWelcomeMessage());

        Location baseLoc = island.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName());
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX());
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY());
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ());
        }

        islandsConfig.set(path + "members", island.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
        islandsConfig.set(path + "bannedPlayers", island.getBannedPlayers().stream().map(UUID::toString).collect(Collectors.toList()));

        islandsConfig.set(path + "homes", null);
        if (island.getNamedHomes() != null && !island.getNamedHomes().isEmpty()) {
            for (Map.Entry<String, Location> homeEntry : island.getNamedHomes().entrySet()) {
                String homeName = homeEntry.getKey();
                Location homeLoc = homeEntry.getValue();
                String homePath = path + "homes." + homeName + ".";
                if (homeLoc != null && homeLoc.getWorld() != null) {
                    islandsConfig.set(homePath + "world", homeLoc.getWorld().getName());
                    // ... (diğer home koordinatları)
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
        if (island == null) return;
        islandsData.put(island.getOwnerUUID(), island);
        writeIslandToConfig(island);
        // saveChangesToDisk(); // Her değişiklikte hemen diske yazmak yerine toplu kaydetme daha performanslı olabilir
    }

    public void removeIslandData(UUID ownerUUID) {
        islandsData.remove(ownerUUID);
        if (islandsConfig != null) {
            islandsConfig.set("islands." + ownerUUID.toString(), null);
            // saveChangesToDisk(); // Toplu kaydetme daha iyi olabilir
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
            plugin.getLogger().info((offlinePlayer.getName() != null ? offlinePlayer.getName() : ownerUUID.toString()) + " adlı oyuncunun ada verisi bellekten ve config'den silindi.");
        }
    }

    public void saveChangesToDisk() {
        if (islandsConfig == null || islandsFile == null) {
            plugin.getLogger().severe("islandsConfig veya islandsFile null, değişiklikler diske yazılamadı!");
            return;
        }
        try {
            islandsConfig.save(islandsFile);
            plugin.getLogger().info("Ada verilerindeki değişiklikler islands.yml dosyasına kaydedildi (IslandDataHandler).");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "islands.yml dosyasına kaydedilirken hata oluştu!", e);
        }
    }

    public Island getIslandByOwner(UUID ownerUUID) {
        return islandsData.get(ownerUUID);
    }

    public boolean playerHasIsland(UUID playerUUID) {
        return islandsData.containsKey(playerUUID);
    }

    public Map<UUID, Island> getAllIslandsDataView() {
        return Collections.unmodifiableMap(islandsData); // Düzeltildi: Collections importu eklendi
    }

    public World getSkyblockWorld() {
        if (this.skyblockWorld == null) {
            plugin.getLogger().warning("Skyblock dünyası (skyblockWorld) IslandDataHandler içinde null! Muhtemelen loadSkyblockWorld() çağrılmadı.");
            // Acil durum olarak tekrar yüklemeyi deneyebilir veya null dönebilir.
            // Şimdilik null dönmesi, çağıran yerin kontrol etmesini gerektirir.
        }
        return this.skyblockWorld;
    }

    // EmptyWorldGenerator'ı IslandManager'dan buraya taşıdık (veya SkyBlockProject'e de taşınabilir)
    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return Bukkit.createChunkData(world);
        }

        // Oyuncuların ilk spawn olacağı yer (opsiyonel, dünya oluşturulurken kullanılır)
        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.5, 128, 0.5); // Veya config'den alınabilir
        }
    }
}
