package com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R1.multiversion.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_16_R1 extends IridiumInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}