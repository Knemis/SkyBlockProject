package com.knemis.skyblock.skyblockcoreproject.missions;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        File missionsFile = new File(plugin.getDataFolder(), "missions.yml");
        if (!missionsFile.exists()) {
            plugin.saveResource("missions.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(missionsFile);
        ConfigurationSection missionsSection = config.getConfigurationSection("missions");

        if (missionsSection == null) {
            plugin.getLogger().warning("No 'missions' section found in missions.yml. No missions will be loaded.");
            return;
        }

        Set<String> missionIds = missionsSection.getKeys(false);
        for (String missionId : missionIds) {
            ConfigurationSection missionData = missionsSection.getConfigurationSection(missionId);
            if (missionData == null) {
                plugin.getLogger().warning("Mission data for ID '" + missionId + "' is null. Skipping.");
                continue;
            }

            try {
                String name = missionData.getString("name", "Unnamed Mission");
                List<String> description = missionData.getStringList("description");
                String category = missionData.getString("category", "General");
                String iconMaterialName = missionData.getString("iconMaterial", "STONE");
                // Material.matchMaterial(iconMaterialName) can be used for validation if needed upon use

                // Parse Objectives
                List<MissionObjective> objectives = new ArrayList<>();
                ConfigurationSection objectivesSection = missionData.getConfigurationSection("objectives");
                if (objectivesSection != null) { // Old format might be a list
                    for (String objectiveKey : objectivesSection.getKeys(false)) {
                        ConfigurationSection objData = objectivesSection.getConfigurationSection(objectiveKey);
                        if (objData == null) continue;
                        objectives.add(new MissionObjective(
                                objData.getString("type"),
                                objData.getString("target"),
                                objData.getInt("amount"),
                                objData.getString("display_name_override")
                        ));
                    }
                } else if (missionData.isList("objectives")) { // New list format
                    List<Map<?, ?>> objectivesMapList = missionData.getMapList("objectives");
                    for (Map<?, ?> objMapRaw : objectivesMapList) {
                        // Manually cast and check types from Map<?, ?>
                        Map<String, Object> objMap = (Map<String, Object>) objMapRaw;
                        objectives.add(new MissionObjective(
                                (String) objMap.get("type"),
                                String.valueOf(objMap.get("target")), // Target can be int or string
                                (Integer) objMap.get("amount"),
                                (String) objMap.get("display_name_override")
                        ));
                    }
                }


                // Parse Rewards
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
                                plugin.getLogger().warning("Invalid material '" + parts[0] + "' in rewards for mission " + missionId);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error parsing item reward '" + itemString + "' for mission " + missionId + ": " + e.getMessage());
                        }
                    }
                    rewards = new MissionReward(money, experience, itemRewards, commandStrings);
                } else {
                    rewards = new MissionReward(0,0, Collections.emptyList(), Collections.emptyList()); // Default empty rewards
                }

                List<String> dependencies = missionData.getStringList("dependencies");
                String repeatableType = missionData.getString("repeatableType", "NONE").toUpperCase();
                int cooldownHours = missionData.getInt("cooldownHours", 0);
                String requiredPermission = missionData.getString("requiredPermission");

                Mission mission = new Mission(missionId, name, description, category, iconMaterialName,
                        objectives, rewards, dependencies, repeatableType, cooldownHours, requiredPermission);
                allMissions.put(missionId, mission);
                plugin.getLogger().info("Loaded mission: " + name + " (ID: " + missionId + ")");

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load mission with ID '" + missionId + "': " + e.getMessage(), e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + allMissions.size() + " missions.");
    }

    public Mission getMission(String missionId) {
        return allMissions.get(missionId);
    }

    public Collection<Mission> getAllMissions() {
        return Collections.unmodifiableCollection(allMissions.values());
    }

    // Placeholder for future methods
    public List<Mission> getAvailableMissions(PlayerMissionData playerData) {
        // Logic to determine which missions are available based on dependencies, completions, cooldowns, permissions
        return allMissions.values().stream()
                .filter(mission -> !playerData.hasCompletedMission(mission.getId()) || !"NONE".equals(mission.getRepeatableType())) // crude filter for now
                .filter(mission -> !playerData.isMissionOnCooldown(mission.getId()))
                // Add dependency checks, permission checks etc.
                .collect(Collectors.toList());
    }

    public void startMission(PlayerMissionData playerData, String missionId) {
        Mission mission = getMission(missionId);
        if (mission != null) { // canStartMission should be called before this
            playerData.addActiveMission(mission);
            // Log or message player
        }
    }

    public boolean canStartMission(Player player, Mission mission) {
        if (mission == null) return false;
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());

        if (playerData.getActiveMissionProgress(mission.getId()) != null) {
            player.sendMessage(ChatColor.YELLOW + "You have already started the mission: " + mission.getName());
            return false;
        }

        if (playerData.hasCompletedMission(mission.getId()) && "NONE".equalsIgnoreCase(mission.getRepeatableType())) {
            player.sendMessage(ChatColor.YELLOW + "You have already completed the mission: " + mission.getName() + " and it's not repeatable.");
            return false;
        }

        if (playerData.isMissionOnCooldown(mission.getId())) {
            long remaining = (playerData.getCooldownEndTime(mission.getId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.YELLOW + "Mission '" + mission.getName() + "' is on cooldown. Time remaining: " + formatCooldown(remaining));
            return false;
        }

        if (mission.getDependencies() != null && !mission.getDependencies().isEmpty()) {
            for (String dependencyId : mission.getDependencies()) {
                if (!playerData.hasCompletedMission(dependencyId)) {
                    Mission dependencyMission = getMission(dependencyId);
                    String depName = dependencyMission != null ? dependencyMission.getName() : dependencyId;
                    player.sendMessage(ChatColor.RED + "You must complete the mission '" + depName + "' first.");
                    return false;
                }
            }
        }

        if (mission.getRequiredPermission() != null && !mission.getRequiredPermission().isEmpty() && !player.hasPermission(mission.getRequiredPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to start this mission.");
            return false;
        }
        return true;
    }

    public boolean startMission(Player player, Mission mission) {
        if (!canStartMission(player, mission)) {
            return false; // canStartMission already sent the message
        }
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        playerData.addActiveMission(mission); // This creates PlayerMissionProgress internally
        player.sendMessage(ChatColor.GREEN + "Mission '" + mission.getName() + "' started!");
        return true;
    }

    public void completeMission(Player player, Mission mission) {
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        if (mission == null || playerData.getActiveMissionProgress(mission.getId()) == null) {
            // Not active or doesn't exist, should not happen if logic is correct
            return;
        }

        playerData.markMissionCompleted(mission.getId(), mission); // This handles cooldowns internally

        MissionReward rewards = mission.getRewards();
        if (rewards != null) {
            if (rewards.getMoney() > 0 && plugin.getEconomy() != null) {
                plugin.getEconomy().depositPlayer(player, rewards.getMoney());
                player.sendMessage(ChatColor.GOLD + "You received " + rewards.getMoney() + " " + plugin.getEconomy().currencyNamePlural() + "!");
            }
            if (rewards.getExperience() > 0) {
                player.giveExp(rewards.getExperience()); // giveExp takes raw XP points
                player.sendMessage(ChatColor.AQUA + "You received " + rewards.getExperience() + " experience points!");
            }
            if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
                for (ItemStack item : rewards.getItems()) {
                    HashMap<Integer, ItemStack> unadded = player.getInventory().addItem(item.clone());
                    if (!unadded.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), unadded.get(0));
                        player.sendMessage(ChatColor.YELLOW + "Some items couldn't fit in your inventory and were dropped nearby!");
                    }
                }
                player.sendMessage(ChatColor.GREEN + "You received item rewards!");
            }
            if (rewards.getCommands() != null && !rewards.getCommands().isEmpty()) {
                for (String command : rewards.getCommands()) {
                    String processedCommand = command.replace("{player}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                }
            }
        }
        player.sendMessage(ChatColor.GREEN + "Mission '" + mission.getName() + "' completed! Rewards received.");
        // Optional: Broadcast
    }

    public void abandonMission(Player player, String missionId) {
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData.getActiveMissionProgress(missionId) != null) {
            playerData.removeActiveMission(missionId);
            Mission mission = getMission(missionId);
            player.sendMessage(ChatColor.YELLOW + "Mission '" + (mission != null ? mission.getName() : missionId) + "' abandoned.");
        } else {
            player.sendMessage(ChatColor.RED + "You are not currently on that mission.");
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
