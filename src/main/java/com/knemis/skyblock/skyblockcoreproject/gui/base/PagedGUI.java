package com.knemis.skyblock.skyblockcoreproject.gui.base;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import java.util.Collection; // Correct import
import java.util.HashMap; // For items map

public abstract class PagedGUI<T> implements InventoryHolder {
    protected final Player player;
    protected int page;
    protected final int size;
    protected final ItemStack backgroundItem;
    protected final ItemStack previousPageItem;
    protected final ItemStack nextPageItem;
    protected final ItemStack backButton;
    protected final HashMap<Integer, T> items; // To store items by slot for click handling

    public PagedGUI(int initialPage, int size, ItemStack backgroundItem, ItemStack previousPageItem, ItemStack nextPageItem, Player player, ItemStack backButton) {
        this.page = initialPage > 0 ? initialPage : 1; // Ensure page is at least 1
        this.size = size;
        this.backgroundItem = backgroundItem;
        this.previousPageItem = previousPageItem;
        this.nextPageItem = nextPageItem;
        this.player = player;
        this.backButton = backButton;
        this.items = new HashMap<>();
    }

    protected int getSize() { return this.size; }

    protected void addContent(Inventory inventory) {
        // Fill background if item is provided
        if (backgroundItem != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, backgroundItem.clone()); // Clone to avoid issues with shared item stacks
            }
        }
        // Actual item population for the page is done by subclasses in getInventory()
        // Then, navigation buttons are added on top.
        if (backButton != null && getBackButtonSlot() != -1 && getBackButtonSlot() < inventory.getSize()) {
            inventory.setItem(getBackButtonSlot(), backButton);
        }
        if (previousPageItem != null && getPreviousButtonSlot() != -1 && getPreviousButtonSlot() < inventory.getSize()) {
            inventory.setItem(getPreviousButtonSlot(), previousPageItem);
        }
        if (nextPageItem != null && getNextButtonSlot() != -1 && getNextButtonSlot() < inventory.getSize()) {
            inventory.setItem(getNextButtonSlot(), nextPageItem);
        }
    }

    public abstract Collection<T> getPageObjects();
    public abstract ItemStack getItemStack(T object);
    public boolean isPaged() { return true;}

    public T getItem(int slot) { return items.get(slot); }

    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        int clickedSlot = event.getSlot();

        if (backButton != null && clickedSlot == getBackButtonSlot()) {
            // Default back action: close inventory. Subclasses can override for different behavior.
            player.closeInventory();
            // Or, if a previousGUI system is implemented:
            // if (this instanceof BackGUI && ((BackGUI)this).getPreviousGUI() != null) {
            //    player.openInventory(((BackGUI)this).getPreviousGUI().getInventory());
            // } else { player.closeInventory(); }
            return;
        }

        int totalObjects = getPageObjects().size();
        int itemsPerPage = size - 9; // Assuming 9 slots are for navigation/border
        if (itemsPerPage <=0) itemsPerPage = 1; // Avoid division by zero or negative
        int totalPages = (int) Math.ceil((double) totalObjects / itemsPerPage);
        if (totalPages == 0) totalPages = 1;


        if (previousPageItem != null && clickedSlot == getPreviousButtonSlot()) {
            if (page > 1) {
                page--;
                player.openInventory(getInventory()); // Re-open to refresh
            }
            return;
        }

        if (nextPageItem != null && clickedSlot == getNextButtonSlot()) {
            if (page < totalPages) {
                page++;
                player.openInventory(getInventory()); // Re-open to refresh
            }
            return;
        }

        // Further click handling for specific items in the GUI should be done by subclasses
        // by overriding this method and calling super.onInventoryClick(event) first.
    }

    protected int getPreviousButtonSlot() { return size > 8 ? size - 9 : -1; }
    protected int getNextButtonSlot() { return size > 0 ? size - 1 : -1; }
    protected int getBackButtonSlot() { return size > 4 ? size - 5 : -1; }

    @Override
    public abstract Inventory getInventory();
}
