package com.knemis.skyblock.skyblockcoreproject.missions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerMissionData {
    private final UUID playerUuid;
    private final SkyBlockProject plugin; // Added plugin instance
    private final Map<String, PlayerMissionProgress> activeMissions;
    private final Set<String> completedMissions;
    private final Map<String, Long> missionCooldowns; // Key: missionId, Value: timestamp when cooldown expires

    public PlayerMissionData(UUID playerUuid, SkyBlockProject plugin) {
        System.out.println("[TRACE] Executing PlayerMissionData constructor for player " + playerUuid);
        this.playerUuid = playerUuid;
        this.plugin = plugin;
        this.activeMissions = new HashMap<>();
        this.completedMissions = new HashSet<>();
        this.missionCooldowns = new HashMap<>();
    }

    // Constructor for loading from storage
    public PlayerMissionData(UUID playerUuid, SkyBlockProject plugin, Map<String, PlayerMissionProgress> activeMissions,
                             Set<String> completedMissions, Map<String, Long> missionCooldowns) {
        this.playerUuid = playerUuid;
        this.plugin = plugin;
        this.activeMissions = activeMissions != null ? new HashMap<>(activeMissions) : new HashMap<>();
        this.completedMissions = completedMissions != null ? new HashSet<>(completedMissions) : new HashSet<>();
        this.missionCooldowns = missionCooldowns != null ? new HashMap<>(missionCooldowns) : new HashMap<>();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Map<String, PlayerMissionProgress> getActiveMissions() {
        return new HashMap<>(activeMissions); // Return a copy
    }

    public PlayerMissionProgress getActiveMissionProgress(String missionId) {
        return activeMissions.get(missionId);
    }

    public void addActiveMission(Mission mission) {
        System.out.println("[TRACE] Executing addActiveMission for mission " + (mission != null ? mission.getId() : "null") + " for player " + this.playerUuid);
        if (mission == null || activeMissions.containsKey(mission.getId()) || completedMissions.contains(mission.getId())) {
            return; // Or throw exception / log warning
        }
        if (isMissionOnCooldown(mission.getId())) {
            return; // Or inform player
        }
        activeMissions.put(mission.getId(), new PlayerMissionProgress(mission.getId(), mission.getObjectives().size()));
    }

    public void removeActiveMission(String missionId) {
        System.out.println("[TRACE] Executing removeActiveMission for missionId " + missionId + " for player " + this.playerUuid);
        activeMissions.remove(missionId);
    }

    public Set<String> getCompletedMissions() {
        return new HashSet<>(completedMissions); // Return a copy
    }

    public boolean hasCompletedMission(String missionId) {
        return completedMissions.contains(missionId);
    }

    public void markMissionCompleted(String missionId, Mission mission) {
        System.out.println("[TRACE] Executing markMissionCompleted for missionId " + missionId + " and mission " + (mission != null ? mission.getId() : "null") + " for player " + this.playerUuid);
        activeMissions.remove(missionId);
        completedMissions.add(missionId);

        if (mission != null && !"NONE".equalsIgnoreCase(mission.getRepeatableType()) && mission.getCooldownHours() > 0) {
            boolean isOwner = false;
            Player player = Bukkit.getPlayer(this.playerUuid);

            if (player != null && this.plugin != null && this.plugin.getLuckPermsApi() != null) {
                LuckPerms luckPerms = this.plugin.getLuckPermsApi();
                try {
                    User lpUser = luckPerms.getUserManager().getUser(player.getUniqueId());
                    if (lpUser != null) {
                        String primaryGroup = lpUser.getPrimaryGroup();
                        if (primaryGroup != null && primaryGroup.equalsIgnoreCase("owner")) {
                            isOwner = true;
                        }
                    }
                } catch (Exception e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error checking LuckPerms group for player " + player.getName() + " in PlayerMissionData (markMissionCompleted)", e);
                }
            } else {
                if (this.plugin == null) {
                    Bukkit.getLogger().warning("[SkyBlockCoreProject] PlayerMissionData.plugin is null, cannot check LuckPerms for mission cooldown bypass.");
                } else if (this.plugin.getLuckPermsApi() == null) {
                    this.plugin.getLogger().warning("LuckPerms API not available for mission cooldown bypass check for player " + (player != null ? player.getName() : playerUuid.toString()));
                }
            }

            if (!isOwner) {
                long cooldownEndTime = System.currentTimeMillis() + (mission.getCooldownHours() * 60 * 60 * 1000L);
                missionCooldowns.put(missionId, cooldownEndTime);
            } else {
                if (this.plugin != null) { // Check plugin not null before logging
                    this.plugin.getLogger().info("Mission cooldown for " + missionId + " bypassed for owner " + (player != null ? player.getName() : playerUuid.toString()));
                }
            }
        }
    }

    public Map<String, Long> getMissionCooldowns() {
        return new HashMap<>(missionCooldowns); // Return a copy
    }

    public boolean isMissionOnCooldown(String missionId) {
        if (!missionCooldowns.containsKey(missionId)) {
            return false;
        }

        boolean isOwner = false;
        Player player = Bukkit.getPlayer(this.playerUuid);

        if (player != null && this.plugin != null && this.plugin.getLuckPermsApi() != null) {
            LuckPerms luckPerms = this.plugin.getLuckPermsApi();
            try {
                User lpUser = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (lpUser != null) {
                    String primaryGroup = lpUser.getPrimaryGroup();
                    if (primaryGroup != null && primaryGroup.equalsIgnoreCase("owner")) {
                        isOwner = true;
                    }
                }
            } catch (Exception e) {
                 this.plugin.getLogger().log(Level.SEVERE, "Error checking LuckPerms group for player " + player.getName() + " in PlayerMissionData (isMissionOnCooldown)", e);
            }
        } else {
            if (this.plugin == null) {
                 Bukkit.getLogger().warning("[SkyBlockCoreProject] PlayerMissionData.plugin is null, cannot check LuckPerms for mission cooldown bypass (isMissionOnCooldown).");
            } else if (this.plugin.getLuckPermsApi() == null && player != null) { // Avoid spam if player is offline
                 this.plugin.getLogger().warning("LuckPerms API not available for mission cooldown bypass check (isMissionOnCooldown) for player " + player.getName());
            }
        }

        if (isOwner) {
            // Optional: Log bypass for debugging, but can be spammy
            // if (this.plugin != null) {
            //     this.plugin.getLogger().info("Mission cooldown check for " + missionId + " bypassed for owner " + player.getName());
            // }
            return false; // Owners are never on cooldown
        }

        Long cooldownEndTime = missionCooldowns.get(missionId);
        return cooldownEndTime != null && System.currentTimeMillis() < cooldownEndTime;
    }

    public long getCooldownEndTime(String missionId) {
        return missionCooldowns.getOrDefault(missionId, 0L);
    }

    public void updateObjectiveProgress(String missionId, int objectiveIndex, int amount) {
        PlayerMissionProgress progress = activeMissions.get(missionId);
        if (progress != null) {
            progress.incrementProgress(objectiveIndex, amount);
            // Potentially check for mission completion here or let MissionManager handle it
        }
    }

    public void removeCompletedMission(String missionId) {
        System.out.println("[TRACE] Executing removeCompletedMission for missionId " + missionId + " for player " + this.playerUuid);
        completedMissions.remove(missionId);
    }

    public void removeMissionCooldown(String missionId) {
        System.out.println("[TRACE] Executing removeMissionCooldown for missionId " + missionId + " for player " + this.playerUuid);
        missionCooldowns.remove(missionId);
    }

    public void resetMission(String missionId) {
        System.out.println("[TRACE] Executing resetMission for missionId " + missionId + " for player " + this.playerUuid);
        removeActiveMission(missionId);
        removeCompletedMission(missionId);
        removeMissionCooldown(missionId);
    }
}
