package com.knemis.skyblock.skyblockcoreproject.missions;

public enum MissionCategory {
    AVAILABLE("Available"),
    ACTIVE("Active"),
    COMPLETED("Completed");

    private final String displayName;

    MissionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
