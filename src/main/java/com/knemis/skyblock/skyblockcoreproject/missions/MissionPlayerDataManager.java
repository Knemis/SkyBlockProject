package com.knemis.skyblock.skyblockcoreproject.missions;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class MissionPlayerDataManager {
    private final SkyBlockProject plugin;
    private final Map<UUID, PlayerMissionData> playerDataCache = new HashMap<>();

    public MissionPlayerDataManager(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    public PlayerMissionData getPlayerData(UUID playerUuid) {
        if (playerDataCache.containsKey(playerUuid)) {
            return playerDataCache.get(playerUuid);
        }
        // loadPlayerData will cache it if found, or create a new one and cache if not.
        loadPlayerData(playerUuid);
        // If loadPlayerData created a new one because file didn't exist, it's now in cache.
        // If load failed catastrophically and didn't cache, this might still be null,
        // so ensure a new one is created as a fallback.
        if (!playerDataCache.containsKey(playerUuid)) {
            PlayerMissionData newPlayerData = new PlayerMissionData(playerUuid);
            playerDataCache.put(playerUuid, newPlayerData);
            plugin.getLogger().info("Created new PlayerMissionData for " + playerUuid.toString());
            return newPlayerData;
        }
        return playerDataCache.get(playerUuid);
    }

    public void loadPlayerData(UUID playerUuid) {
        File playerFile = new File(plugin.getDataFolder() + File.separator + "missiondata", playerUuid.toString() + ".yml");
        if (!playerFile.getParentFile().exists()) {
            playerFile.getParentFile().mkdirs();
        }

        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            Map<String, PlayerMissionProgress> activeMissions = new HashMap<>();
            Set<String> completedMissions = new HashSet<>(config.getStringList("completed-missions"));
            Map<String, Long> missionCooldowns = new HashMap<>();

            ConfigurationSection activeMissionsSection = config.getConfigurationSection("active-missions");
            if (activeMissionsSection != null) {
                for (String missionId : activeMissionsSection.getKeys(false)) {
                    ConfigurationSection missionProgressSection = activeMissionsSection.getConfigurationSection(missionId);
                    if (missionProgressSection != null) {
                        long startTime = missionProgressSection.getLong("start-time", System.currentTimeMillis());
                        Map<Integer, Integer> objectiveProgressMap = new HashMap<>();
                        ConfigurationSection objectivesSection = missionProgressSection.getConfigurationSection("objectives");
                        if (objectivesSection != null) {
                            for (String objectiveIndexStr : objectivesSection.getKeys(false)) {
                                try {
                                    int objectiveIndex = Integer.parseInt(objectiveIndexStr);
                                    objectiveProgressMap.put(objectiveIndex, objectivesSection.getInt(objectiveIndexStr));
                                } catch (NumberFormatException e) {
                                    plugin.getLogger().warning("Invalid objective index '" + objectiveIndexStr + "' for mission " + missionId + " for player " + playerUuid);
                                }
                            }
                        }
                        activeMissions.put(missionId, new PlayerMissionProgress(missionId, objectiveProgressMap, startTime));
                    }
                }
            }

            ConfigurationSection cooldownsSection = config.getConfigurationSection("mission-cooldowns");
            if (cooldownsSection != null) {
                for (String missionId : cooldownsSection.getKeys(false)) {
                    missionCooldowns.put(missionId, cooldownsSection.getLong(missionId));
                }
            }
            PlayerMissionData loadedData = new PlayerMissionData(playerUuid, activeMissions, completedMissions, missionCooldowns);
            playerDataCache.put(playerUuid, loadedData);
            plugin.getLogger().info("Loaded mission data for player " + playerUuid.toString());
        } else {
            // Player file doesn't exist, create new data
            PlayerMissionData newPlayerData = new PlayerMissionData(playerUuid);
            playerDataCache.put(playerUuid, newPlayerData);
            plugin.getLogger().info("No mission data file found for " + playerUuid.toString() + ". Created new PlayerMissionData.");
        }
    }

    public void savePlayerData(UUID playerUuid, boolean removeFromCache) {
        PlayerMissionData dataToSave = playerDataCache.get(playerUuid);
        if (dataToSave == null) {
            plugin.getLogger().warning("Attempted to save mission data for " + playerUuid + ", but no data was found in cache.");
            return;
        }

        File playerFile = new File(plugin.getDataFolder() + File.separator + "missiondata", playerUuid.toString() + ".yml");
        if (!playerFile.getParentFile().exists()) {
            playerFile.getParentFile().mkdirs();
        }

        YamlConfiguration config = new YamlConfiguration();

        // Save active missions
        if (dataToSave.getActiveMissions() != null && !dataToSave.getActiveMissions().isEmpty()) {
            for (Map.Entry<String, PlayerMissionProgress> entry : dataToSave.getActiveMissions().entrySet()) {
                String missionId = entry.getKey();
                PlayerMissionProgress progress = entry.getValue();
                String basePath = "active-missions." + missionId;
                config.set(basePath + ".start-time", progress.getStartTime());
                if (progress.getObjectiveProgress() != null) {
                    for (Map.Entry<Integer, Integer> objEntry : progress.getObjectiveProgress().entrySet()) {
                        config.set(basePath + ".objectives." + objEntry.getKey(), objEntry.getValue());
                    }
                }
            }
        } else {
            config.set("active-missions", null); // Clear section if empty
        }


        config.set("completed-missions", new ArrayList<>(dataToSave.getCompletedMissions()));

        // Save cooldowns - Bukkit should handle Map<String, Long>
        if (dataToSave.getMissionCooldowns() != null && !dataToSave.getMissionCooldowns().isEmpty()) {
            config.createSection("mission-cooldowns", dataToSave.getMissionCooldowns());
        } else {
            config.set("mission-cooldowns", null); // Clear section if empty
        }


        try {
            config.save(playerFile);
            plugin.getLogger().info("Saved mission data for player " + playerUuid.toString());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save mission data for " + playerUuid.toString(), e);
        }

        if (removeFromCache) {
            playerDataCache.remove(playerUuid);
            plugin.getLogger().info("Removed mission data from cache for player " + playerUuid.toString());
        }
    }

    public void saveAllPlayerData() {
        plugin.getLogger().info("Saving mission data for all cached players...");
        // Iterate over a copy of keys to avoid ConcurrentModificationException if savePlayerData modifies the cache
        for (UUID playerUuid : new HashSet<>(playerDataCache.keySet())) {
            savePlayerData(playerUuid, false); // Don't remove from cache during a full save-all
        }
        plugin.getLogger().info("Finished saving all cached player mission data.");
    }

    public void clearPlayerDataFromCache(UUID playerUuid) {
        playerDataCache.remove(playerUuid);
        plugin.getLogger().info("Cleared mission data from cache for player " + playerUuid.toString() + " (if present).");
    }
}
