package com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R1;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.SkyBlockInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_20_R1 extends SkyBlockInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
