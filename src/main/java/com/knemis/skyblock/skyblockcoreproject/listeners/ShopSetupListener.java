// com/knemis/skyblock/skyblockcoreproject/listeners/ShopSetupListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Added import
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession; // Import session

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

    private static final int ITEM_SELECT_PLACEMENT_SLOT = 13;
    private static final int QUANTITY_PLACEMENT_SLOT = 22;
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
        UUID playerId = player.getUniqueId();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(viewTitleComponent);
        ItemStack clickedItem = event.getCurrentItem();
        String clickedItemName = (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) ?
                                 LegacyComponentSerializer.legacySection().serialize(clickedItem.getItemMeta().displayName()) :
                                 (clickedItem != null ? clickedItem.getType().name() : "null");

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) {
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) clicked in Shop Setup GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), playerId, viewTitle, event.getRawSlot(), clickedItemName));
            event.setCancelled(true);
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (session == null) {
                player.sendMessage(ChatColor.RED + "Shop setup session not found. Please start over.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s in SHOP_TYPE_TITLE but no session found.", player.getName()));
                player.closeInventory();
                return;
            }
            Location chestLocation = session.getChestLocation();
            Shop pendingShop = session.getPendingShop();

            if (pendingShop == null) { // Should not happen if session exists and is managed correctly
                player.sendMessage(ChatColor.RED + "Mağaza kurulum bilgisi bulunamadı. Lütfen tekrar deneyin.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in GUI '%s', but pendingShop is null in session for location %s.",
                        player.getName(), playerId, viewTitle, Shop.locationToString(chestLocation)));
                player.closeInventory();
                shopManager.cancelShopSetup(playerId); // Cancel to clean up session
                return;
            }

            int rawSlot = event.getRawSlot();
            boolean intentSet = false;
            if (rawSlot == PLAYER_BUY_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(false);
                intentSet = true;
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s set intent: BUY_ONLY for shop at %s.", player.getName(), Shop.locationToString(chestLocation)));
            } else if (rawSlot == PLAYER_SELL_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(false);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s set intent: SELL_ONLY for shop at %s.", player.getName(), Shop.locationToString(chestLocation)));
            } else if (rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s set intent: BUY_AND_SELL for shop at %s.", player.getName(), Shop.locationToString(chestLocation)));
            }

            if (intentSet) {
                session.setCurrentGuiTitle(ShopSetupGUIManager.ITEM_SELECT_TITLE.toString()); 
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) clicked invalid slot %d in SHOP_TYPE_TITLE GUI for shop %s.",
                        player.getName(), playerId, rawSlot, Shop.locationToString(chestLocation)));
            }
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE.toString())) { // Compare string with string
            if (session == null) {
                player.sendMessage(ChatColor.RED + "Shop setup session not found. Please start over.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s in ITEM_SELECT_TITLE but no session found.", player.getName()));
                event.setCancelled(true); player.closeInventory(); return;
            }
            handleItemSelectionGuiClickLogic(event, player, topInventory, session);
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE.toString())) { // Compare string with string
            if (session == null) {
                player.sendMessage(ChatColor.RED + "Shop setup session not found. Please start over.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s in QUANTITY_INPUT_TITLE but no session found.", player.getName()));
                event.setCancelled(true); player.closeInventory(); return;
            }
            handleQuantityInputGuiClickLogic(event, player, topInventory, session);
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.CONFIRMATION_TITLE.toString())) { // Compare string with string
             if (session == null) {
                player.sendMessage(ChatColor.RED + "Shop setup session not found. Please start over.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s in CONFIRMATION_TITLE but no session found.", player.getName()));
                event.setCancelled(true); player.closeInventory(); return;
            }
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) clicked in Shop Setup GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), playerId, viewTitle, event.getRawSlot(), clickedItemName));
            event.setCancelled(true);

            Shop pendingShop = session.getPendingShop();
            Location chestLocation = session.getChestLocation(); // From session

            if (pendingShop == null || session.getExpectedInputType() != ShopSetupGUIManager.InputType.PRICE) {
                player.sendMessage(ChatColor.RED + "Onaylama hatası. Kurulum bilgileri eksik veya yanlış adım.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in confirmation GUI for %s, but state error. PendingShop: %s, ExpectedInput: %s",
                        player.getName(), playerId, Shop.locationToString(chestLocation), pendingShop, session.getExpectedInputType()));
                player.closeInventory();
                shopManager.cancelShopSetup(playerId);
                return;
            }

            if (event.getCurrentItem() != null) {
                Material itemTypeForLog = pendingShop.getTemplateItemStack() != null ? pendingShop.getTemplateItemStack().getType() : Material.AIR;
                double buyPriceForLog = pendingShop.getBuyPrice();
                double sellPriceForLog = pendingShop.getSellPrice();
                int quantityForLog = pendingShop.getBundleAmount();
                // ShopType typeForLog = pendingShop.getShopType(); // Removed ShopType

                if (event.getCurrentItem().getType() == Material.GREEN_WOOL) {
                    shopManager.finalizeShopSetup(playerId); 
                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) CONFIRMED shop setup for item %s at %s. BuyPrice: %.2f, SellPrice: %.2f, Quantity: %d. Intents: Buy=%b, Sell=%b",
                            player.getName(), playerId, itemTypeForLog, Shop.locationToString(chestLocation), buyPriceForLog, sellPriceForLog, quantityForLog, session.isIntentToAllowPlayerBuy(), session.isIntentToAllowPlayerSell()));
                } else if (event.getCurrentItem().getType() == Material.RED_WOOL) {
                    player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumu iptal edildi.");
                    shopManager.cancelShopSetup(playerId);
                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) CANCELLED shop setup for location %s via confirmation GUI (red wool).",
                            player.getName(), playerId, Shop.locationToString(chestLocation)));
                }
                player.closeInventory(); // Close after processing
            }
        }
    }

    private void handleItemSelectionGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory, ShopSetupSession session) {
        Inventory clickedInventory = event.getClickedInventory();
        Location shopLocation = session.getChestLocation();
        String locStr = Shop.locationToString(shopLocation);

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    if (guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT) == null || guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT).getType() == Material.AIR) {
                        guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, itemToMove.clone());
                        event.setCurrentItem(null);
                        player.updateInventory();
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) shift-clicked item %s into ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), itemToMove.getType(), locStr));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Eşya seçme yuvası zaten dolu. Lütfen önce mevcut eşyayı alın.");
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) tried to shift-click item %s into occupied ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), itemToMove.getType(), locStr));
                    }
                }
                event.setCancelled(true);
            } else {
                event.setCancelled(false);
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == ITEM_SELECT_PLACEMENT_SLOT) {
                InventoryAction action = event.getAction();
                ItemStack cursorItem = event.getCursor();
                ItemStack currentItemInSlot = event.getCurrentItem();

                if (action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME ||
                    action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_HALF ||
                    action == InventoryAction.SWAP_WITH_CURSOR) {
                    event.setCancelled(false);
                    if ((action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME) && cursorItem != null && cursorItem.getType() != Material.AIR) {
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) placed item %s into ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), cursorItem.getType(), locStr));
                    } else if (action.name().startsWith("PICKUP_") && currentItemInSlot != null && currentItemInSlot.getType() != Material.AIR) {
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) picked up item %s from ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), currentItemInSlot.getType(), locStr));
                    } else if (action == InventoryAction.SWAP_WITH_CURSOR) {
                         plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) swapped item in cursor (%s) with item in slot (%s) in ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), cursorItem != null ? cursorItem.getType() : "EMPTY", currentItemInSlot != null ? currentItemInSlot.getType() : "EMPTY", locStr));
                    }
                } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    if (currentItemInSlot != null && currentItemInSlot.getType() != Material.AIR) {
                         plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) shift-clicked item %s out of ITEM_SELECT_PLACEMENT_SLOT for shop at %s.",
                                player.getName(), player.getUniqueId(), currentItemInSlot.getType(), locStr));
                    }
                    event.setCancelled(false);
                }
                else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
                plugin.getLogger().finer(String.format("ShopSetupListener: Player %s (UUID: %s) clicked non-interactive GUI slot %d in ITEM_SELECT_TITLE for shop %s. Action: %s. Cancelled.",
                        player.getName(), player.getUniqueId(), event.getRawSlot(), locStr, event.getAction().name()));
            }
        } else if (clickedInventory == null && event.getAction().name().contains("DROP")) {
             event.setCancelled(true);
        }
    }

    private void handleQuantityInputGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory, ShopSetupSession session) {
        Inventory clickedInventory = event.getClickedInventory();
        Location chestLocation = session.getChestLocation();
        Shop pendingShop = session.getPendingShop();
        String locStr = Shop.locationToString(chestLocation);

        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "Kurulum hatası: Satılacak eşya şablonu bulunamadı. Lütfen baştan başlayın.");
            plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) in quantity GUI for shop %s, but pendingShop or templateItem is null in session. Cancelling.",
                    player.getName(), player.getUniqueId(), locStr));
            player.closeInventory();
            event.setCancelled(true);
            shopManager.cancelShopSetup(player.getUniqueId());
            return;
        }
        ItemStack templateItem = pendingShop.getTemplateItemStack();

        if (event.getRawSlot() == CONFIRM_BUTTON_SLOT_QUANTITY && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_WOOL) {
            event.setCancelled(true);
            ItemStack quantityItemStack = guiInventory.getItem(QUANTITY_PLACEMENT_SLOT);
            if (quantityItemStack != null && quantityItemStack.isSimilar(templateItem) && quantityItemStack.getAmount() > 0) {
                pendingShop.setItemQuantityForPrice(quantityItemStack.getAmount());
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) confirmed quantity %d for item %s for shop at %s. Transitioning to price input.",
                        player.getName(), player.getUniqueId(), quantityItemStack.getAmount(), templateItem.getType(), locStr));
                session.setCurrentGuiTitle(ShopSetupGUIManager.PRICE_INPUT_TITLE.toString()); // Update state
                session.setExpectedInputType(ShopSetupGUIManager.InputType.PRICE); // Update state
                shopSetupGUIManager.openPriceInputPrompt(player, pendingShop);
            } else {
                player.sendMessage(ChatColor.RED + "Lütfen miktar yuvasına doğru türde (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") ve geçerli miktarda eşya koyun.");
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) clicked confirm in quantity GUI for shop %s, but item/quantity was invalid. ItemInSlot: %s, Template: %s",
                        player.getName(), player.getUniqueId(), locStr, quantityItemStack, templateItem));
            }
            return;
        }
        // ... (rest of the logic for player inventory and GUI interactions, ensuring event.setCancelled as appropriate)
        // This part largely remains the same but relies on `templateItem` from `pendingShop` (from session)
        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true); // Manage manually
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
                }
            } else {
                event.setCancelled(false); // Default player inv interactions
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == QUANTITY_PLACEMENT_SLOT) {
                ItemStack cursorItem = event.getCursor();
                if (event.getAction().name().startsWith("PLACE_")) {
                    if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                        if (cursorItem.isSimilar(templateItem)) {
                            event.setCancelled(false);
                        } else {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                            event.setCancelled(true);
                        }
                    } else { // Placing AIR (removing item)
                        event.setCancelled(false);
                    }
                } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR && !cursorItem.isSimilar(templateItem)) {
                        player.sendMessage(ChatColor.RED + "Bu yuvayla sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya değiştirebilirsiniz!");
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false);
                    }
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(false); // Allow shift-click out
                } else {
                    event.setCancelled(true); // Other actions on this slot
                }
            } else if (event.getRawSlot() != CONFIRM_BUTTON_SLOT_QUANTITY) { // Any other slot in GUI that is not confirm
                event.setCancelled(true);
                plugin.getLogger().finer(String.format("ShopSetupListener: Player %s (UUID: %s) clicked non-interactive slot %d in QUANTITY_INPUT_TITLE GUI for shop %s.",
                        player.getName(), player.getUniqueId(), event.getRawSlot(), locStr));
            }
            // Click on confirm button is handled at the start of the method
        } else {
            if(clickedInventory != null) event.setCancelled(true); // Clicked outside GUI but not in player inventory
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (session == null) {
            // Not in a shop setup session, or session was already cleaned up.
            // Check if it's an admin GUI close that needs specific handling from ShopListener
            Component viewTitleComponentAdmin = event.getView().title();
            String viewTitleAdmin = LegacyComponentSerializer.legacySection().serialize(viewTitleComponentAdmin);
            if (viewTitleAdmin.equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE) || viewTitleAdmin.equals(LegacyComponentSerializer.legacySection().serialize(ShopVisitGUIManager.SHOP_VISIT_TITLE))) {
                // These are handled by ShopListener's onInventoryClose, so just return.
                return;
            }
            plugin.getLogger().finer("ShopSetupListener: onInventoryClose for " + player.getName() + ", but no active setup session. GUI: " + viewTitleAdmin);
            return;
        }

        Location chestLocation = session.getChestLocation();
        Shop pendingShop = session.getPendingShop();
        String locStr = Shop.locationToString(chestLocation);
        String currentSessionGuiTitle = session.getCurrentGuiTitle(); // Get title from session
        String closedViewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());

        // Only proceed if the closed GUI matches what the session thought was open,
        // or if currentSessionGuiTitle is null (e.g. after chat input).
        if (currentSessionGuiTitle != null && !currentSessionGuiTitle.equals(closedViewTitle)) {
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed GUI '%s' but session expected '%s'. Ignoring close for this specific logic, might be handled by another listener or subsequent GUI open.",
                                                   player.getName(), closedViewTitle, currentSessionGuiTitle));
            return;
        }

        boolean isStillInSetupChain = true; // Assume still in chain unless explicitly cancelled

        if (closedViewTitle.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE.toString())) {
            if (pendingShop == null) {
                plugin.getLogger().severe(String.format("ShopSetupListener: Player %s (UUID: %s) closed ITEM_SELECT_TITLE for shop %s, but pendingShop is NULL in session. Cancelling.",
                        player.getName(), playerId, locStr));
                shopManager.cancelShopSetup(playerId); // This will remove session
                return;
            }
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_SELECT_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                pendingShop.setTemplateItemStack(itemInSlot.clone());
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s (UUID: %s) selected item %s for shop %s. Transitioning to quantity selection.",
                        player.getName(), playerId, itemInSlot.getType(), locStr));
                final Shop finalPendingShop = pendingShop;
                final Player finalPlayer = player;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Re-fetch session in case it was invalidated or changed
                        ShopSetupSession currentSession = shopSetupGUIManager.getPlayerSession(finalPlayer.getUniqueId());
                        if (currentSession != null && currentSession.getPendingShop() == finalPendingShop) {
                             shopSetupGUIManager.openQuantityInputMenu(finalPlayer, finalPendingShop);
                        } else {
                            plugin.getLogger().warning("ShopSetupListener: Session changed or invalidated before opening quantity menu for " + finalPlayer.getName());
                        }
                    }
                }.runTask(plugin);
            } else {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) closed ITEM_SELECT_TITLE for shop %s with no item selected. Cancelling setup.",
                        player.getName(), playerId, locStr));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            }
        } else if (closedViewTitle.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE.toString())) {
            if (pendingShop == null) {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed QUANTITY_INPUT_TITLE for shop %s, but pendingShop is null in session. Cancelling.", player.getName(), locStr));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
                return;
            }
            if (pendingShop.getBundleAmount() <= 0 && session.getExpectedInputType() != ShopSetupGUIManager.InputType.PRICE) {
                ItemStack itemInQuantitySlot = event.getInventory().getItem(QUANTITY_PLACEMENT_SLOT);
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed QUANTITY_INPUT_TITLE for shop %s. Quantity not set/confirmed (Bundle: %d). Cancelling. Item in slot: %s",
                        player.getName(), playerId, locStr, pendingShop.getBundleAmount(), itemInQuantitySlot != null ? itemInQuantitySlot.toString() : "null"));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            } else if (session.getExpectedInputType() == ShopSetupGUIManager.InputType.PRICE) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed QUANTITY_INPUT_TITLE for shop %s. Quantity %d was confirmed. Proceeding with price input.",
                        player.getName(), playerId, locStr, pendingShop.getBundleAmount()));
            } else if (pendingShop.getBundleAmount() > 0) {
                 plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed QUANTITY_INPUT_TITLE for shop %s. Quantity %d set. Awaiting price input or confirmation.",
                        player.getName(), playerId, locStr, pendingShop.getBundleAmount()));
            } else {
                 plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed QUANTITY_INPUT_TITLE for shop %s in unhandled state. Bundle: %d, ExpectedInput: %s. Cancelling.",
                        player.getName(), playerId, locStr, pendingShop.getBundleAmount(), session.getExpectedInputType()));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            }
        } else if (closedViewTitle.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE.toString())) { // Compare string with string
            // Check if intent flags were set. If not, it means player closed GUI without selecting type.
            if (pendingShop != null && !session.isIntentToAllowPlayerBuy() && !session.isIntentToAllowPlayerSell()) {
                plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE for shop %s. No buy/sell intent selected. Cancelling.",
                        player.getName(), playerId, locStr));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            } else if (pendingShop != null) {
                 plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE for shop %s. Intents: Buy=%b, Sell=%b. Proceeding.",
                        player.getName(), playerId, locStr, session.isIntentToAllowPlayerBuy(), session.isIntentToAllowPlayerSell()));
            } else { // pendingShop is null
                 plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE for shop %s, but pendingShop is null in session. Cancelling.",
                        player.getName(), playerId, locStr));
                shopManager.cancelShopSetup(playerId);
                isStillInSetupChain = false;
            }
        } else if (closedViewTitle.equals(ShopSetupGUIManager.CONFIRMATION_TITLE.toString())) {
            // If confirmation GUI is closed without clicking green/red wool, it implies cancellation
            // if the setup hasn't been finalized or explicitly cancelled by button click yet.
            if (session.getExpectedInputType() == ShopSetupGUIManager.InputType.PRICE || pendingShop != null && !pendingShop.isSetupComplete()) {
                  plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed CONFIRMATION_TITLE for shop %s before final confirmation. Cancelling setup.",
                        player.getName(), playerId, locStr));
                shopManager.cancelShopSetup(playerId); // This will remove the session.
            } else {
                 plugin.getLogger().finer(String.format("ShopSetupListener: Player %s closed CONFIRMATION_TITLE, but setup already finalized or cancelled. No action needed from here.", player.getName()));
            }
            isStillInSetupChain = false;
        }

        // If after all specific GUI logic, the session still exists but isStillInSetupChain is false,
        // it means a specific handler decided to cancel, and cancelShopSetup (which removes session) was called.
        // If session still exists AND isStillInSetupChain is true, but the current expected input is null
        // (meaning not waiting for chat input like price), it might be an incomplete step.
        if (isStillInSetupChain && shopSetupGUIManager.getPlayerSession(playerId) != null && session.getExpectedInputType() == null &&
            !closedViewTitle.equals(ShopSetupGUIManager.CONFIRMATION_TITLE.toString()) && // Confirmation is the end, or handled by click
            pendingShop != null && !pendingShop.isSetupComplete() // And shop not yet completed
           ) {
            plugin.getLogger().warning(String.format("ShopSetupListener: Player %s closed GUI '%s' for shop %s. Setup seems incomplete and no specific input is pending. Cancelling as fallback.",
                    player.getName(), playerId, closedViewTitle, locStr));
            shopManager.cancelShopSetup(playerId);
        }
        session.setCurrentGuiTitle(null); // Clear current GUI from session as it's closed.
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (session == null) {
            // Not in a shop setup session this listener is concerned with
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        String viewTitle = LegacyComponentSerializer.legacySection().serialize(viewTitleComponent);
        boolean isItemSelectGui = viewTitle.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE.toString());
        boolean isQuantityGui = viewTitle.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE.toString());
        
        if (!isItemSelectGui && !isQuantityGui) return; // Not a GUI we manage drags for here

        Location shopLocation = session.getChestLocation();
        Shop pendingShop = session.getPendingShop();
        String locStr = Shop.locationToString(shopLocation);

        int targetPlacementSlot = isItemSelectGui ? ITEM_SELECT_PLACEMENT_SLOT : (isQuantityGui ? QUANTITY_PLACEMENT_SLOT : -1);
        boolean affectsOnlyPlacementSlot = true;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topInventory.getSize()) { // Drag involves top inventory
                if (rawSlot != targetPlacementSlot) {
                    affectsOnlyPlacementSlot = false;
                    break;
                }
            }
        }

        ItemStack draggedItem = event.getOldCursor(); // The item being dragged

        if (!affectsOnlyPlacementSlot) {
            event.setCancelled(true);
            plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) invalid drag (not exclusively to placement slot or involves other GUI slots) in GUI %s for shop %s. Cancelled. Dragged item: %s",
                    player.getName(), playerId, viewTitle, locStr, (draggedItem != null ? draggedItem.getType().name() : "null")));
        } else { // Drag is exclusively to the target placement slot
            if (isQuantityGui) {
                if (pendingShop == null || pendingShop.getTemplateItemStack() == null) {
                     event.setCancelled(true);
                     plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) drag in QUANTITY_INPUT_TITLE for shop %s but pendingShop/templateItem is null in session. Cancelled.",
                                player.getName(), playerId, locStr));
                     return;
                }
                if (draggedItem != null && draggedItem.getType() != Material.AIR && !draggedItem.isSimilar(pendingShop.getTemplateItemStack())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(pendingShop.getTemplateItemStack()) + ChatColor.RED + ") türünde eşya sürükleyebilirsiniz!");
                    plugin.getLogger().warning(String.format("ShopSetupListener: Player %s (UUID: %s) invalid drag (item %s not similar to template %s) in QUANTITY_INPUT_TITLE GUI for shop %s. Cancelled.",
                            player.getName(), playerId, draggedItem.getType().name(), pendingShop.getTemplateItemStack().getType().name(), locStr));
                }
                // If valid, event.setCancelled(false) is default - allow it.
            }
            // For item selection GUI, any item can be dragged into the slot, so no specific check beyond placement.
        }
    }

    @EventHandler
    public void onPlayerChatForPrice(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (session == null || session.getExpectedInputType() != ShopSetupGUIManager.InputType.PRICE) {
            // Not our target for price input, or admin input which is handled in ShopListener
            if (plugin.getPlayerWaitingForAdminInput().containsKey(playerId)) {
                 // Admin input handling is separate and should remain in ShopListener if it uses its own map.
                 // This check avoids interference if ShopListener's admin input logic is still active.
            }
            return;
        }

        Location chestLocation = session.getChestLocation();
        Shop pendingShop = session.getPendingShop();
        String locStr = Shop.locationToString(chestLocation);
        String message = event.getMessage();

        plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) providing price input for shop %s. Message: '%s'",
                player.getName(), playerId, locStr, message));
        event.setCancelled(true);

        if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getBundleAmount() <= 0) {
            player.sendMessage(ChatColor.RED + "Mağaza kurulum hatası (eksik bilgi). Baştan başla.");
            plugin.getLogger().warning(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) for shop %s, but pendingShop/template/bundleAmount invalid in session. Pending: %s",
                    player.getName(), playerId, locStr, pendingShop));
            shopManager.cancelShopSetup(playerId);
            return;
        }

        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            shopManager.cancelShopSetup(playerId);
            player.sendMessage(ChatColor.YELLOW + "Fiyat girişi ve dükkan kurulumu iptal edildi.");
            plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) cancelled price input for shop %s.",
                    player.getName(), playerId, locStr));
            return;
        }

        try {
            double buyPrice = -1.0;
            double sellPrice = -1.0;

            if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                String[] priceParts = message.split(":");
                if (priceParts.length != 2) {
                    player.sendMessage(ChatColor.RED + "Geçersiz format. Fiyatları ALIS_FIYATI:SATIS_FIYATI şeklinde girin (örn: 100:80 veya 50:-1). 'iptal' yazarak iptal edin.");
                    return;
                }
                buyPrice = Double.parseDouble(priceParts[0].trim());
                sellPrice = Double.parseDouble(priceParts[1].trim());

                if ((buyPrice < 0 && buyPrice != -1) || (sellPrice < 0 && sellPrice != -1)) {
                    player.sendMessage(ChatColor.RED + "Geçersiz fiyatlar. Fiyatlar pozitif olmalı veya -1 (devre dışı) olmalı.");
                    return;
                }
                if (buyPrice == -1 && sellPrice == -1) {
                    player.sendMessage(ChatColor.RED + "Bir dükkan ya satış yapmalı ya da alış yapmalı. İki fiyatı da -1 yapamazsınız.");
                    return;
                }
            } else if (session.isIntentToAllowPlayerBuy()) {
                buyPrice = Double.parseDouble(message.trim());
                if (buyPrice < 0) {
                    player.sendMessage(ChatColor.RED + "Alış fiyatı pozitif bir değer olmalı.");
                    return;
                }
                sellPrice = -1.0; // Explicitly set sell price to disabled
            } else if (session.isIntentToAllowPlayerSell()) {
                sellPrice = Double.parseDouble(message.trim());
                if (sellPrice < 0) {
                    player.sendMessage(ChatColor.RED + "Satış fiyatı pozitif bir değer olmalı.");
                    return;
                }
                buyPrice = -1.0; // Explicitly set buy price to disabled
            } else {
                // Should not happen if intents are set correctly
                plugin.getLogger().severe("Price input requested but no buy/sell intent set for player " + playerId);
                player.sendMessage(ChatColor.RED + "Fiyat giriş hatası: Dükkan türü belirlenmemiş.");
                shopManager.cancelShopSetup(playerId);
                return;
            }

            pendingShop.setBuyPrice(buyPrice);
            pendingShop.setSellPrice(sellPrice);
            session.setExpectedInputType(null); 
            session.setCurrentGuiTitle(ShopSetupGUIManager.CONFIRMATION_TITLE.toString()); 

            plugin.getLogger().info(String.format("ShopSetupListener (PriceSetup): Player %s (UUID: %s) set prices for shop %s. Buy: %.2f, Sell: %.2f. Proceeding to confirmation.",
                    player.getName(), playerId, locStr, buyPrice, sellPrice));
            
            String priceConfirmationMessage = ChatColor.GREEN + "Fiyatlar kabul edildi. ";
            if(session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                 priceConfirmationMessage += "Alış: " + (buyPrice == -1 ? "Devre Dışı" : String.format("%.2f", buyPrice)) +
                                          ", Satış: " + (sellPrice == -1 ? "Devre Dışı" : String.format("%.2f", sellPrice));
            } else if (session.isIntentToAllowPlayerBuy()) {
                priceConfirmationMessage += "Oyuncu Alış Fiyatı: " + String.format("%.2f", buyPrice);
            } else if (session.isIntentToAllowPlayerSell()) {
                priceConfirmationMessage += "Oyuncu Satış Fiyatı: " + String.format("%.2f", sellPrice);
            }
            player.sendMessage(priceConfirmationMessage);

            final Player finalPlayer = player; 
            final Shop finalPendingShop = pendingShop; // For runnable
            new BukkitRunnable() {
                @Override
                public void run() {
                    ShopSetupSession currentSession = shopSetupGUIManager.getPlayerSession(finalPlayer.getUniqueId());
                    if (currentSession == null || currentSession.getPendingShop() != finalPendingShop) {
                        plugin.getLogger().warning("Session changed or ended before opening confirmation for " + finalPlayer.getName());
                        return;
                    }
                    shopSetupGUIManager.openConfirmationMenu(finalPlayer, finalPendingShop, finalPendingShop.getBuyPrice(), finalPendingShop.getSellPrice());
                }
            }.runTask(plugin);

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Fiyatlar için geçersiz sayı formatı. Örn: 10.5 veya -1.");
        }
    }

    private String getItemNameForMessages(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen Eşya";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            try { // Paper Adventure Component
                Component displayNameComponent = meta.displayName();
                if (displayNameComponent != null) return LegacyComponentSerializer.legacySection().serialize(displayNameComponent);
            } catch (NoSuchMethodError e) { // Older Bukkit String display name
                 if (meta.hasDisplayName()) return meta.getDisplayName();
            }
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