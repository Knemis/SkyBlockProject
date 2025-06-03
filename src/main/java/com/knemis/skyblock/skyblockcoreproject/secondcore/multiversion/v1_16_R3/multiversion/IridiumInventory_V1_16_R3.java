package com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.v1_16_R3.multiversion;

import com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.common.multiversion.SkyBlockProjectInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockProjectInventory_V1_16_R3 extends SkyBlockProjectInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
