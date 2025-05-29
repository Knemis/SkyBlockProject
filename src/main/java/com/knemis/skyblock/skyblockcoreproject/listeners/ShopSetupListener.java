// com/knemis/skyblock/skyblockcoreproject/listeners/ShopSetupListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    private static final int PLAYER_BUY_SHOP_SLOT = 2;
    private static final int PLAYER_SELL_SHOP_SLOT = 4;
    private static final int PLAYER_BUY_SELL_SHOP_SLOT = 6;

    // ITEM_SELECT_TITLE için ortadaki slot (13), QUANTITY_INPUT_TITLE için miktar girme slotu (22) olmalı
    // ShopSetupGUIManager.openQuantityInputMenu'deki tasarıma göre güncellenmeli.
    // Şimdilik, her iki GUI için de farklı olabileceklerini varsayarak,
    // handleQuantityInputGuiClickLogic'e slot parametresi eklemek yerine
    // ITEM_PLACEMENT_SLOT'u genel bir yerleştirme slotu olarak düşünelim
    // ve GUI tasarımına göre bu listener içindeki click logic'te doğru slotu kullanalım.
    private static final int ITEM_SELECT_PLACEMENT_SLOT = 13; // Eşya seçimi GUI'sindeki yerleştirme slotu
    private static final int QUANTITY_PLACEMENT_SLOT = 22;    // Miktar GUI'sindeki yerleştirme slotu
    private static final int CONFIRM_BUTTON_SLOT_QUANTITY = 31;

    public ShopSetupListener(SkyBlockProject plugin, ShopManager shopManager, ShopSetupGUIManager shopSetupGUIManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(viewTitleComponent);
        ItemStack clickedItem = event.getCurrentItem();
        String clickedItemName = (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) ?
                LegacyComponentSerializer.legacySection().serialize(clickedItem.getItemMeta().displayName()) :
                (clickedItem != null ? clickedItem.getType().name() : "null");

        Location chestLocationForLog = plugin.getPlayerShopSetupState().get(player.getUniqueId());

        if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) {
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) clicked in Shop Setup GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), player.getUniqueId(), viewTitle, event.getRawSlot(), clickedItemName));
            event.setCancelled(true);
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int rawSlot = event.getRawSlot();
            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            if (chestLocation == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum hatası! Lütfen tekrar deneyin.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in GUI '%s', but chestLocation is null. State error.",
                        player.getName(), player.getUniqueId(), viewTitle));
                player.closeInventory();
                return;
            }
            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            if (pendingShop == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum bilgisi bulunamadı. Lütfen tekrar deneyin.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in GUI '%s', but pendingShop is null for location %s. State error.",
                        player.getName(), player.getUniqueId(), viewTitle, chestLocation));
                player.closeInventory();
                return;
            }

            ShopType selectedType = null;
            if (rawSlot == PLAYER_BUY_SHOP_SLOT) selectedType = ShopType.PLAYER_BUY_SHOP;
            else if (rawSlot == PLAYER_SELL_SHOP_SLOT) selectedType = ShopType.PLAYER_SELL_SHOP;
            else if (rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) selectedType = ShopType.PLAYER_BUY_SELL_SHOP;

            if (selectedType != null) {
                pendingShop.setShopType(selectedType);
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) selected shop type %s for shop at %s.",
                        player.getName(), player.getUniqueId(), selectedType.name(), chestLocation));
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) clicked invalid slot %d in SHOP_TYPE_TITLE GUI for shop %s.",
                        player.getName(), player.getUniqueId(), rawSlot, chestLocation));
            }
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            // Logging for item selection is handled within handleItemSelectionGuiClickLogic if needed
            handleItemSelectionGuiClickLogic(event, player, topInventory);
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            // Logging for quantity input is handled within handleQuantityInputGuiClickLogic
            handleQuantityInputGuiClickLogic(event, player, topInventory);
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.CONFIRMATION_TITLE)) {
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) clicked in Shop Setup GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), player.getUniqueId(), viewTitle, event.getRawSlot(), clickedItemName));
            event.setCancelled(true);
            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;

            if (pendingShop == null || !plugin.getPlayerWaitingForSetupInput().containsKey(player.getUniqueId()) ||
                    plugin.getPlayerWaitingForSetupInput().get(player.getUniqueId()) != ShopSetupGUIManager.InputType.PRICE) {
                player.sendMessage(ChatColor.RED + "Onaylama hatası. Kurulum bilgileri eksik.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in confirmation GUI for %s, but state error. PendingShop: %s, InputState: %s",
                        player.getName(), player.getUniqueId(), chestLocation, pendingShop, plugin.getPlayerWaitingForSetupInput().get(player.getUniqueId())));
                player.closeInventory();
                shopManager.cancelShopSetup(player.getUniqueId()); // Use shopManager's cancel method
                return;
            }

            if (event.getCurrentItem() != null) {
                String shopIdForLog = pendingShop.getShopId(); // Get ID before it's potentially changed by finalization
                Material itemTypeForLog = pendingShop.getTemplateItemStack() != null ? pendingShop.getTemplateItemStack().getType() : Material.AIR;
                double priceForLog = pendingShop.getBuyPrice() != -1 ? pendingShop.getBuyPrice() : pendingShop.getSellPrice();
                int quantityForLog = pendingShop.getBundleAmount();
                ShopType typeForLog = pendingShop.getShopType();

                if (event.getCurrentItem().getType() == Material.GREEN_WOOL) {
                    shopManager.finalizeShopSetup(
                            chestLocation,
                            player,
                            plugin.getPlayerInitialShopStockItem().get(player.getUniqueId())
                    );
                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) CONFIRMED shop setup for item %s at %s. Price: %.2f, Quantity: %d, Type: %s. Shop ID: %s",
                            player.getName(), player.getUniqueId(), itemTypeForLog, chestLocation, priceForLog, quantityForLog, typeForLog, shopIdForLog));
                } else if (event.getCurrentItem().getType() == Material.RED_WOOL) {
                    player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumu iptal edildi.");
                    shopManager.cancelShopSetup(player.getUniqueId());
                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) CANCELLED shop setup for location %s via confirmation GUI (red wool). Pending Shop ID was: %s",
                            player.getName(), player.getUniqueId(), chestLocation, shopIdForLog));
                }
                player.closeInventory();
            }
        } else {
            // This block handles clicks in GUIs not explicitly managed above but might be part of setup (e.g., player inventory during setup)
            // For now, we don't log these unless they interfere with a specific setup phase.
        }
    }

    private void handleItemSelectionGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory) {
        Inventory clickedInventory = event.getClickedInventory();
        Location shopLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId()); // For logging context

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    if (guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT) == null || guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT).getType() == Material.AIR) {
                        guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, itemToMove.clone());
                        event.setCurrentItem(null);
                        player.updateInventory();
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) moved item %s to item selection slot for shop at %s.",
                                player.getName(), player.getUniqueId(), itemToMove.getType(), shopLocation));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Eşya seçme yuvası zaten dolu. Lütfen önce mevcut eşyayı alın.");
                        plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) failed to move item %s to item selection slot (full) for shop at %s.",
                                player.getName(), player.getUniqueId(), itemToMove.getType(), shopLocation));
                    }
                }
            } else {
                event.setCancelled(false); // Allow normal player inventory interactions
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == ITEM_SELECT_PLACEMENT_SLOT) {
                event.setCancelled(false); // Allow taking/placing item in the designated slot
            } else {
                event.setCancelled(true); // Prevent clicking other GUI slots
                plugin.getLogger().finer(String.format("ShopSetupListener: Player %s (UUID: %s) clicked non-interactive slot %d in ITEM_SELECT_TITLE GUI for shop %s.",
                        player.getName(), player.getUniqueId(), event.getRawSlot(), shopLocation));
            }
        } else {
            if (clickedInventory != null) event.setCancelled(true); // Prevent interaction if clicked inv is not null but also not top/bottom (shouldn't happen)
        }
    }

    private void handleQuantityInputGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory) {
        Inventory clickedInventory = event.getClickedInventory();
        Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
        Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;

        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "Kurulum hatası: Satılacak eşya şablonu bulunamadı. Lütfen baştan başlayın.");
            plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in quantity GUI for shop %s, but pendingShop or templateItem is null. Cancelling.",
                    player.getName(), player.getUniqueId(), chestLocation));
            player.closeInventory();
            event.setCancelled(true);
            if (player != null) shopManager.cancelShopSetup(player.getUniqueId()); // Use shopManager's cancel
            return;
        }
        ItemStack templateItem = pendingShop.getTemplateItemStack();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title()); // For logging

        if (event.getRawSlot() == CONFIRM_BUTTON_SLOT_QUANTITY && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_WOOL) {
            event.setCancelled(true);
            ItemStack quantityItemStack = guiInventory.getItem(QUANTITY_PLACEMENT_SLOT);
            if (quantityItemStack != null && quantityItemStack.isSimilar(templateItem) && quantityItemStack.getAmount() > 0) {
                pendingShop.setItemQuantityForPrice(quantityItemStack.getAmount());
                shopSetupGUIManager.openPriceInputPrompt(player, pendingShop);
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) confirmed quantity %d for item %s for shop at %s. Proceeding to price input.",
                        player.getName(), player.getUniqueId(), quantityItemStack.getAmount(), templateItem.getType(), chestLocation));
            } else {
                player.sendMessage(ChatColor.RED + "Lütfen miktar yuvasına doğru türde (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") ve geçerli miktarda eşya koyun.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) clicked confirm in quantity GUI for shop %s, but item/quantity was invalid. ItemInSlot: %s, Template: %s",
                        player.getName(), player.getUniqueId(), chestLocation, quantityItemStack, templateItem));
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) { // Interacting with player inventory
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift-click from player inv to GUI
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.isSimilar(templateItem)) {
                    ItemStack currentItemInSlot = guiInventory.getItem(QUANTITY_PLACEMENT_SLOT);
                    if (currentItemInSlot == null || currentItemInSlot.getType() == Material.AIR) {
                        guiInventory.setItem(QUANTITY_PLACEMENT_SLOT, itemToMove.clone());
                        event.setCurrentItem(null);
                    } else if (currentItemInSlot.isSimilar(itemToMove) && currentItemInSlot.getAmount() < currentItemInSlot.getMaxStackSize()) {
                        int canAdd = currentItemInSlot.getMaxStackSize() - currentItemInSlot.getAmount();
                        int willAdd = Math.min(canAdd, itemToMove.getAmount());
                        if (willAdd > 0) {
                            currentItemInSlot.setAmount(currentItemInSlot.getAmount() + willAdd);
                            if (itemToMove.getAmount() - willAdd > 0) itemToMove.setAmount(itemToMove.getAmount() - willAdd);
                            else event.setCurrentItem(null);
                        }
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Miktar yuvası dolu veya daha fazla eklenemiyor.");
                    }
                    player.updateInventory();
                } else if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                    plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) tried to shift-click non-template item %s to quantity slot for shop %s (Template: %s).",
                            player.getName(), player.getUniqueId(), itemToMove.getType(), chestLocation, templateItem.getType()));
                }
            } else {
                event.setCancelled(false); // Allow normal player inventory interactions
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) { // Interacting with the GUI inventory
            if (event.getRawSlot() == QUANTITY_PLACEMENT_SLOT) { // Interacting with the designated quantity slot
                ItemStack cursorItem = event.getCursor();
                if (event.getAction().name().startsWith("PLACE_")) {
                    if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                        if (cursorItem.isSimilar(templateItem)) {
                            event.setCancelled(false);
                        } else {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(false); // Allow placing AIR (removing item)
                    }
                } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR && !cursorItem.isSimilar(templateItem)) {
                        player.sendMessage(ChatColor.RED + "Bu yuvayla sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya değiştirebilirsiniz!");
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false); // Allow pickup or swap with similar/air
                    }
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift-click from GUI to player inv
                    event.setCancelled(false);
                }
                else event.setCancelled(true); // Other actions like CLONE_STACK etc.
            }
            else if (event.getRawSlot() != CONFIRM_BUTTON_SLOT_QUANTITY) { // Onay butonu hariç diğer slotlar
                event.setCancelled(true);
                plugin.getLogger().finer(String.format("ShopSetupListener: Player %s (UUID: %s) clicked non-interactive slot %d in QUANTITY_INPUT_TITLE GUI for shop %s.",
                        player.getName(), player.getUniqueId(), event.getRawSlot(), chestLocation));
            }
            // Allow click on confirm button (handled at the start of this method)
        } else {
            if(clickedInventory != null) event.setCancelled(true);
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Component viewTitleComponent = event.getView().title();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(viewTitleComponent);
        UUID playerId = player.getUniqueId();

        Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
        if (chestLocation == null) {
            // Not in a shop setup process tracked by this map, or already cleared.
            // Check if they were waiting for admin input, as that's a different map.
            ShopAdminGUIManager.AdminInputType adminInputType = plugin.getPlayerWaitingForAdminInput().get(playerId);
            if (adminInputType != null && viewTitle.equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
                // This case is handled by ShopListener's onInventoryClose, no specific log here unless it's for ShopSetupListener specific admin GUI part
            }
            return;
        }

        Shop pendingShop = shopManager.getPendingShop(chestLocation);
        // If pendingShop is null here, it means it was likely finalized or cancelled by another means before this event.
        // The setup state should have been cleared in that case.

        boolean isStillInSetupChain = plugin.getPlayerWaitingForSetupInput().containsKey(playerId);

        if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_SELECT_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                // This logic is fine, but the actual next step is triggered by a BukkitRunnable
                // Logging for "proceeding to quantity" should be when that runnable executes and opens the next GUI.
                // Here, we just log the item was set.
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) closed ITEM_SELECT_TITLE for shop %s. Item %s was in slot. Setup proceeds.",
                        player.getName(), playerId, chestLocation, itemInSlot.getType()));
                // isStillInSetupChain remains true because the runnable will open the next GUI.
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed ITEM_SELECT_TITLE for shop %s with no item selected. Cancelling setup.",
                        player.getName(), playerId, chestLocation));
                shopManager.cancelShopSetup(playerId); // This will clear states.
                isStillInSetupChain = false; // Explicitly set, as cancelShopSetup should handle state clearing.
            }
        } else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            if (pendingShop != null && pendingShop.getBundleAmount() <= 0) { // Bundle amount not set means they didn't confirm
                ItemStack itemInQuantitySlot = event.getInventory().getItem(QUANTITY_PLACEMENT_SLOT);
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed QUANTITY_INPUT_TITLE for shop %s. Quantity not confirmed. Cancelling. Item in slot: %s",
                        player.getName(), playerId, chestLocation, itemInQuantitySlot != null ? itemInQuantitySlot.toString() : "null"));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            } else if (pendingShop != null) { // Bundle amount is set, means they clicked confirm and are proceeding to price input
                isStillInSetupChain = plugin.getPlayerWaitingForSetupInput().get(playerId) == ShopSetupGUIManager.InputType.PRICE;
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) closed QUANTITY_INPUT_TITLE for shop %s. Quantity %d confirmed. Expecting price input: %s",
                        player.getName(), playerId, chestLocation, pendingShop.getBundleAmount(), isStillInSetupChain));
            } else { // Pending shop became null
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed QUANTITY_INPUT_TITLE for shop %s, but pendingShop is null. Setup likely cancelled.",
                        player.getName(), playerId, chestLocation));
                isStillInSetupChain = false;
            }
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) {
            if (pendingShop != null && pendingShop.getShopType() == null) {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed SHOP_TYPE_TITLE for shop %s. Type not selected. Cancelling setup.",
                        player.getName(), playerId, chestLocation));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            } else if (pendingShop != null) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) closed SHOP_TYPE_TITLE for shop %s. Type %s selected. Proceeding.",
                        player.getName(), playerId, chestLocation, pendingShop.getShopType()));
                isStillInSetupChain = true; // They selected a type, next GUI will open.
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed SHOP_TYPE_TITLE for shop %s, but pendingShop is null. Setup likely cancelled.",
                        player.getName(), playerId, chestLocation));
                isStillInSetupChain = false;
            }
        } else if (viewTitleComponent.equals(ShopSetupGUIManager.CONFIRMATION_TITLE)) {
            // If they close the confirmation GUI without clicking green/red wool, it's an implicit cancel.
            // The click handler for green/red wool already logs success/cancel and closes inventory,
            // so this InventoryCloseEvent might fire right after.
            // We need to ensure we don't double-log cancellation or log cancellation after success.
            // If shop is no longer pending, it means it was finalized or explicitly cancelled by button.
            if (shopManager.getPendingShop(chestLocation) != null) { // Still pending, so they just closed the GUI
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) closed CONFIRMATION_TITLE for shop %s without confirming/cancelling via button. Cancelling setup.",
                        player.getName(), playerId, chestLocation));
                shopManager.cancelShopSetup(playerId);
            }
            isStillInSetupChain = false; // No matter what, closing confirmation GUI ends this chain here.
        }


        // Final check: if player is no longer in a setup chain (e.g. didn't select item, or closed price input prompt)
        // and their setup state is still present, then cancel.
        // This covers cases like closing the chat prompt for price input.
        if (!isStillInSetupChain && plugin.getPlayerShopSetupState().containsKey(playerId)) {
            // Check if it's specifically the price input they bailed on
            if (plugin.getPlayerWaitingForSetupInput().get(playerId) == ShopSetupGUIManager.InputType.PRICE) {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) was in PRICE input state for shop %s but is no longer in setup chain (e.g. closed chat or GUI). Cancelling setup.",
                        player.getName(), playerId, chestLocation));
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed setup GUI '%s' for shop %s. Setup not in a chained state or explicitly cancelled. Ensuring cleanup.",
                        player.getName(), playerId, viewTitle, chestLocation));
            }
            shopManager.cancelShopSetup(playerId); // This method should clear all related states.
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(viewTitleComponent);
        boolean isItemSelectGui = viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE);
        boolean isQuantityGui = viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE);

        if (isItemSelectGui || isQuantityGui) {
            int targetPlacementSlot = isItemSelectGui ? ITEM_SELECT_PLACEMENT_SLOT : (isQuantityGui ? QUANTITY_PLACEMENT_SLOT : -1);
            boolean affectsOnlyPlacementSlot = true;
            if (targetPlacementSlot != -1) {
                for (int rawSlot : event.getRawSlots()) {
                    if (rawSlot < topInventory.getSize()) { // Check if the slot is in the top inventory
                        if (rawSlot != targetPlacementSlot) {
                            affectsOnlyPlacementSlot = false;
                            break;
                        }
                    } else { // Slot is in player inventory, disallow dragging into multiple player inv slots if it originated from GUI single slot
                        // This logic might be too restrictive or complex. Generally, dragging into player inv is fine.
                        // The main concern is dragging into multiple GUI slots.
                    }
                }
            } else { // Should not happen if isItemSelectGui or isQuantityGui is true
                affectsOnlyPlacementSlot = false;
            }

            ItemStack draggedItem = event.getOldCursor(); // Item being dragged

            if (!affectsOnlyPlacementSlot) {
                event.setCancelled(true);
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) invalid drag (not to single placement slot) in GUI %s. Cancelled. Dragged item: %s",
                        player.getName(), player.getUniqueId(), viewTitle, draggedItem != null ? draggedItem.getType().name() : "null"));
            } else { // Affects only the placement slot
                if (isQuantityGui) {
                    Shop pendingShop = shopManager.getPendingShop(plugin.getPlayerShopSetupState().get(player.getUniqueId()));
                    if (pendingShop != null && pendingShop.getTemplateItemStack() != null) {
                        if (draggedItem != null && draggedItem.getType() != Material.AIR && !draggedItem.isSimilar(pendingShop.getTemplateItemStack())) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(pendingShop.getTemplateItemStack()) + ChatColor.RED + ") türünde eşya sürükleyebilirsiniz!");
                            plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) invalid drag (item %s not similar to template %s) in QUANTITY_INPUT_TITLE GUI. Cancelled.",
                                    player.getName(), player.getUniqueId(), draggedItem.getType().name(), pendingShop.getTemplateItemStack().getType().name()));
                        }
                        // If similar or air, allow drag.
                    } else { // Pending shop or template item is null
                        event.setCancelled(true);
                        plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) drag in QUANTITY_INPUT_TITLE but pendingShop/templateItem is null. Cancelled. GUI: %s",
                                player.getName(), player.getUniqueId(), viewTitle));
                    }
                }
                // For item select GUI, any item can be dragged into the slot.
            }
        }
    }

    @EventHandler
    public void onPlayerChatForPrice(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage(); // Get message early for logging

        if (plugin.getPlayerWaitingForAdminInput().containsKey(playerId)) {
            ShopAdminGUIManager.AdminInputType inputType = plugin.getPlayerWaitingForAdminInput().get(playerId);
            Location shopLocation = plugin.getPlayerAdministeringShop().get(playerId);
            plugin.getLogger().info(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) providing chat input for %s for shop %s. Message: '%s'",
                    player.getName(), playerId, inputType.name(), shopLocation, message));
            event.setCancelled(true);


            if (shopLocation == null) {
                player.sendMessage(ChatColor.RED + "Hata: Yönetilen dükkan bulunamadı.");
                plugin.getLogger().warning(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) was waiting for %s input, but shopLocation is null.",
                        player.getName(), playerId, inputType.name()));
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId); // Also clear who is administering
                player.sendMessage(ChatColor.YELLOW + "Dükkan ayarı iptal edildi.");
                plugin.getLogger().info(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) cancelled %s input for shop %s.",
                        player.getName(), playerId, inputType.name(), shopLocation));
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    Shop shop = shopManager.getActiveShop(shopLocation);
                    if (shop == null || !shop.getOwnerUUID().equals(playerId)) {
                        player.sendMessage(ChatColor.RED + "Hata: Dükkan bulunamadı veya yönetme yetkiniz yok.");
                        plugin.getLogger().warning(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) chat input for %s, but shop %s not found or not owner.",
                                player.getName(), playerId, inputType.name(), shopLocation));
                        plugin.getPlayerWaitingForAdminInput().remove(playerId);
                        plugin.getPlayerAdministeringShop().remove(playerId);
                        return;
                    }

                    boolean actionSuccess = false;
                    if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME) {
                        int maxNameLength = plugin.getConfig().getInt("shop.max_display_name_length", 30);
                        if (message.length() > 0 && message.length() <= maxNameLength) {
                            shop.setShopDisplayName(ChatColor.translateAlternateColorCodes('&', message));
                            actionSuccess = true;
                            player.sendMessage(ChatColor.GREEN + "Dükkan adı: " + ChatColor.RESET + shop.getShopDisplayName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Ad 1-" + maxNameLength + " karakter olmalı. Tekrar dene veya 'iptal' yaz.");
                            plugin.getLogger().warning(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) provided invalid display name (length %d) for shop %s: '%s'",
                                    player.getName(), playerId, message.length(), shopLocation, message));
                        }
                    } else if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_PRICE) {
                        try {
                            double newPrice = Double.parseDouble(message);
                            if (newPrice >= 0) {
                                shop.setBuyPrice(newPrice);
                                actionSuccess = true;
                                player.sendMessage(ChatColor.GREEN + "Dükkan paket fiyatı: " + ChatColor.GOLD + String.format("%.2f", newPrice));
                            } else {
                                player.sendMessage(ChatColor.RED + "Fiyat negatif olamaz. Tekrar dene veya 'iptal' yaz.");
                                plugin.getLogger().warning(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) provided negative price for shop %s: '%s'",
                                        player.getName(), playerId, shopLocation, message));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz fiyat. Sayı girin (örn: 10.5) veya 'iptal' yaz.");
                            plugin.getLogger().warning(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) provided non-numeric price for shop %s: '%s'",
                                    player.getName(), playerId, shopLocation, message));
                        }
                    }

                    if (actionSuccess) {
                        shopManager.saveShop(shop);
                        plugin.getPlayerWaitingForAdminInput().remove(playerId);
                        plugin.getPlayerAdministeringShop().remove(playerId); // Clear admin state after successful input
                        plugin.getLogger().info(String.format("ShopSetupListener (Admin): Player %s (UUID: %s) successfully set %s to '%s' for shop %s.",
                                player.getName(), playerId, inputType.name(), (inputType == ShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME ? shop.getShopDisplayName() : String.format("%.2f", shop.getBuyPrice())), shopLocation));
                    }
                    // If not successful, player is prompted to try again or cancel, state remains.
                }
            }.runTask(plugin);
            return; // Processed admin input, return.
        }

        if (plugin.getPlayerShopSetupState().containsKey(playerId) &&
                plugin.getPlayerWaitingForSetupInput().get(playerId) == ShopSetupGUIManager.InputType.PRICE) {
            Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
            plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) providing price input for shop %s. Message: '%s'",
                    player.getName(), playerId, chestLocation, message));
            event.setCancelled(true);
            Shop pendingShop = shopManager.getPendingShop(chestLocation);

            if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getBundleAmount() <= 0) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum hatası (eksik bilgi). Baştan başla.");
                plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, but pendingShop/template/bundleAmount invalid. Pending: %s",
                        player.getName(), playerId, chestLocation, pendingShop));
                shopManager.cancelShopSetup(playerId);
                return;
            }

            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                shopManager.cancelShopSetup(playerId);
                player.sendMessage(ChatColor.YELLOW + "Fiyat girişi ve dükkan kurulumu iptal edildi.");
                plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) cancelled price input for shop %s.",
                        player.getName(), playerId, chestLocation));
                return;
            }

            try {
                String[] priceParts = message.split(":");
                if (priceParts.length != 2) {
                    player.sendMessage(ChatColor.RED + "Geçersiz format. Fiyatları ALIS_FIYATI:SATIS_FIYATI şeklinde girin (örn: 100:80 veya 50:-1). 'iptal' yazarak iptal edin.");
                    plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, invalid price parts: '%s'",
                            player.getName(), playerId, chestLocation, message));
                    return;
                }

                double buyPrice = Double.parseDouble(priceParts[0].trim());
                double sellPrice = Double.parseDouble(priceParts[1].trim());

                if ((buyPrice < 0 && buyPrice != -1) || (sellPrice < 0 && sellPrice != -1)) {
                    player.sendMessage(ChatColor.RED + "Geçersiz fiyatlar. Fiyatlar pozitif olmalı veya -1 (devre dışı) olmalı. Tekrar deneyin veya 'iptal' yazın.");
                    plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, invalid price values (negative but not -1): Buy:%.2f Sell:%.2f",
                            player.getName(), playerId, chestLocation, buyPrice, sellPrice));
                    return;
                }
                if (buyPrice == -1 && sellPrice == -1 && pendingShop.getShopMode() != null ) { // ShopMode might be null if setup is very early aborted
                    player.sendMessage(ChatColor.RED + "Bir dükkan ya satış yapmalı ya da alış yapmalı (veya ikisi de). İki fiyatı da -1 yapamazsınız. Tekrar deneyin veya 'iptal' yazın.");
                    plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, both prices -1. Buy:%.2f Sell:%.2f",
                            player.getName(), playerId, chestLocation, buyPrice, sellPrice));
                    return;
                }

                pendingShop.setBuyPrice(buyPrice);
                pendingShop.setSellPrice(sellPrice);
                plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) set prices for shop %s. Buy: %.2f, Sell: %.2f. Proceeding to finalize.",
                        player.getName(), playerId, chestLocation, buyPrice, sellPrice));

                player.sendMessage(ChatColor.GREEN + "Fiyatlar kabul edildi. Oyuncu Alış Fiyatı: " +
                        (buyPrice == -1 ? ChatColor.GRAY + "Devre Dışı" : ChatColor.GOLD + String.format("%.2f", buyPrice)) +
                        ChatColor.GREEN + ", Oyuncu Satış Fiyatı: " +
                        (sellPrice == -1 ? ChatColor.GRAY + "Devre Dışı" : ChatColor.GOLD + String.format("%.2f", sellPrice)));

                plugin.getPlayerShopSetupState().remove(playerId);
                plugin.getPlayerWaitingForSetupInput().remove(playerId);
                ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);

                if (buyPrice == -1 && initialStock != null) { // If shop is not buying from players, no need for initial stock from player for selling.
                    player.getInventory().addItem(initialStock.clone());
                    player.sendMessage(ChatColor.YELLOW + "Dükkanınız satış yapmayacağı için başlangıç stoğunuz iade edildi.");
                    plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s. Buy price is -1, initial stock %s returned.",
                            player.getName(), playerId, chestLocation, initialStock.getType()));
                    initialStock = null;
                }

                final ItemStack finalInitialStock = initialStock;
                // final Shop finalPendingShop = pendingShop; // pendingShop is already effectively final for the runnable

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Re-fetch pendingShop in case its state was altered by another thread, though unlikely here.
                        // For this specific flow, using the 'pendingShop' from outer scope is generally safe.
                        Shop currentPendingShopState = shopManager.getPendingShop(chestLocation);
                        if (currentPendingShopState == null || !currentPendingShopState.getOwnerUUID().equals(playerId)){
                            plugin.getLogger().severe(String.format("ShopSetupListener (PriceSetup-Runnable): Player %s (UUID: %s) for shop %s. Pending shop became null or owner changed before finalize. Critical error.",
                                    player.getName(), playerId, chestLocation));
                            player.sendMessage(ChatColor.RED + "Kritik bir hata oluştu, dükkan kurulamadı. Lütfen yöneticiye bildirin.");
                            shopManager.cancelShopSetup(playerId); // Ensure cleanup
                            return;
                        }
                        shopManager.finalizeShopSetup(
                                chestLocation,
                                player,
                                finalInitialStock
                        );
                        // Logging for finalization is done within finalizeShopSetup or after this runnable if a reference to the *finalized* shop is obtained.
                    }
                }.runTask(plugin);

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Fiyatlar için geçersiz sayı formatı. Örn: 10.5 veya -1. Tekrar deneyin veya 'iptal' yazın.");
                plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, NumberFormatException for prices: '%s'",
                        player.getName(), playerId, chestLocation, message));
            }
        }
    }

    private String getItemNameForMessages(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen Eşya";
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        String name = itemStack.getType().toString().toLowerCase().replace("_", " ");
        if (!name.isEmpty()) {
            String[] parts = name.split(" ");
            StringBuilder capitalizedName = new StringBuilder();
            for (String part : parts) {
                if (part.length() > 0) {
                    capitalizedName.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase()).append(" ");
                }
            }
            return capitalizedName.toString().trim();
        }
        return "Bilinmeyen Eşya";
    }
}