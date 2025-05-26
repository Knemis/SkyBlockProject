package com.knemis.skyblock.skyblockcoreproject.missions;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MissionReward {
    private final double money;
    private final int experience;
    private final List<ItemStack> items; // Actual ItemStacks, parsing will be handled during config load
    private final List<String> commands;

    public MissionReward(double money, int experience, List<ItemStack> items, List<String> commands) {
        this.money = money;
        this.experience = experience;
        this.items = items; // Should be an immutable list if passed directly
        this.commands = commands; // Should be an immutable list if passed directly
    }

    public double getMoney() {
        return money;
    }

    public int getExperience() {
        return experience;
    }

    public List<ItemStack> getItems() {
        return items; // Consider returning a copy for immutability: Collections.unmodifiableList(new ArrayList<>(items))
    }

    public List<String> getCommands() {
        return commands; // Consider returning a copy for immutability
    }
}
