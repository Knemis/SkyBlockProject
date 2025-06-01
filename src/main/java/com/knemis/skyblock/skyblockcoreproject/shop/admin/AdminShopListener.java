package com.knemis.skyblock.skyblockcoreproject.shop.admin;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopInventoryManager;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminShopListener implements Listener {

    private final SkyBlockProject plugin;
    private final AdminShopGUIManager shopGUIManager;
    private final Map<UUID, CurrentCategoryView> playerCategoryView = new HashMap<>();
    private final NamespacedKey itemKeyPDC;

    private static final String NAV_BACK_NAME_RAW = "&e&lBack to Categories";
    private static final String NAV_NEXT_PAGE_NAME_RAW = "&a&lNext Page";
    private static final String NAV_PREVIOUS_PAGE_NAME_RAW = "&a&lPrevious Page";
    // NAV_PAGE_INFO_IDENTIFIER_RAW is not used for direct comparison of item names.

    private static class CurrentCategoryView {
        final AdminShopGUIManager.ShopCategory category;
        final int page;
        CurrentCategoryView(AdminShopGUIManager.ShopCategory category, int page) {
            this.category = category;
            this.page = page;
        }
    }

    public AdminShopListener(SkyBlockProject plugin, AdminShopGUIManager shopGUIManager) {
        this.plugin = plugin;
        this.shopGUIManager = shopGUIManager;
        this.itemKeyPDC = new NamespacedKey(plugin, "admin_shop_item_internal_name");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        Component viewTitleComponent = event.getView().title(); // Paper API for Component title
        AdminShopGUIManager.ShopCategory currentCategoryFromTitle = null;

        boolean isMainShop = viewTitleComponent.equals(shopGUIManager.getMainShopTitle());
        if (!isMainShop) {
            currentCategoryFromTitle = shopGUIManager.getCategoryByTitle(viewTitleComponent);
        }

        if (!isMainShop && currentCategoryFromTitle == null) return;

        event.setCancelled(true);
        if (clickedInventory != event.getView().getTopInventory()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (isMainShop) {
            handleMainShopGUIClick(player, clickedItem);
        } else if (currentCategoryFromTitle != null) {
            CurrentCategoryView view = playerCategoryView.get(player.getUniqueId());
            if (view == null || view.category != currentCategoryFromTitle) {
                plugin.getLogger().warning("AdminShopListener: Player " + player.getName() + " clicked in category '" +
                                           PlainComponentSerializer.plain().serialize(currentCategoryFromTitle.getDisplayName()) +
                                           "' but view info was missing or mismatched. Assuming page 1.");
                view = new CurrentCategoryView(currentCategoryFromTitle, 1);
                playerCategoryView.put(player.getUniqueId(), view);
            }
            handleCategoryGUIClick(player, clickedItem, view.category, event.getSlot(), event.getClick(), view);
        }
    }

    private void handleMainShopGUIClick(Player player, ItemStack clickedItem) {
        AdminShopGUIManager.ShopCategory category = shopGUIManager.getCategoryByIcon(clickedItem);
        if (category != null) {
            shopGUIManager.openCategoryGUI(player, category, 1);
        }
    }

    private void handleCategoryGUIClick(Player player, ItemStack clickedItem, AdminShopGUIManager.ShopCategory category,
                                       int slot, ClickType clickType, CurrentCategoryView view) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        Component clickedDisplayNameComponent = meta.displayName();
        if (clickedDisplayNameComponent == null) return;

        // Serialize components to plain text for comparison with constants (which are also deserialized then serialized to plain)
        String plainClickedName = PlainComponentSerializer.plain().serialize(clickedDisplayNameComponent);
        String plainNavBack = PlainComponentSerializer.plain().serialize(ChatUtils.deserializeLegacyColorCodes(NAV_BACK_NAME_RAW));
        String plainNavNext = PlainComponentSerializer.plain().serialize(ChatUtils.deserializeLegacyColorCodes(NAV_NEXT_PAGE_NAME_RAW));
        String plainNavPrev = PlainComponentSerializer.plain().serialize(ChatUtils.deserializeLegacyColorCodes(NAV_PREVIOUS_PAGE_NAME_RAW));

        if (plainClickedName.equals(plainNavBack)) {
            shopGUIManager.openMainShopGUI(player);
            return;
        }
        int currentPage = view.page;
        if (plainClickedName.equals(plainNavNext)) {
            if (currentPage < shopGUIManager.getTotalPages(category)) {
                shopGUIManager.openCategoryGUI(player, category, currentPage + 1);
            }
            return;
        }
        if (plainClickedName.equals(plainNavPrev)) {
             if (currentPage > 1) {
                shopGUIManager.openCategoryGUI(player, category, currentPage - 1);
            }
            return;
        }

        AdminShopGUIManager.ShopItem targetShopItem = null;
        targetShopItem = shopGUIManager.getShopItemFromCategoryBySlot(category, slot);
        if (targetShopItem == null) {
            if (meta.getPersistentDataContainer().has(itemKeyPDC, PersistentDataType.STRING)) {
                String itemInternalName = meta.getPersistentDataContainer().get(itemKeyPDC, PersistentDataType.STRING);
                if (itemInternalName != null) {
                    targetShopItem = shopGUIManager.getShopItemByInternalName(itemInternalName);
                }
            }
        }
        if (targetShopItem == null) {
            plugin.getLogger().fine("[AdminShopListener] Clicked item is not a recognized shop item. Slot: " + slot);
            return;
        }
        processItemInteraction(player, targetShopItem, clickType);
    }

    private void processItemInteraction(Player player, AdminShopGUIManager.ShopItem shopItem, ClickType clickType){
        int amountToTransact;
        switch (clickType) {
            case LEFT: amountToTransact = 1; processBuy(player, shopItem, amountToTransact); break;
            case RIGHT: amountToTransact = 1; processSell(player, shopItem, amountToTransact); break;
            case SHIFT_LEFT: amountToTransact = shopItem.getMaterial().getMaxStackSize(); processBuy(player, shopItem, amountToTransact); break;
            case SHIFT_RIGHT: amountToTransact = shopItem.getMaterial().getMaxStackSize(); processSell(player, shopItem, amountToTransact); break;
            default: return;
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
        if (EconomyManager.getBalance(player) < totalCost) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("price", String.format("%.2f", totalCost));
            placeholders.put("currency", PlainComponentSerializer.plain().serialize(shopGUIManager.getCurrencySymbol()));
            player.sendMessage(shopGUIManager.getMessage("insufficient_funds", placeholders));
            return;
        }
        ItemStack toGive = shopGUIManager.createItemStackForShopItem(shopItem, amount);
        if (!ShopInventoryManager.hasEnoughSpace(player, toGive)) {
            player.sendMessage(shopGUIManager.getMessage("inventory_full", null));
            return;
        }
        if (!EconomyManager.withdraw(player, totalCost)) {
            player.sendMessage(shopGUIManager.getMessage("buy_failed_transaction", null));
            return;
        }
        player.getInventory().addItem(toGive);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("item_name", PlainComponentSerializer.plain().serialize(shopItem.getDisplayName()));
        placeholders.put("price", String.format("%.2f", totalCost));
        placeholders.put("currency", PlainComponentSerializer.plain().serialize(shopGUIManager.getCurrencySymbol()));
        player.sendMessage(shopGUIManager.getMessage("buy_success", placeholders));
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
        ItemStack templateItem = new ItemStack(shopItem.getMaterial());
        int itemsPlayerHas = ShopInventoryManager.countItemsInInventory(player, templateItem);
        if (itemsPlayerHas < amount) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("item_name", PlainComponentSerializer.plain().serialize(shopItem.getDisplayName()));
            player.sendMessage(shopGUIManager.getMessage("insufficient_items_to_sell", placeholders));
            return;
        }
        double totalPayment = shopItem.getSellPrice() * amount;
        boolean removed = ShopInventoryManager.removeItemsFromInventory(player, templateItem, amount);
        if (!removed) {
            player.sendMessage(shopGUIManager.getMessage("sell_error_removing_items", null));
            return;
        }
        if (!EconomyManager.deposit(player, totalPayment)) {
            player.sendMessage(shopGUIManager.getMessage("sell_failed_transaction", null));
            ItemStack itemsToReturn = shopGUIManager.createItemStackForShopItem(shopItem, amount);
            HashMap<Integer, ItemStack> couldNotReturn = player.getInventory().addItem(itemsToReturn);
            if (!couldNotReturn.isEmpty()) {
                player.sendMessage(shopGUIManager.getMessage("sell_failed_item_return_inventory_full", null));
                for (ItemStack drop : couldNotReturn.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("item_name", PlainComponentSerializer.plain().serialize(shopItem.getDisplayName()));
        placeholders.put("price", String.format("%.2f", totalPayment));
        placeholders.put("currency", PlainComponentSerializer.plain().serialize(shopGUIManager.getCurrencySymbol()));
        player.sendMessage(shopGUIManager.getMessage("sell_success", placeholders));
    }

    public void playerOpenedCategoryView(Player player, AdminShopGUIManager.ShopCategory category, int page) {
        playerCategoryView.put(player.getUniqueId(), new CurrentCategoryView(category, page));
    }

    public void playerClosedShop(Player player) {
        playerCategoryView.remove(player.getUniqueId());
    }
}
