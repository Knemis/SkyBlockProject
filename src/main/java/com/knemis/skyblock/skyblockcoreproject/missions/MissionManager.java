package com.knemis.skyblock.skyblockcoreproject.missions;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


import org.bukkit.entity.Player; // Player sınıfı için
import org.bukkit.ChatColor;    // ChatColor için
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MissionManager {
    private final SkyBlockProject plugin;
    private final Map<String, Mission> allMissions = new HashMap<>();

    public MissionManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        loadMissions();
    }

    private void loadMissions() {
        plugin.getLogger().info("[MissionManager] Starting to load missions...");
        File missionsFile = new File(plugin.getDataFolder(), "missions.yml");
        if (!missionsFile.exists()) {
            plugin.getLogger().info("[MissionManager] missions.yml not found, saving default resource.");
            plugin.saveResource("missions.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(missionsFile);
        ConfigurationSection missionsSection = config.getConfigurationSection("missions");

        if (missionsSection == null) {
            plugin.getLogger().warning("[MissionManager] No 'missions' section found in missions.yml. No missions will be loaded.");
            return;
        }

        Set<String> missionIds = missionsSection.getKeys(false);
        int successfullyLoadedCount = 0;
        int failedCount = 0;
        Map<String, Integer> categoriesLoaded = new HashMap<>();

        for (String missionId : missionIds) {
            ConfigurationSection missionData = missionsSection.getConfigurationSection(missionId);
            if (missionData == null) {
                plugin.getLogger().warning(String.format("[MissionManager] Mission data for ID '%s' is null. Skipping.", missionId));
                failedCount++;
                continue;
            }

            try {
                String name = missionData.getString("name", "Unnamed Mission");
                List<String> description = missionData.getStringList("description");
                String category = missionData.getString("category", "General");
                String iconMaterialName = missionData.getString("iconMaterial", "STONE");

                List<MissionObjective> objectives = new ArrayList<>();
                ConfigurationSection objectivesSection = missionData.getConfigurationSection("objectives");
                if (objectivesSection != null) {
                    for (String objectiveKey : objectivesSection.getKeys(false)) {
                        ConfigurationSection objData = objectivesSection.getConfigurationSection(objectiveKey);
                        if (objData == null) {
                            plugin.getLogger().warning(String.format("[MissionManager] Objective data for key '%s' in mission '%s' is null. Skipping objective.", objectiveKey, missionId));
                            continue;
                        }
                        objectives.add(new MissionObjective(
                                objData.getString("type"),
                                objData.getString("target"),
                                objData.getInt("amount"),
                                objData.getString("display_name_override")
                        ));
                    }
                } else if (missionData.isList("objectives")) {
                    List<Map<?, ?>> objectivesMapList = missionData.getMapList("objectives");
                    for (Map<?, ?> objMapRaw : objectivesMapList) {
                        Map<String, Object> objMap = (Map<String, Object>) objMapRaw;
                        objectives.add(new MissionObjective(
                                (String) objMap.get("type"),
                                String.valueOf(objMap.get("target")),
                                (Integer) objMap.get("amount"),
                                (String) objMap.get("display_name_override")
                        ));
                    }
                } else {
                     plugin.getLogger().warning(String.format("[MissionManager] Mission '%s' (ID: %s) has no objectives or objectives are malformed.", name, missionId));
                }


                ConfigurationSection rewardsSection = missionData.getConfigurationSection("rewards");
                MissionReward rewards = null;
                if (rewardsSection != null) {
                    double money = rewardsSection.getDouble("money", 0.0);
                    int experience = rewardsSection.getInt("experience", 0);
                    List<String> commandStrings = rewardsSection.getStringList("commands");
                    List<ItemStack> itemRewards = new ArrayList<>();
                    List<String> itemRewardStrings = rewardsSection.getStringList("items");
                    for (String itemString : itemRewardStrings) {
                        try {
                            String[] parts = itemString.split(" ");
                            Material mat = Material.matchMaterial(parts[0].toUpperCase());
                            int amount = 1;
                            if (parts.length > 1) amount = Integer.parseInt(parts[1]);
                            if (mat != null) {
                                itemRewards.add(new ItemStack(mat, amount));
                            } else {
                                plugin.getLogger().warning(String.format("[MissionManager] Invalid material '%s' in rewards for mission %s (ID: %s)", parts[0], name, missionId));
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning(String.format("[MissionManager] Error parsing item reward '%s' for mission %s (ID: %s): %s", itemString, name, missionId, e.getMessage()));
                        }
                    }
                    rewards = new MissionReward(money, experience, itemRewards, commandStrings);
                } else {
                    rewards = new MissionReward(0,0, Collections.emptyList(), Collections.emptyList());
                }

                List<String> dependencies = missionData.getStringList("dependencies");
                String repeatableType = missionData.getString("repeatableType", "NONE").toUpperCase();
                int cooldownHours = missionData.getInt("cooldownHours", 0);
                String requiredPermission = missionData.getString("requiredPermission");

                Mission mission = new Mission(missionId, name, description, category, iconMaterialName,
                        objectives, rewards, dependencies, repeatableType, cooldownHours, requiredPermission);
                allMissions.put(missionId, mission);
                categoriesLoaded.put(category, categoriesLoaded.getOrDefault(category, 0) + 1);
                successfullyLoadedCount++;
                plugin.getLogger().fine(String.format("[MissionManager] Loaded mission: %s (ID: %s, Category: %s)", name, missionId, category));

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, String.format("[MissionManager] Failed to load mission with ID '%s' due to an exception: %s", missionId, e.getMessage()), e);
                failedCount++;
            }
        }
        plugin.getLogger().info(String.format("[MissionManager] Mission loading complete. Successfully loaded: %d missions. Failed: %d missions.", successfullyLoadedCount, failedCount));
        if (!categoriesLoaded.isEmpty()) {
            categoriesLoaded.forEach((cat, count) -> plugin.getLogger().info(String.format("[MissionManager] Category '%s' has %d missions loaded.", cat, count)));
        }
    }

    public Mission getMission(String missionId) {
        // No logging needed for simple getter usually, unless for debugging specific mission retrieval issues.
        return allMissions.get(missionId);
    }

    public Collection<Mission> getAllMissions() {
        return Collections.unmodifiableCollection(allMissions.values());
    }

    public List<Mission> getAvailableMissions(PlayerMissionData playerData) {
        // No detailed logging here as it's a filter method, might be called frequently.
        // Specific checks within canStartMission will log reasons for unavailability.
        return allMissions.values().stream()
                .filter(mission -> !playerData.hasCompletedMission(mission.getId()) || !"NONE".equals(mission.getRepeatableType()))
                .filter(mission -> !playerData.isMissionOnCooldown(mission.getId()))
                .collect(Collectors.toList());
    }

    // This method is mostly internal logic for canStartMission, not directly called by commands usually.
    // public void startMission(PlayerMissionData playerData, String missionId) {
    //     Mission mission = getMission(missionId);
    //     if (mission != null) {
    //         playerData.addActiveMission(mission);
    //     }
    // }

    public boolean canStartMission(Player player, Mission mission) {
        String playerName = player.getName();
        // UUID playerUUID = player.getUniqueId(); // Already available via player.getUniqueId()
        String missionName = mission != null ? mission.getName() : "null_mission_object";
        String missionId = mission != null ? mission.getId() : "null_mission_id";
        // plugin.getLogger().info(String.format("[MissionManager] Checking if player %s (UUID: %s) can start mission '%s' (ID: %s)",
        //        playerName, player.getUniqueId(), missionName, missionId)); // This can be too verbose

        if (mission == null) {
            plugin.getLogger().warning(String.format("[MissionManager] canStartMission check for %s: Mission object is null.", playerName));
            return false;
        }
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());

        if (playerData.getActiveMissionProgress(mission.getId()) != null) {
            player.sendMessage(ChatColor.YELLOW + "You have already started the mission: " + mission.getName());
            plugin.getLogger().info(String.format("[MissionManager] Player %s cannot start mission '%s' (ID: %s): Already active.", playerName, missionName, missionId));
            return false;
        }

        if (playerData.hasCompletedMission(mission.getId()) && "NONE".equalsIgnoreCase(mission.getRepeatableType())) {
            player.sendMessage(ChatColor.YELLOW + "You have already completed the mission: " + mission.getName() + " and it's not repeatable.");
            plugin.getLogger().info(String.format("[MissionManager] Player %s cannot start mission '%s' (ID: %s): Already completed and not repeatable.", playerName, missionName, missionId));
            return false;
        }

        if (playerData.isMissionOnCooldown(mission.getId())) {
            long remaining = (playerData.getCooldownEndTime(mission.getId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.YELLOW + "Mission '" + mission.getName() + "' is on cooldown. Time remaining: " + formatCooldown(remaining));
            plugin.getLogger().info(String.format("[MissionManager] Player %s cannot start mission '%s' (ID: %s): On cooldown (%s remaining).", playerName, missionName, missionId, formatCooldown(remaining)));
            return false;
        }

        if (mission.getDependencies() != null && !mission.getDependencies().isEmpty()) {
            for (String dependencyId : mission.getDependencies()) {
                if (!playerData.hasCompletedMission(dependencyId)) {
                    Mission dependencyMission = getMission(dependencyId);
                    String depName = dependencyMission != null ? dependencyMission.getName() : dependencyId;
                    player.sendMessage(ChatColor.RED + "You must complete the mission '" + depName + "' first.");
                    plugin.getLogger().info(String.format("[MissionManager] Player %s cannot start mission '%s' (ID: %s): Dependency '%s' not met.", playerName, missionName, missionId, depName));
                    return false;
                }
            }
        }

        if (mission.getRequiredPermission() != null && !mission.getRequiredPermission().isEmpty() && !player.hasPermission(mission.getRequiredPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to start this mission.");
            plugin.getLogger().warning(String.format("[MissionManager] Player %s cannot start mission '%s' (ID: %s): Lacks permission '%s'.",
                    playerName, missionName, missionId, mission.getRequiredPermission()));
            return false;
        }
        // plugin.getLogger().info(String.format("[MissionManager] Player %s (UUID: %s) CAN start mission '%s' (ID: %s). All checks passed.",
        //        playerName, playerUUID, missionName, missionId)); // Also potentially verbose
        return true;
    }

    public boolean startMission(Player player, Mission mission) {
        String playerName = player.getName();
        // UUID playerUUID = player.getUniqueId(); // Already available
        String missionName = mission != null ? mission.getName() : "null_mission_object";
        String missionId = mission != null ? mission.getId() : "null_mission_id";
        plugin.getLogger().info(String.format("[MissionManager] Attempting to start mission '%s' (ID: %s) for player %s (UUID: %s).",
                missionName, missionId, playerName, player.getUniqueId()));

        if (!canStartMission(player, mission)) {
            // canStartMission already sent the message and logged the specific reason
            plugin.getLogger().warning(String.format("[MissionManager] startMission failed for player %s, mission '%s' (ID: %s) because canStartMission returned false.",
                    playerName, missionName, missionId));
            return false;
        }
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        playerData.addActiveMission(mission);
        player.sendMessage(ChatColor.GREEN + "Mission '" + mission.getName() + "' started!");
        plugin.getLogger().info(String.format("[MissionManager] Successfully started mission '%s' (ID: %s) for player %s (UUID: %s).",
                missionName, missionId, playerName, player.getUniqueId()));
        return true;
    }

    public void giveMissionRewards(Player player, Mission mission) {
        MissionReward rewards = mission.getRewards();
        String missionName = mission.getName();
        String missionId = mission.getId();
        String playerName = player.getName();
        // UUID playerUUID = player.getUniqueId(); // Already available
        plugin.getLogger().info(String.format("[MissionManager] Giving rewards for mission '%s' (ID: %s) to player %s (UUID: %s).",
                missionName, missionId, playerName, player.getUniqueId()));

        if (rewards == null) {
            plugin.getLogger().warning(String.format("[MissionManager] No rewards defined for mission '%s' (ID: %s). Player %s receives nothing.",
                    missionName, missionId, playerName));
            return;
        }

        boolean allRewardsGiven = true;
        StringBuilder rewardSummary = new StringBuilder();

        if (rewards.getMoney() > 0 && plugin.getEconomy() != null && EconomyManager.isEconomyAvailable()) {
            if (EconomyManager.deposit(player, rewards.getMoney())) { // EconomyManager logs success/failure
                player.sendMessage(ChatColor.GOLD + "You received " + rewards.getMoney() + " " + plugin.getEconomy().currencyNamePlural() + "!");
                rewardSummary.append(String.format("Money: %.2f, ", rewards.getMoney()));
            } else {
                plugin.getLogger().warning(String.format("[MissionManager] Failed to give money reward (%.2f) for mission '%s' to player %s.",
                        rewards.getMoney(), missionId, playerName));
                allRewardsGiven = false;
            }
        }
        if (rewards.getExperience() > 0) {
            player.giveExp(rewards.getExperience());
            player.sendMessage(ChatColor.AQUA + "You received " + rewards.getExperience() + " experience points!");
            rewardSummary.append(String.format("XP: %d, ", rewards.getExperience()));
        }
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            int itemsGivenCount = 0;
            for (ItemStack item : rewards.getItems()) {
                HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item.clone());
                if (!unadded.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), unadded.get(0));
                    player.sendMessage(ChatColor.YELLOW + "Some items couldn't fit in your inventory and were dropped nearby!");
                    plugin.getLogger().warning(String.format("[MissionManager] Item reward %s for mission '%s' for player %s was partially dropped.",
                            unadded.get(0).toString(), missionId, playerName));
                    allRewardsGiven = false; // Or handle as partial success
                }
                itemsGivenCount++;
            }
            if (itemsGivenCount > 0) {
                player.sendMessage(ChatColor.GREEN + "You received item rewards!");
                rewardSummary.append(String.format("Items: %d types, ", itemsGivenCount));
            }
        }
        if (rewards.getCommands() != null && !rewards.getCommands().isEmpty()) {
            int commandsExecuted = 0;
            for (String command : rewards.getCommands()) {
                String processedCommand = command.replace("{player}", player.getName());
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                    plugin.getLogger().info(String.format("[MissionManager] Executed reward command for mission '%s' for player %s: %s",
                            missionId, playerName, processedCommand));
                    commandsExecuted++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, String.format("[MissionManager] Failed to execute reward command '%s' for mission '%s' for player %s.",
                            processedCommand, missionId, playerName), e);
                    allRewardsGiven = false;
                }
            }
             if (commandsExecuted > 0) rewardSummary.append(String.format("Commands: %d, ", commandsExecuted));
        }

        if (allRewardsGiven) {
            plugin.getLogger().info(String.format("[MissionManager] All rewards for mission '%s' (ID: %s) successfully given to player %s. Summary: %s",
                    missionName, missionId, playerName, rewardSummary.length() > 0 ? rewardSummary.substring(0, rewardSummary.length() - 2) : "None"));
        } else {
            plugin.getLogger().warning(String.format("[MissionManager] Some rewards for mission '%s' (ID: %s) failed to be given to player %s. Summary of attempted: %s",
                    missionName, missionId, playerName, rewardSummary.length() > 0 ? rewardSummary.substring(0, rewardSummary.length() - 2) : "None"));
        }
    }


    public void completeMission(Player player, Mission mission) {
        String playerName = player.getName();
        // UUID playerUUID = player.getUniqueId(); // Already available
        String missionName = mission != null ? mission.getName() : "null_mission_object";
        String missionId = mission != null ? mission.getId() : "null_mission_id";
        plugin.getLogger().info(String.format("[MissionManager] Attempting to complete mission '%s' (ID: %s) for player %s (UUID: %s).",
                missionName, missionId, playerName, player.getUniqueId()));

        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        if (mission == null || playerData.getActiveMissionProgress(missionId) == null) {
            plugin.getLogger().warning(String.format("[MissionManager] completeMission called for %s, but mission '%s' is null or not active for player.", playerName, missionId));
            return;
        }

        playerData.markMissionCompleted(mission.getId(), mission); // Handles cooldowns
        giveMissionRewards(player, mission); // giveMissionRewards logs its own details

        player.sendMessage(ChatColor.GREEN + "Mission '" + mission.getName() + "' completed! Rewards received.");
        plugin.getLogger().info(String.format("[MissionManager] Mission '%s' (ID: %s) successfully completed by player %s (UUID: %s).",
                missionName, missionId, playerName, player.getUniqueId()));
    }

    public void abandonMission(Player player, String missionId) {
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        Mission mission = getMission(missionId); // Get mission for logging name
        String missionName = mission != null ? mission.getName() : missionId;
        plugin.getLogger().info(String.format("[MissionManager] Player %s (UUID: %s) attempting to abandon mission '%s' (ID: %s).",
                player.getName(), player.getUniqueId(), missionName, missionId));

        if (playerData.getActiveMissionProgress(missionId) != null) {
            playerData.removeActiveMission(missionId);
            player.sendMessage(ChatColor.YELLOW + "Mission '" + missionName + "' abandoned.");
            plugin.getLogger().info(String.format("[MissionManager] Player %s successfully abandoned mission '%s' (ID: %s).", player.getName(), missionName, missionId));
        } else {
            player.sendMessage(ChatColor.RED + "You are not currently on that mission.");
            plugin.getLogger().warning(String.format("[MissionManager] Player %s failed to abandon mission '%s' (ID: %s): Not active.", player.getName(), missionName, missionId));
        }
    }

    public void resetMissionProgress(Player player, String missionId) {
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        Mission mission = getMission(missionId);
        String missionName = mission != null ? mission.getName() : missionId;
        plugin.getLogger().info(String.format("[MissionManager] Player %s (UUID: %s) attempting to reset progress for mission '%s' (ID: %s).",
                player.getName(), player.getUniqueId(), missionName, missionId));

        // Check if there's any progress to reset
        boolean wasActive = playerData.getActiveMissionProgress(missionId) != null;
        boolean wasCompleted = playerData.hasCompletedMission(missionId);
        boolean wasOnCooldown = playerData.isMissionOnCooldown(missionId);

        if (wasActive || wasCompleted || wasOnCooldown) {
            playerData.removeActiveMission(missionId);      // Remove from active
            playerData.removeCompletedMission(missionId); // Remove from completed
            playerData.removeMissionCooldown(missionId);  // Remove from cooldowns
            
            player.sendMessage(ChatColor.YELLOW + "Progress for mission '" + missionName + "' has been reset.");
            plugin.getLogger().info(String.format("[MissionManager] Player %s successfully reset progress for mission '%s' (ID: %s). Status before reset - Active: %b, Completed: %b, Cooldown: %b", 
                                    player.getName(), missionName, missionId, wasActive, wasCompleted, wasOnCooldown));
        } else {
            player.sendMessage(ChatColor.RED + "Could not reset progress for mission '" + missionName + "'. No active progress, completion, or cooldown found.");
            plugin.getLogger().warning(String.format("[MissionManager] Player %s failed to reset mission '%s' (ID: %s): No active, completed, or cooldown status found to reset.",
                    player.getName(), missionName, missionId));
        }
    }


    public boolean isMissionComplete(Player player, Mission mission) { 
        if (mission == null) return false;
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        PlayerMissionProgress progress = playerData.getActiveMissionProgress(mission.getId());
        if (progress == null) return false; // Not started or already completed and removed

        return progress.areAllObjectivesCompleted(mission);
    }

    public void updateKillMobProgress(Player player, org.bukkit.entity.EntityType mobKilled) {
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null || playerData.getActiveMissions().isEmpty()) return;

        // Create a list of mission IDs to avoid ConcurrentModificationException if a mission is completed
        List<String> activeMissionIds = new ArrayList<>(playerData.getActiveMissions().keySet());

        for (String missionId : activeMissionIds) {
            PlayerMissionProgress prog = playerData.getActiveMissionProgress(missionId);
            if (prog == null) continue; // Should not happen if activeMissionIds is from playerData.getActiveMissions()

            Mission mission = getMission(prog.getMissionId());
            if (mission == null) continue;

            List<MissionObjective> objectives = mission.getObjectives();
            for (int i = 0; i < objectives.size(); i++) {
                MissionObjective obj = objectives.get(i);
                if ("KILL_MOB".equalsIgnoreCase(obj.getType()) &&
                        obj.getTarget().equalsIgnoreCase(mobKilled.name())) {

                    int currentAmount = prog.getProgress(i);
                    if (currentAmount < obj.getAmount()) {
                        prog.incrementProgress(i, 1);
                        currentAmount++; // Update for message
                        player.sendMessage(ChatColor.AQUA + "Progress for '" + mission.getName() + "': Killed " + mobKilled.name() + " (" + currentAmount + "/" + obj.getAmount() + ")");

                        // Check completion after updating progress
                        if (isMissionComplete(player, mission)) {
                            completeMission(player, mission);
                            // If completeMission removes the mission from active,
                            // we need to handle loop carefully - using list of IDs helps here
                            break; // Break from objectives loop for this mission as it's now complete
                        }
                    }
                }
            }
        }
    }

    private String formatCooldown(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes < 60) return minutes + "m " + seconds + "s";
        long hours = minutes / 60;
        minutes %= 60;
        return hours + "h " + minutes + "m " + seconds + "s";
    }
}