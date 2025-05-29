// IslandWorthManager.java
package com.knemis.skyblock.skyblockcoreproject.economy.worth;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // IslandDataHandler'a erişim için
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager; // Ada bölgesini almak için

import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import org.bukkit.configuration.ConfigurationSection;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class IslandWorthManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandLifecycleManager islandLifecycleManager;
    private final Map<Material, Double> blockValues;
    private final Map<Integer, Double> levelRequirements; // Seviye için gereken minimum değer
    private final Map<Integer, String> levelUpConsoleCommands; // Seviye atlama ödül komutları

    public IslandWorthManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandLifecycleManager islandLifecycleManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
        this.blockValues = new HashMap<>();
        this.levelRequirements = new HashMap<>();
        this.levelUpConsoleCommands = new HashMap<>();
        loadWorthConfig();
    }

    private void loadWorthConfig() {
        blockValues.clear();
        levelRequirements.clear();
        levelUpConsoleCommands.clear();

        ConfigurationSection blockValuesSection = plugin.getConfig().getConfigurationSection("island.worth.block_values");
        if (blockValuesSection != null) {
            for (String materialName : blockValuesSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    double value = blockValuesSection.getDouble(materialName);
                    blockValues.put(material, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[IslandWorth] Geçersiz materyal adı '" + materialName + "' block_values altında bulundu.");
                }
            }
        } else {
            plugin.getLogger().warning("[IslandWorth] 'island.worth.block_values' config bölümü bulunamadı!");
        }

        ConfigurationSection levelReqSection = plugin.getConfig().getConfigurationSection("island.worth.level_requirements");
        if (levelReqSection != null) {
            for (String levelStr : levelReqSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelStr);
                    double requiredWorth = levelReqSection.getDouble(levelStr);
                    levelRequirements.put(level, requiredWorth);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("[IslandWorth] Geçersiz seviye sayısı '" + levelStr + "' level_requirements altında bulundu.");
                }
            }
        } else {
            plugin.getLogger().warning("[IslandWorth] 'island.worth.level_requirements' config bölümü bulunamadı!");
        }
        // En azından 1. seviye için 0 değerini garantileyelim
        if (!levelRequirements.containsKey(1)) {
            levelRequirements.put(1, 0.0);
        }


        ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("island.worth.level_up_rewards");
        if (rewardsSection != null) {
            for (String levelStr : rewardsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelStr);
                    if (rewardsSection.isString(levelStr)) {
                        levelUpConsoleCommands.put(level, rewardsSection.getString(levelStr));
                    } else if (rewardsSection.isDouble(levelStr) || rewardsSection.isInt(levelStr)) {
                        // Doğrudan para ödülü için özel bir komut formatı kullanalım
                        levelUpConsoleCommands.put(level, "eco give {player} " + rewardsSection.getDouble(levelStr));
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("[IslandWorth] Geçersiz seviye sayısı '" + levelStr + "' level_up_rewards altında bulundu.");
                }
            }
        }
        plugin.getLogger().info("[IslandWorth] " + blockValues.size() + " blok değeri ve " + levelRequirements.size() + " seviye gereksinimi yüklendi.");
    }

    public void calculateAndSetIslandWorth(Island island, Player requester) {
        if (island == null || island.getBaseLocation() == null || island.getWorld() == null) {
            if (requester != null && requester.isOnline()) {
                requester.sendMessage(ChatColor.RED + "Ada bilgileri eksik, değer hesaplanamıyor.");
            }
            return;
        }

        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.YELLOW + "Ada değerin hesaplanıyor... Bu işlem biraz zaman alabilir.");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                double totalWorth = 0.0;
                World world = island.getWorld();
                ProtectedRegion wgRegion;
                RegionManager regionManager = plugin.getRegionManager(world);

                if (regionManager == null) {
                    plugin.getLogger().severe("[IslandWorth] RegionManager null, " + island.getOwnerUUID() + " için ada değeri hesaplanamıyor.");
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(ChatColor.RED + "Bölge yöneticisi bulunamadı, ada değeri hesaplanamıyor.");
                    }
                    return;
                }
                wgRegion = regionManager.getRegion(islandLifecycleManager.getRegionId(island.getOwnerUUID()));

                if (wgRegion == null) {
                    try { // Eğer WG region yoksa, LifecycleManager'dan territory almayı dene
                        com.sk89q.worldedit.regions.Region territory = islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation());
                        if (territory == null) {
                            if (requester != null && requester.isOnline()) {
                                requester.sendMessage(ChatColor.RED + "Ada bölgesi bulunamadı, değer hesaplanamıyor.");
                            }
                            return;
                        }
                        // WG region'ı olmayan territory'yi yine de CuboidRegion gibi ele alacağız.
                        // Bu durumda direkt worldedit region'ını kullanacağız.
                        wgRegion = new com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion(
                                "temp_worth_calc", territory.getMinimumPoint(), territory.getMaximumPoint()
                        );

                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "[IslandWorth] Ada bölgesi alınırken hata: " + island.getOwnerUUID(), e);
                        if (requester != null && requester.isOnline()) {
                            requester.sendMessage(ChatColor.RED + "Ada bölgesi alınırken bir hata oluştu.");
                        }
                        return;
                    }
                }


                // WGRegion'dan min/max noktaları al
                BlockVector3 min = wgRegion.getMinimumPoint();
                BlockVector3 max = wgRegion.getMaximumPoint();

                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue; // Dünya sınırları kontrolü
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            // Bölgenin gerçekten o koordinatı içerip içermediğini kontrol et (silindirik vb. bölgeler için)
                            if (wgRegion.contains(x, y, z)) {
                                Block block = world.getBlockAt(x, y, z);
                                totalWorth += blockValues.getOrDefault(block.getType(), 0.0);
                            }
                        }
                    }
                }

                final double finalTotalWorth = totalWorth;
                int oldLevel = island.getIslandLevel();

                // Değeri ve seviyeyi güncelle, sonra ana thread'de oyuncuya bildir ve ödül ver.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    island.setIslandWorth(finalTotalWorth);
                    int newLevel = calculateLevelFromWorth(finalTotalWorth);
                    island.setIslandLevel(newLevel);
                    islandDataHandler.addOrUpdateIslandData(island); // Veriyi güncelle
                    islandDataHandler.saveChangesToDisk();      // Diske yaz

                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(ChatColor.GREEN + "Ada değerin hesaplandı: " + ChatColor.GOLD + String.format("%.2f", finalTotalWorth));
                        requester.sendMessage(ChatColor.GREEN + "Ada Seviyen: " + ChatColor.GOLD + newLevel);
                    }

                    // Update mission progress for ISLAND_LEVEL objectives
                    Player islandOwner = Bukkit.getPlayer(island.getOwnerUUID());
                    if (islandOwner != null && islandOwner.isOnline()) {
                        if (plugin.getMissionManager() != null) {
                            plugin.getMissionManager().updateIslandLevelProgress(islandOwner, newLevel);
                            plugin.getLogger().info("[IslandWorth] Updated island level mission progress for player " + islandOwner.getName() + " to level " + newLevel);
                        } else {
                            plugin.getLogger().warning("[IslandWorth] MissionManager is null, cannot update island level objectives for " + islandOwner.getName());
                        }
                    }


                    if (newLevel > oldLevel) {
                        plugin.getLogger().info(island.getOwnerUUID() + " adası " + oldLevel + " seviyesinden " + newLevel + " seviyesine yükseldi!");
                        if (requester != null && requester.isOnline()) {
                            requester.sendMessage(ChatColor.AQUA + "Tebrikler! Adan " + ChatColor.GOLD + newLevel + ". seviyeye ulaştı!");
                        }
                        // Seviye atlama ödüllerini ver
                        for (int levelReached = oldLevel + 1; levelReached <= newLevel; levelReached++) {
                            giveLevelUpReward(island, levelReached);
                        }
                    }
                });

            }
        }.runTaskAsynchronously(plugin); // Hesaplamayı asenkron yap
    }

    private int calculateLevelFromWorth(double worth) {
        int currentLevel = 0;
        // Seviyeleri büyükten küçüğe doğru sıralayarak kontrol et
        for (Map.Entry<Integer, Double> entry : levelRequirements.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toList())) { //toList Java 16+
            if (worth >= entry.getValue()) {
                currentLevel = Math.max(currentLevel, entry.getKey());
                // return entry.getKey(); // İlk eşleşen en yüksek seviye olacak (eğer gereksinimler artan sıradaysa)
                // Ancak sıralamayı tersine çevirdiğimiz için Math.max ile gitmek daha doğru.
                // Ya da en yüksekten başlayıp ilk uyanı al.
                return entry.getKey(); // En yüksekten başladığımız için ilk uygun olan en yüksek seviyedir.
            }
        }
        // Hiçbir gereksinimi karşılamıyorsa (config hatası veya çok düşük değer), en düşük seviye.
        return levelRequirements.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1); // Varsayılan 1
    }

    private void giveLevelUpReward(Island island, int newLevel) {
        String commandTemplate = levelUpConsoleCommands.get(newLevel);
        if (commandTemplate != null && !commandTemplate.isEmpty()) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerUUID());
            String ownerName = (owner != null && owner.getName() != null) ? owner.getName() : "OyuncuBulunamadı";

            String commandToExecute = commandTemplate.replace("{player}", ownerName)
                    .replace("{island_owner_uuid}", island.getOwnerUUID().toString())
                    .replace("{island_level}", String.valueOf(newLevel));
            try {
                plugin.getLogger().info("[IslandWorth] Seviye ödülü veriliyor (Seviye " + newLevel + " - Ada: " + island.getOwnerUUID() + "): " + commandToExecute);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[IslandWorth] Seviye ödül komutu çalıştırılırken hata oluştu: " + commandToExecute, e);
            }

        }
    }
}