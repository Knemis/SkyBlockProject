package com.knemis.skyblock.skyblockcoreproject.shop.admin;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager; // Corrected import path
import com.knemis.skyblock.skyblockcoreproject.shop.ShopInventoryManager;
import org.bukkit.NamespacedKey; // Added import
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;
// import com.knemis.skyblock.skyblockcoreproject.utils.InventoryUtils; // To be created
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer; // Added import
import org.bukkit.persistence.PersistentDataType; // Added import

import java.util.List; // Added import for List
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminShopListener implements Listener {

    private final SkyBlockProject plugin; // plugin instance may still be needed for logging or other non-InventoryUtils tasks. Keep for now.
    private final AdminShopGUIManager shopGUIManager;
    // EconomyManager will be used statically via its static methods.

    // To store current category view for pagination
    // This map holds the last category and page a player was viewing.
    private final Map<UUID, CurrentCategoryView> playerCategoryView = new HashMap<>();
    private final NamespacedKey itemKeyPDC; // Added field

    // Navigation item names - ensure these exactly match the display names set in AdminShopGUIManager
    // These are translated with ChatUtils.translateAlternateColorCodes before comparison.
    private static final String NAV_BACK_NAME_RAW = "&e&lBack to Categories";
    private static final String NAV_NEXT_PAGE_NAME_RAW = "&a&lNext Page";
    private static final String NAV_PREVIOUS_PAGE_NAME_RAW = "&a&lPrevious Page";
    private static final String NAV_PAGE_INFO_IDENTIFIER_RAW = "&fPage "; // Used to identify page info item if needed, though direct click isn't handled.

    /**
     * Helper class to store the category and page a player is currently viewing.
     */
    private static class CurrentCategoryView {
        final AdminShopGUIManager.ShopCategory category;
        final int page;

        CurrentCategoryView(AdminShopGUIManager.ShopCategory category, int page) {
            this.category = category;
            this.page = page;
        }
    }

    /**
     * Constructor for AdminShopListener.
     * @param plugin The main SkyBlockProject plugin instance.
     * @param shopGUIManager The AdminShopGUIManager instance.
     */
    public AdminShopListener(SkyBlockProject plugin, AdminShopGUIManager shopGUIManager) {
        this.plugin = plugin;
        this.shopGUIManager = shopGUIManager;
        this.itemKeyPDC = new NamespacedKey(plugin, "admin_shop_item_internal_name"); // Initialize in constructor
    }

    /**
     * Handles clicks within Admin Shop GUIs.
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) {
            return;
        }

        String viewTitle = event.getView().getTitle(); // Already color-translated by Bukkit
        AdminShopGUIManager.ShopCategory currentCategoryFromTitle = null;

        boolean isMainShop = viewTitle.equals(shopGUIManager.getMainShopTitle());
        if (!isMainShop) {
            // Try to identify category by GUI title
            currentCategoryFromTitle = shopGUIManager.getCategoryByTitle(viewTitle);
        }

        // If the title doesn't match main shop or any known category shop, it's not our GUI.
        if (!isMainShop && currentCategoryFromTitle == null) {
            return;
        }

        event.setCancelled(true); // Cancel all clicks in our GUIs by default.

        // Ignore clicks in player's own inventory.
        if (clickedInventory != event.getView().getTopInventory()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return; // Clicked on an empty slot or invalid item.
        }

        if (isMainShop) {
            handleMainShopGUIClick(player, clickedItem);
        } else if (currentCategoryFromTitle != null) {
            // For category GUI, retrieve the current view context (category and page)
            CurrentCategoryView view = playerCategoryView.get(player.getUniqueId());
            if (view == null || view.category != currentCategoryFromTitle) {
                // This might happen if the map was cleared or player somehow opened a category GUI without going through openCategoryGUI properly.
                // Or, if title matching is somehow inconsistent.
                // Defaulting to page 1 of the category identified by title.
                plugin.getLogger().warning("AdminShopListener: Player " + player.getName() + " clicked in category '" +
                                           currentCategoryFromTitle.getDisplayName() + "' but view info was missing or mismatched. Assuming page 1.");
                view = new CurrentCategoryView(currentCategoryFromTitle, 1);
                // Store it, as AdminShopGUIManager.openCategoryGUI would have.
                playerCategoryView.put(player.getUniqueId(), view);
            }
            handleCategoryGUIClick(player, clickedItem, view.category, event.getSlot(), event.getClick(), view);
        }
    }

    /**
     * Handles clicks in the main shop GUI (category selection).
     * @param player The player who clicked.
     * @param clickedItem The ItemStack that was clicked.
     */
    private void handleMainShopGUIClick(Player player, ItemStack clickedItem) {
        AdminShopGUIManager.ShopCategory category = shopGUIManager.getCategoryByIcon(clickedItem);
        if (category != null) {
            // Player selected a category, open its GUI at page 1.
            // AdminShopGUIManager.openCategoryGUI will call playerOpenedCategoryView to update the map.
            shopGUIManager.openCategoryGUI(player, category, 1);
        }
    }

    /**
     * Handles clicks within a specific category's GUI (item interactions, navigation).
     * @param player The player who clicked.
     * @param clickedItem The ItemStack that was clicked.
     * @param category The ShopCategory being viewed.
     * @param slot The raw slot number clicked.
     * @param clickType The type of click.
     * @param view The CurrentCategoryView containing current page and category context.
     */
    private void handleCategoryGUIClick(Player player, ItemStack clickedItem, AdminShopGUIManager.ShopCategory category,
                                       int slot, ClickType clickType, CurrentCategoryView view) {

        ItemMeta meta = clickedItem.getItemMeta();
        // All interactable items (nav buttons, shop items) should have ItemMeta and a display name.
        if (meta == null || !meta.hasDisplayName()) {
            // This could be a filler pane, or an item that failed to initialize correctly.
            return;
        }
        String displayName = meta.getDisplayName(); // Already color-translated

        // --- Navigation Handling ---
        if (displayName.equals(ChatUtils.translateAlternateColorCodes(NAV_BACK_NAME_RAW))) {
            shopGUIManager.openMainShopGUI(player);
            // playerCategoryView.remove(player.getUniqueId()); // Cleared by playerClosedShop on inventory close event
            return;
        }

        int currentPage = view.page;

        if (displayName.equals(ChatUtils.translateAlternateColorCodes(NAV_NEXT_PAGE_NAME_RAW))) {
            if (currentPage < shopGUIManager.getTotalPages(category)) {
                shopGUIManager.openCategoryGUI(player, category, currentPage + 1);
            } // else, already on last page, button might be grayed out by GUIManager display logic.
            return;
        }

        if (displayName.equals(ChatUtils.translateAlternateColorCodes(NAV_PREVIOUS_PAGE_NAME_RAW))) {
             if (currentPage > 1) {
                shopGUIManager.openCategoryGUI(player, category, currentPage - 1);
            } // else, already on first page.
            return;
        }

        // --- Item Interaction ---
        // If it's not a navigation item, try to resolve it as a shop item.
        // Note: getShopItemFromCategoryBySlot might be unreliable if items are auto-placed without updating a slot map.
        // A more robust way is to iterate items for the current page and match by the ItemStack itself,
        // or by a persistent identifier stored on the item (e.g., NBT tag or PersistentDataContainer).
        // For now, we rely on AdminShopGUIManager.getShopItemFromCategoryBySlot or a similar mechanism.
        // The current AdminShopGUIManager.getShopItemFromCategoryBySlot uses a map based on *configured* slot.
        // This needs to align with how items are *actually displayed* in openCategoryGUI.
        // A simple fix: iterate through items on the current page and check if the clickedItem.isSimilar() to the shopItem's display item.
        // This is less efficient but more reliable than slot mapping if slots are dynamic.

        // Let's refine how we get the ShopItem:
        // Iterate through the items on the current page and see if the clickedItem matches any of them.
        int itemsPerPage = Math.max(1, shopGUIManager.getItemRows() * 9 - 9);
        if (shopGUIManager.getItemRows() == 1) itemsPerPage = shopGUIManager.getItemRows() * 9;

        List<AdminShopGUIManager.ShopItem> pageItems = category.getItems().stream()
            .skip((long)(currentPage - 1) * itemsPerPage)
            .limit(itemsPerPage)
            .collect(java.util.stream.Collectors.toList());

        AdminShopGUIManager.ShopItem targetShopItem = null;

        // First, try to get the item by its configured slot (if applicable for the GUI design)
        targetShopItem = shopGUIManager.getShopItemFromCategoryBySlot(category, slot);

        // If not found by slot (e.g., item was auto-placed or slot mapping isn't direct),
        // then try to identify it using PersistentDataContainer tag.
        if (targetShopItem == null) {
            ItemMeta clickedMeta = clickedItem.getItemMeta();
            if (clickedMeta != null) {
                PersistentDataContainer container = clickedMeta.getPersistentDataContainer();
                if (container.has(itemKeyPDC, PersistentDataType.STRING)) {
                    String itemInternalName = container.get(itemKeyPDC, PersistentDataType.STRING);
                    if (itemInternalName != null) {
                        // This retrieves the ShopItem based on its unique internal name stored in PDC
                        targetShopItem = shopGUIManager.getShopItemByInternalName(itemInternalName);
                    }
                }
            }
        }

        // ItemMeta clickedMeta = clickedItem.getItemMeta(); // Original position of this line, moved up
        // if (clickedMeta != null) { // Original structure, now part of the if (targetShopItem == null) block
        //    PersistentDataContainer container = clickedMeta.getPersistentDataContainer();
            // if (container.has(itemKeyPDC, PersistentDataType.STRING)) { // Original structure
            //    String itemInternalName = container.get(itemKeyPDC, PersistentDataType.STRING);
            //    if (itemInternalName != null) {
            //        targetShopItem = shopGUIManager.getShopItemByInternalName(itemInternalName);
            //    }
            // }
        // } // Original structure

        if (targetShopItem == null) {
            // If after both attempts (slot-based and PDC-based), the item is still not found,
            // it's likely a filler pane or an unhandled part of the GUI.
            // Navigation buttons should have been handled by their display name checks *before* this item resolution logic.
            plugin.getLogger().fine("[AdminShopListener] Clicked item is not a recognized shop item. Slot: " + slot);
            return;
        }

        // If targetShopItem is found (either by slot or PDC), proceed:
        processItemInteraction(player, targetShopItem, clickType);
    }

    /**
     * Processes buy/sell actions based on click type for a given ShopItem.
     * @param player The player performing the action.
     * @param shopItem The ShopItem being interacted with.
     * @param clickType The type of click.
     */
    private void processItemInteraction(Player player, AdminShopGUIManager.ShopItem shopItem, ClickType clickType){
        int amountToTransact;
        switch (clickType) {
            case LEFT:
                amountToTransact = 1;
                processBuy(player, shopItem, amountToTransact);
                break;
            case RIGHT:
                amountToTransact = 1;
                processSell(player, shopItem, amountToTransact);
                break;
            case SHIFT_LEFT:
                amountToTransact = shopItem.getMaterial().getMaxStackSize(); // Max stack size of the item
                processBuy(player, shopItem, amountToTransact);
                break;
            case SHIFT_RIGHT:
                amountToTransact = shopItem.getMaterial().getMaxStackSize();
                processSell(player, shopItem, amountToTransact);
                break;
            default:
                // Other click types not handled for now
                return;
        }
    }


    private void processBuy(Player player, AdminShopGUIManager.ShopItem shopItem, int amount) {
        if (amount <= 0) return;
        if (shopItem.getBuyPrice() < 0) {
            player.sendMessage(shopGUIManager.getMessage("item_not_buyable", null));
            return;
        }
        if (shopItem.getPermissionBuy() != null && !player.hasPermission(shopItem.getPermissionBuy())) {
            player.sendMessage(shopGUIManager.getMessage("no_permission_buy", null));
            return;
        }

        double totalCost = shopItem.getBuyPrice() * amount;
        // Use static call to EconomyManager
        if (EconomyManager.getBalance(player) < totalCost) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("price", String.format("%.2f", totalCost));
            placeholders.put("currency", shopGUIManager.getCurrencySymbol());
            player.sendMessage(shopGUIManager.getMessage("insufficient_funds", placeholders));
            return;
        }

        ItemStack toGive = shopGUIManager.createItemStackForShopItem(shopItem, amount); // Needs to be added to AdminShopGUIManager
        // Ensure 'toGive' has its amount set correctly by createItemStackForShopItem for hasEnoughSpace.
        // ShopInventoryManager.hasEnoughSpace checks based on the amount on the itemToReceive.
        if (!ShopInventoryManager.hasEnoughSpace(player, toGive)) {
            player.sendMessage(shopGUIManager.getMessage("inventory_full", null));
            return;
        }

        // Use static call to EconomyManager
        if (!EconomyManager.withdraw(player, totalCost)) {
            // Withdrawal failed (e.g. another plugin cancelled, or unexpected error)
            // EconomyManager logs details. Send a generic failure to player.
            player.sendMessage(shopGUIManager.getMessage("buy_failed_transaction", null));
            return;
        }
        player.getInventory().addItem(toGive);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("item_name", shopItem.getDisplayName()); // Use the shop item's display name
        placeholders.put("price", String.format("%.2f", totalCost));
        placeholders.put("currency", shopGUIManager.getCurrencySymbol());
        player.sendMessage(shopGUIManager.getMessage("buy_success", placeholders));
        // player.updateInventory(); // Usually not needed, Bukkit handles it.
    }

    private void processSell(Player player, AdminShopGUIManager.ShopItem shopItem, int amount) {
        if (amount <= 0) return;
        if (shopItem.getSellPrice() < 0) {
            player.sendMessage(shopGUIManager.getMessage("item_not_sellable", null));
            return;
        }
        if (shopItem.getPermissionSell() != null && !player.hasPermission(shopItem.getPermissionSell())) {
            player.sendMessage(shopGUIManager.getMessage("no_permission_sell", null));
            return;
        }

        // For selling, nbtData and displayName from shopItem are crucial for matching.
        // The InventoryUtils.countItems and removeItems need to be robust.
        // Start with Material matching, then add more complex logic.

        // Create a template ItemStack for matching based on material.
        // This simplifies matching; NBT/display name specific matching is removed for now as per requirements.
        ItemStack templateItem = new ItemStack(shopItem.getMaterial());

        int itemsPlayerHas = ShopInventoryManager.countItemsInInventory(player, templateItem);

        if (itemsPlayerHas < amount) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("item_name", shopItem.getDisplayName());
            player.sendMessage(shopGUIManager.getMessage("insufficient_items_to_sell", placeholders));
            return;
        }

        double totalPayment = shopItem.getSellPrice() * amount;

        // removeItems needs to be accurate based on what shopItem defines (material, NBT, name)
        // Using the same templateItem for removal.
        boolean removed = ShopInventoryManager.removeItemsFromInventory(player, templateItem, amount);
        if (!removed) {
            player.sendMessage(shopGUIManager.getMessage("sell_error_removing_items", null));
            plugin.getLogger().warning("AdminShopListener: Failed to remove " + amount + " of " + shopItem.getInternalName() + " ("+ shopItem.getMaterial() +") from " + player.getName() + " using template match, despite count check passing.");
            // No transaction occurred with economy yet, so just return.
            return;
        }

        // Use static call to EconomyManager
        if (!EconomyManager.deposit(player, totalPayment)) {
            // Deposit failed! This is problematic as items have been removed.
            player.sendMessage(shopGUIManager.getMessage("sell_failed_transaction", null));
            plugin.getLogger().severe("AdminShopListener: CRITICAL! Failed to deposit " + totalPayment + " to " + player.getName() + " after items were removed. Attempting to return items.");

            // Attempt to return items (this is a best-effort)
            ItemStack itemsToReturn = shopGUIManager.createItemStackForShopItem(shopItem, amount);
            HashMap<Integer, ItemStack> couldNotReturn = player.getInventory().addItem(itemsToReturn);
            if (!couldNotReturn.isEmpty()) {
                player.sendMessage(shopGUIManager.getMessage("sell_failed_item_return_inventory_full", null));
                for (ItemStack drop : couldNotReturn.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                plugin.getLogger().warning("AdminShopListener: Could not return all items to " + player.getName() + " due to full inventory; items were dropped.");
            } else {
                 plugin.getLogger().info("AdminShopListener: Successfully returned items to " + player.getName() + " after failed deposit.");
            }
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("item_name", shopItem.getDisplayName());
        placeholders.put("price", String.format("%.2f", totalPayment));
        placeholders.put("currency", shopGUIManager.getCurrencySymbol());
        player.sendMessage(shopGUIManager.getMessage("sell_success", placeholders));
        // player.updateInventory();
    }

    // Called by AdminShopGUIManager when a category GUI is opened
    public void playerOpenedCategoryView(Player player, AdminShopGUIManager.ShopCategory category, int page) {
        playerCategoryView.put(player.getUniqueId(), new CurrentCategoryView(category, page));
    }

    public void playerClosedShop(Player player) {
        playerCategoryView.remove(player.getUniqueId());
    }
}
