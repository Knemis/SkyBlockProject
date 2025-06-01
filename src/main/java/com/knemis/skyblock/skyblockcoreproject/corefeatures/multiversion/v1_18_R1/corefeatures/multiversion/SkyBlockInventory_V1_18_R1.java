package com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R1.corefeatures.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_18_R1 extends IridiumInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}