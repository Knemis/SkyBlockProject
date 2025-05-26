package com.knemis.skyblock.skyblockcoreproject.missions;

import java.util.List;
import java.util.Map;

public class Mission {
    private final String id;
    private final String name;
    private final List<String> description;
    private final String category;
    private final String iconMaterial;
    private final List<MissionObjective> objectives;
    private final MissionReward rewards;
    private final List<String> dependencies;
    private final String repeatableType; // Consider Enum: NONE, COOLDOWN, DAILY, WEEKLY
    private final int cooldownHours;
    private final String requiredPermission;

    public Mission(String id, String name, List<String> description, String category,
                   String iconMaterial, List<MissionObjective> objectives, MissionReward rewards,
                   List<String> dependencies, String repeatableType, int cooldownHours, String requiredPermission) {
        this.id = id;
        this.name = name;
        this.description = description; // Should be immutable if passed directly
        this.category = category;
        this.iconMaterial = iconMaterial;
        this.objectives = objectives; // Should be immutable
        this.rewards = rewards;
        this.dependencies = dependencies; // Should be immutable
        this.repeatableType = repeatableType;
        this.cooldownHours = cooldownHours;
        this.requiredPermission = requiredPermission;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description; // Consider returning a copy
    }

    public String getCategory() {
        return category;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public List<MissionObjective> getObjectives() {
        return objectives; // Consider returning a copy
    }

    public MissionReward getRewards() {
        return rewards;
    }

    public List<String> getDependencies() {
        return dependencies; // Consider returning a copy
    }

    public String getRepeatableType() {
        return repeatableType;
    }

    public int getCooldownHours() {
        return cooldownHours;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}
