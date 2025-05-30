package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // Keep if admin GUIs are relevant for onInventoryClose
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Keep if relevant for onInventoryClose
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;

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
// import java.util.logging.Level; // Not used directly, consider removing

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    private static final int PLAYER_BUY_SHOP_SLOT = 11; // Owner Sells Items
    private static final int PLAYER_SELL_SHOP_SLOT = 13; // Owner Buys Items
    private static final int PLAYER_BUY_SELL_SHOP_SLOT = 15; // Dual function

    private static final int ITEM_SELECT_PLACEMENT_SLOT = 13;

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

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (session == null) {
            if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE))) {
                event.setCancelled(true);
                player.closeInventory();
                plugin.getLogger().warning("ShopSetupListener: Player " + player.getName() + " clicked in setup GUI '" + viewTitle + "' without a session. Closed inventory.");
            }
            return;
        }

        String sessionGuiTitle = session.getCurrentGuiTitle();
        if (sessionGuiTitle == null || !sessionGuiTitle.equals(viewTitle)) {
            if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT)) ||
                    viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE))) {
                plugin.getLogger().warning("ShopSetupListener: Mismatch between actual view title '" + viewTitle +
                        "' and session title '" + sessionGuiTitle + "' for player " + player.getName() + ". Cancelling event.");
                event.setCancelled(true);
            }
            return;
        }

        if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE))) {
            event.setCancelled(true);
            ItemStack clickedStack = event.getCurrentItem();
            if (clickedStack == null || clickedStack.getType() == Material.AIR) return;

            Shop pendingShop = session.getPendingShop();
            if (pendingShop == null) {
                player.sendMessage(ChatColor.RED + "Shop setup error (no pending shop). Please start over.");
                player.closeInventory();
                shopManager.cancelShopSetup(playerId);
                return;
            }

            int rawSlot = event.getRawSlot();
            boolean intentSet = false;
            if (rawSlot == PLAYER_BUY_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(false);
                intentSet = true;
            } else if (rawSlot == PLAYER_SELL_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(false);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
            } else if (rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
            }

            if (intentSet) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s set intent: Buy=%b, Sell=%b for shop at %s.",
                        player.getName(), session.isIntentToAllowPlayerBuy(), session.isIntentToAllowPlayerSell(), Shop.locationToString(session.getChestLocation())));
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
            }

        } else if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE))) {
            handleItemSelectionGuiClickLogic(event, player, topInventory, session);

        } else if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.QUANTITY_INPUT_TITLE))) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "This setup step is being updated. Please restart shop setup.");
            shopManager.cancelShopSetup(playerId);

        } else if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE))) {
            event.setCancelled(true);
            Shop pendingShop = session.getPendingShop();
            if (pendingShop == null) {
                player.sendMessage(ChatColor.RED + "Confirmation error. Setup info missing. Please restart.");
                player.closeInventory();
                shopManager.cancelShopSetup(playerId);
                return;
            }

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null) {
                if (currentItem.getType() == Material.GREEN_WOOL) {
                    shopManager.finalizeShopSetup(playerId);
                } else if (currentItem.getType() == Material.RED_WOOL) {
                    player.sendMessage(ChatColor.YELLOW + "Shop setup cancelled.");
                    shopManager.cancelShopSetup(playerId);
                }
            }
        }
    }

    private void handleItemSelectionGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory, ShopSetupSession session) {
        Inventory clickedInventory = event.getClickedInventory();
        Shop pendingShop = session.getPendingShop();

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    ItemStack currentItemInSlot = guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT);
                    if (currentItemInSlot == null || currentItemInSlot.getType() == Material.GRAY_STAINED_GLASS_PANE || currentItemInSlot.getType() == Material.AIR) {

                        event.setCancelled(true);
                        ItemStack movedItemClone = itemToMove.clone();
                        guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, movedItemClone);
                        event.setCurrentItem(null);
                        player.updateInventory();

                        pendingShop.setTemplateItemStack(movedItemClone.clone());
                        plugin.getLogger().info(String.format("ShopSetupListener: Player %s auto-selected item %s (shift-click) for shop at %s. Proceeding to price/quantity setup.",
                                player.getName(), movedItemClone.getType(), Shop.locationToString(session.getChestLocation())));
                        shopSetupGUIManager.openPriceQuantityAnvilGUI(player, pendingShop);
                        return;
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Item selection slot is already full. Please remove the current item first.");
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(false);
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == ITEM_SELECT_PLACEMENT_SLOT) {
                ItemStack cursorItem = event.getCursor();
                // ItemStack currentItemInSlot = event.getCurrentItem(); // Not used in this revised logic

                boolean isPlacingAction = event.getAction() == InventoryAction.PLACE_ONE ||
                        event.getAction() == InventoryAction.PLACE_ALL ||
                        event.getAction() == InventoryAction.PLACE_SOME ||
                        (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR);

                if (isPlacingAction) {
                    event.setCancelled(true);
                    ItemStack itemToPlace = cursorItem.clone();

                    pendingShop.setTemplateItemStack(itemToPlace.clone());
                    guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, itemToPlace);
                    player.setItemOnCursor(null);

                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s auto-selected item %s (direct place) for shop at %s. Proceeding to price/quantity setup.",
                            player.getName(), itemToPlace.getType(), Shop.locationToString(session.getChestLocation())));
                    shopSetupGUIManager.openPriceQuantityAnvilGUI(player, pendingShop);

                } else if (event.getAction().name().startsWith("PICKUP_") ||
                        (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && (cursorItem == null || cursorItem.getType() == Material.AIR)) ||
                        event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(false);
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else if (clickedInventory == null && event.getAction().name().contains("DROP")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        String closedViewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());

        if (session == null) {
            String itemSelectTitle = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE);
            String shopTypeTitle = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE);
            if (closedViewTitle.equals(itemSelectTitle) || closedViewTitle.equals(shopTypeTitle)) {
                plugin.getLogger().warning("ShopSetupListener: " + player.getName() + " closed setup GUI '" + closedViewTitle + "' but session was null.");
            }
            return;
        }

        String sessionGuiTitle = session.getCurrentGuiTitle();
        Shop pendingShop = session.getPendingShop();

        if (sessionGuiTitle != null && !sessionGuiTitle.equals(closedViewTitle)) {
            String anvilTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT);
            if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE)) &&
                    sessionGuiTitle.equals(anvilTitleSerialized)) {
                plugin.getLogger().fine("ShopSetupListener: Player " + player.getName() + " closed ITEM_SELECT_TITLE, but session already advanced to ANVIL. Auto-forwarding likely handled it.");
                return;
            }
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed GUI '%s' but session expected '%s'. Ignoring close for this specific logic.",
                    player.getName(), closedViewTitle, sessionGuiTitle));
            return;
        }

        if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE))) {
            if (pendingShop == null) {
                shopManager.cancelShopSetup(playerId); return;
            }
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_SELECT_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR && itemInSlot.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                pendingShop.setTemplateItemStack(itemInSlot.clone());

                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed ITEM_SELECT_TITLE with item %s in slot. Proceeding to Anvil (fallback).",
                        player.getName(), itemInSlot.getType()));

                final Player finalPlayer = player;
                final Shop finalPendingShop = pendingShop;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ShopSetupSession currentSession = shopSetupGUIManager.getPlayerSession(finalPlayer.getUniqueId());
                        if (currentSession != null && currentSession.getPendingShop() == finalPendingShop) {
                            shopSetupGUIManager.openPriceQuantityAnvilGUI(finalPlayer, finalPendingShop);
                        } else {
                            plugin.getLogger().warning("ShopSetupListener: Session changed or invalidated before fallback opening of Anvil GUI for " + finalPlayer.getName());
                        }
                    }
                }.runTask(plugin);
            } else {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed ITEM_SELECT_TITLE with no valid item selected. Cancelling setup.", player.getName()));
                shopManager.cancelShopSetup(playerId);
            }
        } else if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE))) {
            if (pendingShop != null && !session.isIntentToAllowPlayerBuy() && !session.isIntentToAllowPlayerSell()) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE. No buy/sell intent selected. Cancelling.", player.getName()));
                shopManager.cancelShopSetup(playerId);
            } else if (pendingShop == null) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE, but pendingShop is null. Cancelling.", player.getName()));
                shopManager.cancelShopSetup(playerId);
            }
        } else if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE))) {
            if (pendingShop != null && !pendingShop.isSetupComplete() && session.getExpectedInputType() != null) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed CONFIRMATION_TITLE before final confirmation/cancellation. Cancelling setup.", player.getName()));
                shopManager.cancelShopSetup(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(player.getUniqueId());

        if (session == null) return;

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        String viewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        String itemSelectTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE);

        if (!viewTitle.equals(itemSelectTitleSerialized)) {
            return;
        }

        boolean isItemSelectGui = viewTitle.equals(itemSelectTitleSerialized);
        int targetPlacementSlot = isItemSelectGui ? ITEM_SELECT_PLACEMENT_SLOT : -1;

        if (targetPlacementSlot == -1) return;

        boolean affectsOnlyPlacementSlot = true;
        boolean affectsPlacementSlotAtAll = false;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topInventory.getSize()) {
                if (rawSlot == targetPlacementSlot) {
                    affectsPlacementSlotAtAll = true;
                } else {
                    affectsOnlyPlacementSlot = false;
                }
            }
        }

        if (!affectsPlacementSlotAtAll || !affectsOnlyPlacementSlot) {
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerChatForPrice(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);

        if (session == null || session.getExpectedInputType() != ShopSetupGUIManager.InputType.PRICE) {
            return;
        }

        Shop pendingShop = session.getPendingShop();
        String message = event.getMessage();
        event.setCancelled(true);

        if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getBundleAmount() <= 0) {
            player.sendMessage(ChatColor.RED + "Shop setup error (missing info). Please restart.");
            shopManager.cancelShopSetup(playerId);
            return;
        }

        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            shopManager.cancelShopSetup(playerId);
            player.sendMessage(ChatColor.YELLOW + "Price input and shop setup cancelled.");
            return;
        }

        try {
            double buyPrice = -1.0;
            double sellPrice = -1.0;
            boolean priceValid = false;

            if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                String[] priceParts = message.split(":");
                if (priceParts.length != 2) {
                    player.sendMessage(ChatColor.RED + "Invalid format. Use BUY_PRICE:SELL_PRICE (e.g., 100:80 or -1:70). Type 'cancel' to abort."); return;
                }
                buyPrice = Double.parseDouble(priceParts[0].trim());
                sellPrice = Double.parseDouble(priceParts[1].trim());
                if (!((buyPrice >= 0 || buyPrice == -1) && (sellPrice >= 0 || sellPrice == -1) && !(buyPrice == -1 && sellPrice == -1))) {
                    player.sendMessage(ChatColor.RED + "Invalid prices. Must be positive or -1 (disabled), and not both -1."); return;
                }
                priceValid = true;
            } else if (session.isIntentToAllowPlayerBuy()) {
                buyPrice = Double.parseDouble(message.trim());
                if (buyPrice < 0) { player.sendMessage(ChatColor.RED + "Buy price must be positive."); return; }
                sellPrice = -1.0; priceValid = true;
            } else if (session.isIntentToAllowPlayerSell()) {
                sellPrice = Double.parseDouble(message.trim());
                if (sellPrice < 0) { player.sendMessage(ChatColor.RED + "Sell price must be positive."); return; }
                buyPrice = -1.0; priceValid = true;
            }

            if(priceValid) {
                pendingShop.setBuyPrice(buyPrice);
                pendingShop.setSellPrice(sellPrice);
                session.setExpectedInputType(null);

                player.sendMessage(ChatColor.GREEN + "Prices accepted. Proceeding to confirmation.");
                final Player finalPlayer = player;
                final Shop finalPendingShop = pendingShop;
                final double finalBuyPrice = buyPrice;
                final double finalSellPrice = sellPrice;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ShopSetupSession currentSession = shopSetupGUIManager.getPlayerSession(finalPlayer.getUniqueId());
                        if (currentSession == null || currentSession.getPendingShop() != finalPendingShop) return;
                        shopSetupGUIManager.openConfirmationMenu(finalPlayer, finalPendingShop, finalBuyPrice, finalSellPrice);
                    }
                }.runTask(plugin);
            } else {
                player.sendMessage(ChatColor.RED + "Error: Shop type (buy/sell intent) not properly set for price input.");
                shopManager.cancelShopSetup(playerId);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format for price. Example: 10.5 or 100:80. Type 'cancel' to abort.");
        }
    }
}