package com.knemis.skyblock.skyblockcoreproject.gui.base;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class BackGUI implements InventoryHolder {
    protected final Player player;
    protected final ItemStack backgroundItem;
    protected final ItemStack backButton;
    // protected final InventoryHolder previousGUI; // Changed type to InventoryHolder for broader compatibility

    // Constructor for GUIs that might have a previous GUI to go back to.
    public BackGUI(ItemStack backgroundItem, Player player, ItemStack backButton /*, InventoryHolder previousGUI */) {
        this.backgroundItem = backgroundItem;
        this.player = player;
        this.backButton = backButton;
        // this.previousGUI = previousGUI;
    }

    protected void addContent(Inventory inventory) {
        // Fill background if item is provided
        if (backgroundItem != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, backgroundItem.clone());
            }
        }
        // Add back button
        if (backButton != null && getBackButtonSlot() != -1 && getBackButtonSlot() < inventory.getSize()) {
            inventory.setItem(getBackButtonSlot(), backButton);
        }
    }

    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int clickedSlot = event.getSlot();

        if (backButton != null && clickedSlot == getBackButtonSlot()) {
            // Default back action: close inventory.
            // Subclasses or instances with a previousGUI would override or handle differently.
            // if (previousGUI != null) {
            //    player.openInventory(previousGUI.getInventory());
            // } else {
            player.closeInventory();
            // }
        }
        // Other item clicks handled by subclasses.
    }

    // Example, actual slot would be from config in SkyBlockFeatureManager or layout manager
    // Typically bottom-left (inventory.getSize() - 9) or bottom-center (inventory.getSize() - 5)
    protected int getBackButtonSlot() { return 0; }

    // public InventoryHolder getPreviousGUI() { return previousGUI; }

    @Override
    public abstract Inventory getInventory();
}
