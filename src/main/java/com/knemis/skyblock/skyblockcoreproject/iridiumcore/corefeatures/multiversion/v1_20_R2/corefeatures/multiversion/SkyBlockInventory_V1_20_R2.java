package com.knemis.skyblock.skyblockcoreproject.iridiumcore.corefeatures.multiversion.v1_20_R2.corefeatures.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_20_R2 extends IridiumInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}