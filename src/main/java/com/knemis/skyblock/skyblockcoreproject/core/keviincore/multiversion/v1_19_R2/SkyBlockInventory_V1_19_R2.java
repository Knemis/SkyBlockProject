package com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R2;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.SkyBlockInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockInventory_V1_19_R2 extends SkyBlockInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
