package com.keviin.keviincore.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class keviinInventoryDefault extends keviinInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
