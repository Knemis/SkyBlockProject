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
        if (playerUuid == null) {
            plugin.getLogger().warning("[MissionPlayerDataManager] getPlayerData called with null playerUuid.");
            return new PlayerMissionData(null); // Or throw IllegalArgumentException
        }
        if (playerDataCache.containsKey(playerUuid)) {
            return playerDataCache.get(playerUuid);
        }
        loadPlayerData(playerUuid); // This will cache it or create a new one if not found/failed
        
        // Ensure data is in cache after load attempt, if not, create new as a final fallback
        if (!playerDataCache.containsKey(playerUuid)) {
            plugin.getLogger().warning(String.format("[MissionPlayerDataManager] loadPlayerData for %s did not result in cached data. Creating new PlayerMissionData as fallback.", playerUuid));
            PlayerMissionData newPlayerData = new PlayerMissionData(playerUuid);
            playerDataCache.put(playerUuid, newPlayerData);
            return newPlayerData;
        }
        return playerDataCache.get(playerUuid);
    }

    public void loadPlayerData(UUID playerUuid) {
        if (playerUuid == null) {
            plugin.getLogger().warning("[MissionPlayerDataManager] loadPlayerData called with null playerUuid. Skipping.");
            return;
        }
        plugin.getLogger().info(String.format("[MissionPlayerDataManager] Attempting to load mission data for player %s.", playerUuid));
        File playerFile = new File(plugin.getDataFolder() + File.separator + "missiondata", playerUuid.toString() + ".yml");

        if (!playerFile.getParentFile().exists()) {
            if (!playerFile.getParentFile().mkdirs()) {
                plugin.getLogger().severe(String.format("[MissionPlayerDataManager] Could not create missiondata directory for player %s at %s",
                        playerUuid, playerFile.getParentFile().getAbsolutePath()));
                // Create new data in cache as loading failed due to directory issue
                playerDataCache.put(playerUuid, new PlayerMissionData(playerUuid));
                plugin.getLogger().warning(String.format("[MissionPlayerDataManager] Created new PlayerMissionData in cache for %s due to directory creation failure.", playerUuid));
                return;
            }
        }

        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            Map<String, PlayerMissionProgress> activeMissions = new HashMap<>();
            Set<String> completedMissions = new HashSet<>(config.getStringList("completed-missions"));
            Map<String, Long> missionCooldowns = new HashMap<>();
            int loadedActive = 0;
            int warnings = 0;

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
                                    plugin.getLogger().warning(String.format("[MissionPlayerDataManager] Invalid objective index '%s' for mission %s for player %s. Skipping objective.",
                                            objectiveIndexStr, missionId, playerUuid));
                                    warnings++;
                                }
                            }
                        }
                        activeMissions.put(missionId, new PlayerMissionProgress(missionId, objectiveProgressMap, startTime));
                        loadedActive++;
                    } else {
                         plugin.getLogger().warning(String.format("[MissionPlayerDataManager] Mission progress section for mission %s is null for player %s. Skipping active mission entry.", missionId, playerUuid));
                         warnings++;
                    }
                }
            }

            ConfigurationSection cooldownsSection = config.getConfigurationSection("mission-cooldowns");
            if (cooldownsSection != null) {
                for (String missionId : cooldownsSection.getKeys(false)) {
                    if (cooldownsSection.isLong(missionId) || cooldownsSection.isInt(missionId)) {
                        missionCooldowns.put(missionId, cooldownsSection.getLong(missionId));
                    } else {
                        plugin.getLogger().warning(String.format("[MissionPlayerDataManager] Invalid cooldown value for mission %s for player %s. Must be a number. Skipping cooldown.", missionId, playerUuid));
                        warnings++;
                    }
                }
            }
            PlayerMissionData loadedData = new PlayerMissionData(playerUuid, activeMissions, completedMissions, missionCooldowns);
            playerDataCache.put(playerUuid, loadedData);
            plugin.getLogger().info(String.format("[MissionPlayerDataManager] Successfully loaded mission data for player %s. Active: %d, Completed: %d, Cooldowns: %d. Warnings during load: %d.",
                    playerUuid, loadedActive, completedMissions.size(), missionCooldowns.size(), warnings));
        } else {
            PlayerMissionData newPlayerData = new PlayerMissionData(playerUuid);
            playerDataCache.put(playerUuid, newPlayerData);
            plugin.getLogger().info(String.format("[MissionPlayerDataManager] No mission data file found for player %s. Created new PlayerMissionData in cache.", playerUuid));
        }
    }

    public void savePlayerData(UUID playerUuid, boolean removeFromCache) {
        if (playerUuid == null) {
            plugin.getLogger().warning("[MissionPlayerDataManager] savePlayerData called with null playerUuid. Skipping.");
            return;
        }
        PlayerMissionData dataToSave = playerDataCache.get(playerUuid);
        if (dataToSave == null) {
            plugin.getLogger().warning(String.format("[MissionPlayerDataManager] Attempted to save mission data for %s, but no data was found in cache. Skipping save.", playerUuid));
            return;
        }
        plugin.getLogger().info(String.format("[MissionPlayerDataManager] Attempting to save mission data for player %s.", playerUuid));

        File playerFile = new File(plugin.getDataFolder() + File.separator + "missiondata", playerUuid.toString() + ".yml");
        if (!playerFile.getParentFile().exists()) {
            if (!playerFile.getParentFile().mkdirs()) {
                plugin.getLogger().severe(String.format("[MissionPlayerDataManager] Could not create missiondata directory for player %s at %s. Save failed.",
                        playerUuid, playerFile.getParentFile().getAbsolutePath()));
                return;
            }
        }

        YamlConfiguration config = new YamlConfiguration();
        int savedActive = 0;

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
                savedActive++;
            }
        } else {
            config.set("active-missions", null);
        }

        config.set("completed-missions", new ArrayList<>(dataToSave.getCompletedMissions()));

        if (dataToSave.getMissionCooldowns() != null && !dataToSave.getMissionCooldowns().isEmpty()) {
            // Bukkit's config system should handle Map<String, Long> directly, but let's be safe
            ConfigurationSection cooldownsSection = config.createSection("mission-cooldowns");
            dataToSave.getMissionCooldowns().forEach(cooldownsSection::set);
        } else {
            config.set("mission-cooldowns", null);
        }

        try {
            config.save(playerFile);
            plugin.getLogger().info(String.format("[MissionPlayerDataManager] Successfully saved mission data for player %s to %s. Active: %d, Completed: %d, Cooldowns: %d.",
                    playerUuid, playerFile.getName(), savedActive, dataToSave.getCompletedMissions().size(), dataToSave.getMissionCooldowns().size()));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("[MissionPlayerDataManager] Could not save mission data for player %s to %s.", playerUuid, playerFile.getName()), e);
        }

        if (removeFromCache) {
            playerDataCache.remove(playerUuid);
            plugin.getLogger().info(String.format("[MissionPlayerDataManager] Removed mission data from cache for player %s.", playerUuid));
        }
    }

    public void saveAllPlayerData() {
        plugin.getLogger().info(String.format("[MissionPlayerDataManager] Attempting to save mission data for all %d cached players...", playerDataCache.size()));
        int count = 0;
        for (UUID playerUuid : new HashSet<>(playerDataCache.keySet())) { // Iterate over a copy of keys
            savePlayerData(playerUuid, false); // false: do not remove from cache during a save-all
            count++;
        }
        plugin.getLogger().info(String.format("[MissionPlayerDataManager] Finished saving mission data for %d players.", count));
    }

    public void clearPlayerDataFromCache(UUID playerUuid) {
        if (playerUuid == null) {
            plugin.getLogger().warning("[MissionPlayerDataManager] clearPlayerDataFromCache called with null playerUuid.");
            return;
        }
        if (playerDataCache.remove(playerUuid) != null) {
            plugin.getLogger().info(String.format("[MissionPlayerDataManager] Cleared mission data from cache for player %s.", playerUuid));
        } else {
            plugin.getLogger().fine(String.format("[MissionPlayerDataManager] Attempted to clear mission data from cache for player %s, but they were not cached.", playerUuid));
        }
    }
}
