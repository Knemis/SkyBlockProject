package com.knemis.skyblock.skyblockcoreproject.missions;

public class MissionObjective {
    private final String type;
    private final String target;
    private final int amount;
    private final String displayNameOverride; // Optional

    // Current progress is typically transient or managed elsewhere (e.g., PlayerMissionData)
    // private transient int currentProgress;

    public MissionObjective(String type, String target, int amount, String displayNameOverride) {
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.displayNameOverride = displayNameOverride;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplayNameOverride() {
        return displayNameOverride;
    }

    // If currentProgress were managed here:
    // public int getCurrentProgress() { return currentProgress; }
    // public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }
    // public void incrementProgress(int amount) { this.currentProgress += amount; }
}
