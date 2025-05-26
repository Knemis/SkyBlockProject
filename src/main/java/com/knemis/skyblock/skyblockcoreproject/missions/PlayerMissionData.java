package com.knemis.skyblock.skyblockcoreproject.missions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerMissionData {
    private final UUID playerUuid;
    private final Map<String, PlayerMissionProgress> activeMissions;
    private final Set<String> completedMissions;
    private final Map<String, Long> missionCooldowns; // Key: missionId, Value: timestamp when cooldown expires

    public PlayerMissionData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.activeMissions = new HashMap<>();
        this.completedMissions = new HashSet<>();
        this.missionCooldowns = new HashMap<>();
    }

    // Constructor for loading from storage
    public PlayerMissionData(UUID playerUuid, Map<String, PlayerMissionProgress> activeMissions,
                             Set<String> completedMissions, Map<String, Long> missionCooldowns) {
        this.playerUuid = playerUuid;
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
        if (mission == null || activeMissions.containsKey(mission.getId()) || completedMissions.contains(mission.getId())) {
            return; // Or throw exception / log warning
        }
        if (isMissionOnCooldown(mission.getId())) {
            return; // Or inform player
        }
        activeMissions.put(mission.getId(), new PlayerMissionProgress(mission.getId(), mission.getObjectives().size()));
    }

    public void removeActiveMission(String missionId) {
        activeMissions.remove(missionId);
    }

    public Set<String> getCompletedMissions() {
        return new HashSet<>(completedMissions); // Return a copy
    }

    public boolean hasCompletedMission(String missionId) {
        return completedMissions.contains(missionId);
    }

    public void markMissionCompleted(String missionId, Mission mission) {
        activeMissions.remove(missionId);
        completedMissions.add(missionId);
        if (mission != null && !"NONE".equalsIgnoreCase(mission.getRepeatableType()) && mission.getCooldownHours() > 0) {
            long cooldownEndTime = System.currentTimeMillis() + (mission.getCooldownHours() * 60 * 60 * 1000L);
            missionCooldowns.put(missionId, cooldownEndTime);
        }
    }

    public Map<String, Long> getMissionCooldowns() {
        return new HashMap<>(missionCooldowns); // Return a copy
    }

    public boolean isMissionOnCooldown(String missionId) {
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
}
