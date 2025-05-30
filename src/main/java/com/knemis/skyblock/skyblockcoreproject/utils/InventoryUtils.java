package com.knemis.skyblock.skyblockcoreproject.utils;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject; // Not strictly needed if plugin instance is passed
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Objects;
// No NBT API from Bukkit directly, for complex NBT matching, external libraries or NMS would be needed.
// For this scope, NBT matching will be very basic or placeholder.

/**
 * Utility class for inventory operations.
 */
public class InventoryUtils {

    /**
     * Checks if the inventory has enough space to add a specific amount of an item.
     * This method considers existing stacks of the same item and empty slots.
     *
     * @param inventory   The inventory to check.
     * @param itemToAdd   The ItemStack prototype of the item to add (material, meta for matching similar items).
     * @param plugin      Reference to the main plugin for logging (can be null if no logging needed).
     * @param amountToAdd The total number of items intended to be added.
     * @return True if there is enough space for the specified amount, false otherwise.
     */
    public static boolean hasEnoughSpace(Inventory inventory, ItemStack itemToAdd, @Nullable JavaPlugin plugin, int amountToAdd) {
        if (inventory == null || itemToAdd == null || itemToAdd.getType() == Material.AIR || amountToAdd <= 0) {
            return true; // Adding nothing, or invalid input, considered as "enough space".
        }

        int maxStackSize = itemToAdd.getMaxStackSize();
        int spaceAvailable = 0;

        for (ItemStack slotItem : inventory.getStorageContents()) { // getStorageContents() excludes armor, offhand, etc.
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                spaceAvailable += maxStackSize;
            } else if (slotItem.isSimilar(itemToAdd)) { // isSimilar checks type, durability, and most meta
                spaceAvailable += Math.max(0, maxStackSize - slotItem.getAmount());
            }
            // Optimization: if spaceAvailable is already enough, no need to check further.
            if (spaceAvailable >= amountToAdd) {
                return true;
            }
        }
        return spaceAvailable >= amountToAdd;
    }

    /**
     * A simpler version of hasEnoughSpace for a single ItemStack instance.
     * This is useful when you have a fully defined ItemStack (e.g., with NBT, specific amount)
     * and want to see if it can fit.
     *
     * @param inventory The inventory to check.
     * @param singleItemStack The complete ItemStack to add.
     * @return True if the singleItemStack can fit, false otherwise.
     */
    public static boolean hasEnoughSpace(Inventory inventory, ItemStack singleItemStack) {
        if (inventory == null || singleItemStack == null || singleItemStack.getType() == Material.AIR) {
            return true;
        }
        return inventory.addItem(singleItemStack.clone()).isEmpty(); // addItem returns a map of items that couldn't fit.
    }


    /**
     * Counts items in an inventory that match the specified criteria.
     * NBT and display name matching are currently placeholders or very basic.
     *
     * @param inventory          The inventory to scan.
     * @param material           The material to match.
     * @param nbtStringMatcher   (Placeholder) String for NBT matching logic (e.g., JSON string).
     * @param displayNameMatcher (Basic) String for display name matching (checks exact match, ignoring color).
     * @param plugin             Reference to the main plugin for logging (can be null).
     * @return The total count of matching items.
     */
    public static int countItems(Inventory inventory, Material material, @Nullable String nbtStringMatcher, @Nullable String displayNameMatcher, @Nullable JavaPlugin plugin) {
        if (inventory == null || material == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            if (item != null && item.getType() == material) {
                boolean match = true;

                // Display Name Matching (if specified)
                if (displayNameMatcher != null) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        // Compare stripped color names
                        if (!ChatUtils.stripColor(item.getItemMeta().getDisplayName()).equals(ChatUtils.stripColor(displayNameMatcher))) {
                            match = false;
                        }
                    } else {
                        match = false; // Item doesn't have a display name, but matcher expects one
                    }
                }

                // NBT Matching (Basic for Enchanted Books - StoredEnchantments)
                // This is a very simplified example. Robust NBT comparison is complex.
                if (match && nbtStringMatcher != null && !nbtStringMatcher.isEmpty() && item.getType() == Material.ENCHANTED_BOOK) {
                    // This would require parsing nbtStringMatcher and comparing with item's NBT.
                    // For example, if nbtStringMatcher is '{StoredEnchantments:[{id:"minecraft:sharpness",lvl:1s}]}'
                    // you'd need to check if the item has that specific stored enchantment.
                    // This is non-trivial with Bukkit API alone for arbitrary NBT.
                    // For AdminShop, the sourceItem in ShopItem already has NBT, so direct .isSimilar() might be better if applicable.
                    // However, for selling, player items might have *additional* NBT.
                    // As a placeholder, we are not deeply matching NBT here.
                    // A real implementation might involve comparing specific NBT tags if a library is not used.
                    // if (plugin != null) plugin.getLogger().finer("[InventoryUtils] NBT matching for ENCHANTED_BOOK is complex and not fully implemented for selling count.");
                } else if (match && nbtStringMatcher != null && !nbtStringMatcher.isEmpty()) {
                    // Placeholder for other item types NBT matching
                    // if (plugin != null) plugin.getLogger().finer("[InventoryUtils] Generic NBT matching is not implemented for selling count.");
                }


                if (match) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    /**
     * Removes a specified amount of items matching the criteria from the inventory.
     * Matching logic is similar to countItems (currently basic).
     *
     * @param inventory           The inventory to remove from.
     * @param material            The material to match.
     * @param totalAmountToRemove The total amount to remove.
     * @param nbtStringMatcher    (Placeholder) String for NBT matching.
     * @param displayNameMatcher  (Basic) String for display name matching.
     * @param plugin              Reference to the main plugin for logging (can be null).
     * @return True if the full amount was successfully removed, false otherwise.
     */
    public static boolean removeItems(Inventory inventory, Material material, int totalAmountToRemove, @Nullable String nbtStringMatcher, @Nullable String displayNameMatcher, @Nullable JavaPlugin plugin) {
        if (inventory == null || material == null || totalAmountToRemove <= 0) {
            return true; // Nothing to remove or invalid input.
        }

        int amountRemoved = 0;
        ItemStack[] contents = inventory.getStorageContents(); // Iterate over a copy or direct array

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                boolean match = true;

                // Display Name Matching
                if (displayNameMatcher != null) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        if (!ChatUtils.stripColor(item.getItemMeta().getDisplayName()).equals(ChatUtils.stripColor(displayNameMatcher))) {
                            match = false;
                        }
                    } else {
                        match = false;
                    }
                }

                // NBT Matching (Placeholder - see countItems)
                if (match && nbtStringMatcher != null && !nbtStringMatcher.isEmpty()) {
                    // NBT matching logic would go here. For now, it's a placeholder.
                }

                if (match) {
                    int amountInStack = item.getAmount();
                    int amountToTakeFromStack = Math.min(amountInStack, totalAmountToRemove - amountRemoved);

                    if (amountToTakeFromStack >= amountInStack) {
                        inventory.setItem(i, null); // Remove the whole stack
                    } else {
                        item.setAmount(amountInStack - amountToTakeFromStack);
                        // inventory.setItem(i, item); // Not needed if 'item' is a direct reference from getStorageContents() that modifies the original
                    }
                    amountRemoved += amountToTakeFromStack;

                    if (amountRemoved >= totalAmountToRemove) {
                        return true; // All items removed
                    }
                }
            }
        }

        if (amountRemoved < totalAmountToRemove && plugin != null) {
            plugin.getLogger().warning("[InventoryUtils] Could not remove all " + totalAmountToRemove + " of " + material + ". Actually removed: " + amountRemoved);
        }
        return amountRemoved >= totalAmountToRemove;
    }
}
