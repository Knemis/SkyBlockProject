package com.keviin.keviincore.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class keviinInventory_V1_20_R4 extends keviinInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
