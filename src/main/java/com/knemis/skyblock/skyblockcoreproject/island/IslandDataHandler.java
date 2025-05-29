package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
    private final Map<UUID, Island> islandsData; // Aktif ada verilerini tutar
    private final String defaultIslandNamePrefix;
    private World skyblockWorld;
    private final int defaultInitialMaxHomes;
    private boolean dataChangedSinceLastSave = false;

    public IslandDataHandler(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        this.islandsData = new HashMap<>();
        this.defaultIslandNamePrefix = plugin.getConfig().getString("island.default-name-prefix", "Island");
        this.defaultInitialMaxHomes = plugin.getConfig().getInt("island.max-named-homes", 3);

        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().severe("Plugin data folder could not be created: " + plugin.getDataFolder().getPath());
            }
        }
        this.islandsConfig = new YamlConfiguration();
        loadIslandsFile(); // Dosyayı yükle veya oluştur
    }

    private void loadIslandsFile() {
        if (!islandsFile.exists()) {
            try {
                // Dosya yoksa, boş bir 'islands' bölümüyle oluştur
                islandsConfig.createSection("islands");
                islandsConfig.save(islandsFile);
                plugin.getLogger().info(islandsFile.getName() + " created and saved with default structure.");
                dataChangedSinceLastSave = false; // Yeni dosya, kaydedilecek değişiklik yok
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " could not be created or saved!", e);
            }
        } else {
            // Dosya varsa yükle
            try {
                islandsConfig.load(islandsFile);
                dataChangedSinceLastSave = false; // Yüklemeden sonra henüz değişiklik yok
                plugin.getLogger().info(islandsFile.getName() + " successfully loaded.");
            } catch (FileNotFoundException e) { // Bu genellikle exists() kontrolüyle önlenir ama yine de...
                plugin.getLogger().log(Level.SEVERE, islandsFile.getName() + " not found during load attempt!", e);
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
            plugin.getLogger().info(worldName + " world not found, attempting to create...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.generator(new EmptyWorldGenerator()); // Özel boş dünya jeneratörü
            try {
                this.skyblockWorld = wc.createWorld();
                if (this.skyblockWorld != null) {
                    plugin.getLogger().info(worldName + " world successfully created (IslandDataHandler).");
                } else {
                    plugin.getLogger().severe(worldName + " world could not be created! Plugin may not function correctly.");
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
            plugin.getLogger().severe("Island data cannot be loaded: Skyblock world is not loaded. Call loadSkyblockWorld() first.");
            return;
        }
        if (this.islandsConfig == null) {
            plugin.getLogger().warning("Islands config is null. Attempting to reload islands.yml...");
            loadIslandsFile(); // Yeniden yüklemeyi dene
            if (this.islandsConfig == null) {
                plugin.getLogger().severe("Islands config could not be reloaded. Island data loading aborted.");
                return;
            }
        }

        islandsData.clear(); // Önceki verileri temizle
        ConfigurationSection islandsSection = islandsConfig.getConfigurationSection("islands");

        if (islandsSection == null) {
            plugin.getLogger().info("'islands' section not found in config or is empty. No islands loaded. Creating section.");
            islandsConfig.createSection("islands"); // Eksikse bölümü oluştur
            dataChangedSinceLastSave = true; // Config değişti, kaydedilmeli
            return;
        }

        int successfullyLoaded = 0;
        for (String uuidString : islandsSection.getKeys(false)) {
            UUID ownerUUID;
            try {
                ownerUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid owner UUID format '" + uuidString + "' in config. Skipping this island.");
                continue;
            }

            String path = "islands." + uuidString + "."; // Kısaltılmış yol

            // Dünya adını al ve kontrol et
            String worldName = islandsConfig.getString(path + "baseLocation.world");
            if (worldName == null || worldName.isEmpty()) {
                plugin.getLogger().warning("World name for island (Owner: " + uuidString + ") is missing. Skipping.");
                continue;
            }

            World islandWorld = Bukkit.getWorld(worldName);
            if (islandWorld == null) {
                // Eğer ana skyblock dünyası ise onu kullan
                if (this.skyblockWorld != null && worldName.equals(this.skyblockWorld.getName())) {
                    islandWorld = this.skyblockWorld;
                } else {
                    plugin.getLogger().warning("World '" + worldName + "' for island (Owner: " + uuidString + ") not found. Skipping.");
                    continue;
                }
            }

            Location baseLocation = new Location(islandWorld,
                    islandsConfig.getDouble(path + "baseLocation.x"),
                    islandsConfig.getDouble(path + "baseLocation.y"),
                    islandsConfig.getDouble(path + "baseLocation.z"));

            String islandName = islandsConfig.getString(path + "islandName", defaultIslandNamePrefix + "-" + ownerUUID.toString().substring(0,8));
            long creationTimestamp = islandsConfig.getLong(path + "creationDate", System.currentTimeMillis());
            boolean isPublic = islandsConfig.getBoolean(path + "isPublic", false);
            boolean boundariesEnforced = islandsConfig.getBoolean(path + "boundariesEnforced", true);
            String currentBiome = islandsConfig.getString(path + "currentBiome"); // null olabilir
            String welcomeMessage = islandsConfig.getString(path + "welcomeMessage", "");
            int maxHomesLimit = islandsConfig.getInt(path + "maxHomesLimit", defaultInitialMaxHomes);
            double islandWorth = islandsConfig.getDouble(path + "islandWorth", 0.0);
            int islandLevel = islandsConfig.getInt(path + "islandLevel", 1);

            // --- YENİ: regionId YÜKLEME ---
            String regionId = islandsConfig.getString(path + "regionId");
            // regionId null gelirse, Island constructor'ı varsayılan bir ID ("skyblock_island_" + ownerUUID) üretecektir.

            Set<UUID> members = new HashSet<>();
            islandsConfig.getStringList(path + "members").forEach(memberStr -> {
                try {
                    members.add(UUID.fromString(memberStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid member UUID for island (Owner: " + uuidString + "): " + memberStr);
                }
            });

            Map<String, Location> namedHomes = new HashMap<>();
            ConfigurationSection homesSection = islandsConfig.getConfigurationSection(path + "homes");
            if (homesSection != null) {
                for (String homeNameKey : homesSection.getKeys(false)) {
                    String homePath = path + "homes." + homeNameKey + "."; // Doğru homePath kullanımı
                    String homeWorldName = homesSection.getString(homePath + "world", worldName); // Ev dünyası yoksa ana ada dünyasını kullan
                    World homeWorld = Bukkit.getWorld(homeWorldName);
                    if (homeWorld == null) { // Eğer hala null ise ana skyblock dünyasını dene
                        if (this.skyblockWorld != null && homeWorldName.equals(this.skyblockWorld.getName())) {
                            homeWorld = this.skyblockWorld;
                        } else {
                            plugin.getLogger().warning("World for home '" + homeNameKey + "' (Owner: " + uuidString + ", World: "+homeWorldName+") not found. Skipping home.");
                            continue;
                        }
                    }
                    namedHomes.put(homeNameKey.toLowerCase(), new Location(homeWorld,
                            homesSection.getDouble(homePath + "x"),
                            homesSection.getDouble(homePath + "y"),
                            homesSection.getDouble(homePath + "z"),
                            (float) homesSection.getDouble(homePath + "yaw", 0.0),
                            (float) homesSection.getDouble(homePath + "pitch", 0.0)));
                }
            }
            // --- Island Constructor ÇAĞRISI GÜNCELLENDİ (regionId eklendi) ---
            Island island = new Island(ownerUUID, islandName, baseLocation, creationTimestamp,
                    isPublic, boundariesEnforced, members, namedHomes,
                    currentBiome, welcomeMessage, maxHomesLimit, islandWorth, islandLevel, regionId);

            islandsData.put(ownerUUID, island);
            successfullyLoaded++;
        }
        if (successfullyLoaded > 0) {
            plugin.getLogger().info(successfullyLoaded + " island data successfully loaded from config (IslandDataHandler).");
        } else if (islandsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().info("No islands found in config to load.");
        } else {
            plugin.getLogger().warning("No islands were successfully loaded, though some entries existed. Check previous warnings.");
        }
    }

    // Verilen bir Island nesnesini config nesnesine yazar (diske kaydetmez)
    private void writeIslandToConfigInternal(Island island) {
        if (island == null) {
            plugin.getLogger().warning("Attempted to write a null island object to config.");
            return;
        }
        if (islandsConfig == null) {
            plugin.getLogger().severe("Islands config is null. Island cannot be written: " + island.getOwnerUUID());
            return;
        }

        String uuidString = island.getOwnerUUID().toString();
        String path = "islands." + uuidString + ".";

        islandsConfig.set(path + "islandName", island.getIslandName());
        islandsConfig.set(path + "creationDate", island.getCreationTimestamp());
        islandsConfig.set(path + "isPublic", island.isPublic());
        islandsConfig.set(path + "boundariesEnforced", island.areBoundariesEnforced());
        islandsConfig.set(path + "currentBiome", island.getCurrentBiome()); // null olabilir
        islandsConfig.set(path + "welcomeMessage", island.getWelcomeMessage());
        islandsConfig.set(path + "maxHomesLimit", island.getMaxHomesLimit());
        islandsConfig.set(path + "islandWorth", island.getIslandWorth());
        islandsConfig.set(path + "islandLevel", island.getIslandLevel());

        // --- YENİ: regionId KAYDETME ---
        islandsConfig.set(path + "regionId", island.getRegionId()); // regionId'yi kaydet

        Location baseLoc = island.getBaseLocation(); // Island.getBaseLocation() zaten klon döndürüyor
        if (baseLoc != null && baseLoc.getWorld() != null) {
            islandsConfig.set(path + "baseLocation.world", baseLoc.getWorld().getName());
            islandsConfig.set(path + "baseLocation.x", baseLoc.getBlockX()); // getBlockX int döndürür, bu genellikle daha iyidir
            islandsConfig.set(path + "baseLocation.y", baseLoc.getBlockY());
            islandsConfig.set(path + "baseLocation.z", baseLoc.getBlockZ());
        } else {
            plugin.getLogger().warning("Base location or world for island (Owner: " + uuidString + ") is missing. Location not saved.");
        }

        islandsConfig.set(path + "members", island.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));

        islandsConfig.set(path + "homes", null); // Önceki evleri temizle
        Map<String, Location> namedHomes = island.getNamedHomes(); // Island.getNamedHomes() zaten klonlanmış bir harita döndürüyor
        if (namedHomes != null && !namedHomes.isEmpty()) {
            for (Map.Entry<String, Location> homeEntry : namedHomes.entrySet()) {
                String homeName = homeEntry.getKey(); // Zaten küçük harf olmalı (Island sınıfında ayarlanıyor)
                Location homeLoc = homeEntry.getValue(); // Zaten klonlanmış olmalı
                String homePath = path + "homes." + homeName + ".";
                if (homeLoc != null && homeLoc.getWorld() != null) {
                    islandsConfig.set(homePath + "world", homeLoc.getWorld().getName());
                    islandsConfig.set(homePath + "x", homeLoc.getX());
                    islandsConfig.set(homePath + "y", homeLoc.getY());
                    islandsConfig.set(homePath + "z", homeLoc.getZ());
                    islandsConfig.set(homePath + "yaw", homeLoc.getYaw());
                    islandsConfig.set(homePath + "pitch", homeLoc.getPitch());
                } else {
                    plugin.getLogger().warning("Named home '"+homeName+"' for island (Owner: " + uuidString + ") has null location or world. Home not saved.");
                }
            }
        }
        dataChangedSinceLastSave = true; // Config nesnesi değişti
    }

    // Bir adayı hem hafızaya hem de config nesnesine ekler/günceller
    public void addOrUpdateIslandData(Island island) {
        if (island == null || island.getOwnerUUID() == null) {
            plugin.getLogger().warning("Cannot add/update null island or island with null owner UUID.");
            return;
        }
        islandsData.put(island.getOwnerUUID(), island); // Hafızaya ekle/güncelle
        writeIslandToConfigInternal(island); // Config nesnesine yaz (diske değil)
        // dataChangedSinceLastSave zaten writeIslandToConfigInternal içinde true yapıldı.
        plugin.getLogger().fine("Island data for " + island.getOwnerUUID() + " updated in memory and prepared for saving.");
    }

    // Bir adayı hem hafızadan hem de config nesnesinden siler
    public void removeIslandData(UUID ownerUUID) {
        if (ownerUUID == null) return;
        Island removedIsland = islandsData.remove(ownerUUID); // Hafızadan sil

        if (islandsConfig != null) { // Config nesnesinden sil
            islandsConfig.set("islands." + ownerUUID.toString(), null);
            dataChangedSinceLastSave = true;
        }

        String playerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
        if (playerName == null) playerName = ownerUUID.toString();

        if (removedIsland != null) {
            plugin.getLogger().info("Island data for player " + playerName + " deleted from memory and marked for deletion from config.");
        } else {
            plugin.getLogger().warning("Attempted to delete island data for " + playerName + ", but it was not found in memory. Marked for deletion from config anyway.");
        }
    }

    // Config nesnesindeki değişiklikleri diske kaydeder
    public void saveAllIslandsToDisk() {
        plugin.getLogger().info("Attempting to save all island data to disk...");
        islandsConfig.set("islands", null); // Önce tüm "islands" bölümünü temizle
        if (islandsData.isEmpty()) {
            plugin.getLogger().info("No active island data in memory to save.");
        } else {
            for (Island island : islandsData.values()) {
                writeIslandToConfigInternal(island); // Her adayı config nesnesine yaz
            }
            plugin.getLogger().info(islandsData.size() + " island data objects written to config structure.");
        }
        // dataChangedSinceLastSave zaten writeIslandToConfigInternal içinde true yapıldı.
        saveChangesToDisk(); // Asıl diske yazma işlemi
    }


    public void saveChangesToDisk() {
        if (islandsConfig == null || islandsFile == null) {
            plugin.getLogger().severe("Islands config or islandsFile is null! Changes cannot be written to disk.");
            return;
        }
        if (dataChangedSinceLastSave) {
            try {
                islandsConfig.save(islandsFile);
                dataChangedSinceLastSave = false; // Değişiklikler kaydedildi
                plugin.getLogger().info("Changes to island data successfully saved to " + islandsFile.getName());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "I/O error occurred while saving island data to " + islandsFile.getName(), e);
            }
        } else {
            plugin.getLogger().fine("No changes detected in island data, disk write operation skipped.");
        }
    }

    public Island getIslandByOwner(UUID ownerUUID) {
        return islandsData.get(ownerUUID);
    }

    public boolean playerHasIsland(UUID playerUUID) {
        return islandsData.containsKey(playerUUID);
    }

    public Map<UUID, Island> getAllIslandsDataView() {
        return Collections.unmodifiableMap(new HashMap<>(islandsData)); // Değiştirilemez kopya
    }

    public World getSkyblockWorld() {
        if (this.skyblockWorld == null) {
            plugin.getLogger().warning("Skyblock world (skyblockWorld) is null in IslandDataHandler. Attempting to reload. This may indicate an issue with plugin load order or world creation.");
            loadSkyblockWorld(); // Yeniden yüklemeyi dene
            if (this.skyblockWorld == null) {
                plugin.getLogger().severe("Skyblock world could not be loaded even after re-attempt. Critical features may fail.");
            }
        }
        return this.skyblockWorld;
    }

    // Bu metot artık Island.getRegionId() ile aynı formatı kullanıyor ve Island nesnesi üzerinden regionId alınabilir.
    // Ancak WorldGuard bölgelerini kontrol ederken hala kullanışlı olabilir.
    private static String getWGRegionIdString(UUID ownerUUID) {
        return "skyblock_island_" + ownerUUID.toString();
    }

    public Island getIslandAt(Location location) {
        if (location == null || location.getWorld() == null) return null;

        World currentWorld = location.getWorld();
        World skyWorld = getSkyblockWorld(); // skyblockWorld'ün null olmadığından emin ol

        if (skyWorld == null || !currentWorld.equals(skyWorld)) {
            return null; // Sadece skyblock dünyasındaki adaları kontrol et
        }

        // WorldGuard entegrasyonu varsa kullan
        if (plugin.getWorldGuardInstance() != null) {
            RegionManager regionManager = plugin.getRegionManager(currentWorld);
            if (regionManager == null) {
                // Bu durum için loglama, plugin.getRegionManager içinde yapılmalı.
                return null; // RegionManager alınamazsa, konum tabanlı ada tespiti yapılamaz.
            }
            // Konumdaki tüm bölgeleri al
            com.sk89q.worldedit.util.Location weLocation = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
            com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            if (container == null) return null;
            RegionManager locRegionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            if(locRegionManager == null) return null;

            var applicableRegions = locRegionManager.getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

            for (ProtectedRegion region : applicableRegions) {
                String regionId = region.getId();
                if (regionId.startsWith("skyblock_island_")) {
                    try {
                        String uuidPart = regionId.substring("skyblock_island_".length());
                        UUID ownerUUID = UUID.fromString(uuidPart);
                        Island island = islandsData.get(ownerUUID);
                        // Ada bulunduysa ve regionId'si WorldGuard bölgesiyle eşleşiyorsa döndür.
                        // Bu, Island nesnesinin kendi regionId'si ile WG'deki regionId'nin tutarlılığını da doğrular.
                        if (island != null && island.getRegionId().equals(regionId)) {
                            return island;
                        }
                    } catch (IllegalArgumentException e) {
                        // Geçersiz UUID formatı, bu bölgeyi atla
                        plugin.getLogger().fine("Found a region matching prefix but with invalid UUID: " + regionId);
                    }
                }
            }
        } else {
            // WorldGuard yoksa, basit bir sınırlayıcı kutu (bounding box) veya merkez noktasına olan uzaklık kontrolü yapılabilir.
            // Bu kısım, WorldGuard olmadan çalışacak bir fallback mekanizması gerektirir ve şu an implemente edilmemiştir.
            // plugin.getLogger().info("WorldGuard is not enabled. Island detection at location relies on it.");
            // Örnek: En yakın ada merkezini bulup, o adanın sınırları içinde mi diye bakılabilir.
            // Bu, daha karmaşık ve daha az kesin bir yöntem olacaktır.
        }
        return null; // Eşleşen ada bulunamadı
    }


    public static class EmptyWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return Bukkit.createChunkData(world); // Tamamen boş chunklar oluşturur
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            // Dünyanın (0, Y, 0) noktasında bir spawn belirle, Y oyuncunun boğulmayacağı bir yükseklik olmalı.
            return new Location(world, 0.5, 128, 0.5);
        }
    }
}