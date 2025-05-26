package com.knemis.skyblock.skyblockcoreproject.missions;

import java.util.HashMap;
import java.util.Map;

public class PlayerMissionProgress {
    private final String missionId;
    private final Map<Integer, Integer> objectiveProgress; // Key: objective index, Value: current progress
    private final long startTime;

    public PlayerMissionProgress(String missionId, int totalObjectives) {
        this.missionId = missionId;
        this.objectiveProgress = new HashMap<>();
        for (int i = 0; i < totalObjectives; i++) {
            this.objectiveProgress.put(i, 0);
        }
        this.startTime = System.currentTimeMillis();
    }

    // Constructor for loading from storage (if needed, though typically part of PlayerMissionData deserialization)
    public PlayerMissionProgress(String missionId, Map<Integer, Integer> objectiveProgress, long startTime) {
        this.missionId = missionId;
        this.objectiveProgress = objectiveProgress;
        this.startTime = startTime;
    }

    public String getMissionId() {
        return missionId;
    }

    public Map<Integer, Integer> getObjectiveProgress() {
        return new HashMap<>(objectiveProgress); // Return a copy to maintain encapsulation
    }

    public int getProgress(int objectiveIndex) {
        return objectiveProgress.getOrDefault(objectiveIndex, 0);
    }

    public void setProgress(int objectiveIndex, int progress) {
        objectiveProgress.put(objectiveIndex, progress);
    }

    public void incrementProgress(int objectiveIndex, int amount) {
        objectiveProgress.put(objectiveIndex, objectiveProgress.getOrDefault(objectiveIndex, 0) + amount);
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isObjectiveCompleted(int objectiveIndex, MissionObjective objective) {
        return getProgress(objectiveIndex) >= objective.getAmount();
    }

    public boolean areAllObjectivesCompleted(Mission mission) {
        if (mission == null || !mission.getId().equals(this.missionId)) {
            return false; // Or throw an exception for mismatched mission
        }
        for (int i = 0; i < mission.getObjectives().size(); i++) {
            if (!isObjectiveCompleted(i, mission.getObjectives().get(i))) {
                return false;
            }
        }
        return true;
    }
}
