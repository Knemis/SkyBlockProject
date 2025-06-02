package com.keviin.keviincore.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class keviinInventory_V1_13_R2 extends keviinInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
