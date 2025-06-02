package com.keviin.keviincore.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class keviinInventory_V1_18_R1 extends keviinInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
