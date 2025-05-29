package com.knemis.skyblock.skyblockcoreproject.shop;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ShopInventoryManager {

    /**
     * Checks if the player has enough space in their inventory to receive the given item.
     *
     * @param player        The player whose inventory is to be checked.
     * @param itemToReceive The ItemStack to be received.
     * @return True if the player has enough space, false otherwise.
     */
    public static boolean hasEnoughSpace(Player player, ItemStack itemToReceive) {
        if (player == null || itemToReceive == null || itemToReceive.getType() == Material.AIR || itemToReceive.getAmount() <= 0) {
            return true; // No item to receive or invalid parameters means "enough space".
        }
        Inventory inv = player.getInventory();
        int amountNeeded = itemToReceive.getAmount();
        for (ItemStack slotItem : inv.getStorageContents()) { // Only checks main storage, not armor/offhand
            if (amountNeeded <= 0) {
                break;
            }
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                amountNeeded -= itemToReceive.getMaxStackSize();
            } else if (slotItem.isSimilar(itemToReceive)) {
                amountNeeded -= (Math.max(0, itemToReceive.getMaxStackSize() - slotItem.getAmount()));
            }
        }
        return amountNeeded <= 0;
    }

    /**
     * Removes a specified amount of items similar to the template from a chest.
     *
     * @param chest                 The chest to remove items from.
     * @param templateItemToRemove The ItemStack template to match (ignores amount).
     * @param amountToRemove       The total amount of items to remove.
     * @return True if the specified amount was successfully removed, false otherwise.
     */
    public static boolean removeItemsFromChest(Chest chest, ItemStack templateItemToRemove, int amountToRemove) {
        if (chest == null || templateItemToRemove == null || templateItemToRemove.getType() == Material.AIR || amountToRemove <= 0) {
            return false;
        }
        Inventory chestInventory = chest.getInventory();
        if (countItemsInChest(chest, templateItemToRemove) < amountToRemove) {
            return false; // Not enough items to remove
        }
        int removedCount = 0;
        ItemStack[] contents = chestInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemInSlot = contents[i];
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToRemove)) {
                int amountInSlot = itemInSlot.getAmount();
                int canRemoveFromSlot = Math.min(amountToRemove - removedCount, amountInSlot);
                itemInSlot.setAmount(amountInSlot - canRemoveFromSlot);
                removedCount += canRemoveFromSlot;
                if (itemInSlot.getAmount() <= 0) {
                    contents[i] = null; // Clear the slot if empty
                }
                if (removedCount >= amountToRemove) {
                    break;
                }
            }
        }
        chestInventory.setContents(contents); // Update the inventory
        return removedCount >= amountToRemove;
    }

    /**
     * Counts items similar to the template item within a chest.
     *
     * @param chest               The chest to count items in.
     * @param templateItemToMatch The ItemStack template to match (ignores amount).
     * @return The total count of matching items.
     */
    public static int countItemsInChest(Chest chest, ItemStack templateItemToMatch) {
        int count = 0;
        if (chest == null || templateItemToMatch == null || templateItemToMatch.getType() == Material.AIR) {
            return 0;
        }
        Inventory chestInventory = chest.getInventory();
        for (ItemStack itemInSlot : chestInventory.getContents()) {
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToMatch)) {
                count += itemInSlot.getAmount();
            }
        }
        return count;
    }

    /**
     * Counts items similar to the template item in a player's inventory.
     *
     * @param player       The player whose inventory is to be checked.
     * @param templateItem The ItemStack template to match (ignores amount).
     * @return The total count of matching items in the player's inventory.
     */
    public static int countItemsInInventory(Player player, ItemStack templateItem) {
        if (player == null || templateItem == null || templateItem.getType() == Material.AIR) {
            return 0;
        }
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) { // Checks all contents including armor/offhand
            if (item != null && item.isSimilar(templateItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Removes a specified amount of items similar to the template from a player's inventory.
     *
     * @param player          The player to remove items from.
     * @param templateItem    The ItemStack template to match for removal.
     * @param amountToRemove The total amount of items to remove.
     * @return True if the specified amount was successfully removed, false otherwise.
     */
    public static boolean removeItemsFromInventory(Player player, ItemStack templateItem, int amountToRemove) {
        if (player == null || templateItem == null || templateItem.getType() == Material.AIR || amountToRemove <= 0) {
            return false;
        }
        // Ensure we have enough to remove first
        if (countItemsInInventory(player, templateItem) < amountToRemove) {
            return false;
        }
        ItemStack itemToRemoveCloned = templateItem.clone();
        itemToRemoveCloned.setAmount(amountToRemove);
        HashMap<Integer, ItemStack> didNotRemove = player.getInventory().removeItem(itemToRemoveCloned);
        return didNotRemove.isEmpty();
    }

    /**
     * Checks if there is enough space in a chest to add a given quantity of an item.
     *
     * @param chest         The chest to check.
     * @param itemToAdd     The ItemStack template of the item to be added.
     * @param quantityToAdd The quantity of the item to be added.
     * @return True if there is enough space, false otherwise.
     */
    public static boolean hasEnoughSpaceInChest(Chest chest, ItemStack itemToAdd, int quantityToAdd) {
        if (chest == null || itemToAdd == null || itemToAdd.getType() == Material.AIR || quantityToAdd <= 0) {
            return false; // Or true if 0 quantity means "enough space"
        }
        Inventory chestInventory = chest.getInventory();
        int maxStackSize = itemToAdd.getMaxStackSize();
        int remainingToAdd = quantityToAdd;

        for (ItemStack slotItem : chestInventory.getStorageContents()) {
            if (remainingToAdd <= 0) {
                break;
            }
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                remainingToAdd -= maxStackSize;
            } else if (slotItem.isSimilar(itemToAdd)) {
                int spaceInSlot = maxStackSize - slotItem.getAmount();
                remainingToAdd -= spaceInSlot;
            }
        }
        return remainingToAdd <= 0;
    }
}
