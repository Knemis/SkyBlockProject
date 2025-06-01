package com.knemis.skyblock.skyblockcoreproject.iridiumcore.corefeatures.multiversion.v1_16_R3.corefeatures.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_16_R3 extends IridiumInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}