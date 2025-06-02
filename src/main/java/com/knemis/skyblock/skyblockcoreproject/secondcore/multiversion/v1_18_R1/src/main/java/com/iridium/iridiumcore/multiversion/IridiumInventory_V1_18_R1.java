package com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.v1_18_R1.src.main.java.com.iridium.iridiumcore.multiversion;

import com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.common.src.main.java.com.iridium.iridiumcore.multiversion.SkyBlockProjectInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockProjectInventory_V1_18_R1 extends SkyBlockProjectInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
