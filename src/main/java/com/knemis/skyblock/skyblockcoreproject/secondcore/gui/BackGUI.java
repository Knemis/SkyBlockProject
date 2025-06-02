package com.knemis.skyblock.skyblockcoreproject.secondcore.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Background;
import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import com.knemis.skyblock.skyblockcoreproject.secondcore.SkyBlockProjectSecondCore;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.InventoryUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public abstract class BackGUI implements GUI {
    private final Background background;
    private final Inventory previousInventory;
    private final Item backButton;

    public BackGUI(Background background, Player player, Item backButton) {
        this.background = background;
        this.backButton = backButton;

        if (player == null) {
            this.previousInventory = null;
        } else {
            Inventory previousInventory = SkyBlockProjectSecondCore.getInstance().getSkyBlockProjectInventory().getTopInventory(player);
            this.previousInventory = previousInventory.getType() == InventoryType.CHEST ? previousInventory : null;
        }
    }

    @Override
    public void addContent(Inventory inventory) {
        InventoryUtils.fillInventory(inventory, background);
        if (previousInventory != null) {
            inventory.setItem(inventory.getSize() + backButton.slot, ItemStackUtils.makeItem(backButton));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (previousInventory != null && event.getSlot() == (event.getInventory().getSize() + backButton.slot)) {
            event.getWhoClicked().openInventory(previousInventory);
        }
    }
}
