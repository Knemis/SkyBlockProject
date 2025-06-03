package com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.common.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class SkyBlockProjectInventory {
    public abstract Inventory getTopInventory(Player player);
}
