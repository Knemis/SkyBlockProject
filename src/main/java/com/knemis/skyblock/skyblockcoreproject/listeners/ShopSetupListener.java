package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
// import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // Keep if admin GUIs are relevant for onInventoryClose
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
// import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Keep if relevant for onInventoryClose
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
// import org.bukkit.Bukkit; // Not directly used in this revised version
import org.bukkit.ChatColor;
// import org.bukkit.Location; // Not directly used in this revised version
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent; // Keep for now, though price input might be fully Anvil
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
// import org.bukkit.inventory.meta.ItemMeta; // Not directly used for creating items here
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    private static final int PLAYER_BUYS_FROM_SHOP_SLOT = 11;
    private static final int PLAYER_SELLS_TO_SHOP_SLOT = 13;
    private static final int PLAYER_BUY_SELL_SHOP_SLOT = 15;

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
            if (rawSlot == PLAYER_BUYS_FROM_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(false);
                intentSet = true;
            } else if (rawSlot == PLAYER_SELLS_TO_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(false);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
            } else if (rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) {
                session.setIntentToAllowPlayerBuy(true);
                session.setIntentToAllowPlayerSell(true);
                intentSet = true;
            }

            if (intentSet) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s set intent: OwnerSells=%b, OwnerBuys=%b for shop at %s.",
                        player.getName(), session.isIntentToAllowPlayerBuy(), session.isIntentToAllowPlayerSell(), Shop.locationToString(session.getChestLocation())));
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
            }

        } else if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE))) {
            handleItemSelectionGuiClickLogic(event, player, topInventory, session);

        } else if (viewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.QUANTITY_INPUT_TITLE))) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "This setup step has been replaced by the Anvil GUI. Please restart shop setup if you see this.");
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

            ItemStack currentItemClicked = event.getCurrentItem();
            if (currentItemClicked != null) {
                if (currentItemClicked.getType() == Material.GREEN_WOOL) {
                    shopManager.finalizeShopSetup(playerId);
                } else if (currentItemClicked.getType() == Material.RED_WOOL) {
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
                    ItemStack currentItemInGUISlot = guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT);
                    if (currentItemInGUISlot == null || currentItemInGUISlot.getType() == Material.GRAY_STAINED_GLASS_PANE || currentItemInGUISlot.getType() == Material.AIR) {
                        event.setCancelled(true);

                        // Clone only one item for the template
                        ItemStack movedItemCloneForTemplate = itemToMove.clone();
                        movedItemCloneForTemplate.setAmount(1);

                        pendingShop.setTemplateItemStack(movedItemCloneForTemplate);

                        // Update GUI
                        ItemStack displayItem = movedItemCloneForTemplate.clone();
                        displayItem.setAmount(1);
                        guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, displayItem);

                        // Update player inventory
                        if (itemToMove.getAmount() == 1) {
                            event.setCurrentItem(null);
                        } else {
                            itemToMove.setAmount(itemToMove.getAmount() - 1);
                            event.setCurrentItem(itemToMove);
                        }

                        player.updateInventory();

                        // Proceed to Anvil GUI after a small delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                shopSetupGUIManager.openPriceQuantityAnvilGUI(player, pendingShop);
                            }
                        }.runTaskLater(plugin, 1L);
                    }
                }
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == ITEM_SELECT_PLACEMENT_SLOT) {
                ItemStack cursorItem = event.getCursor();
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    event.setCancelled(true);
                    ItemStack itemToPlaceCopy = cursorItem.clone();

                    pendingShop.setTemplateItemStack(itemToPlaceCopy.clone());
                    player.setItemOnCursor(null);

                    plugin.getLogger().info(String.format("ShopSetupListener: Player %s auto-selected item %s (direct place) for shop at %s. Proceeding to Anvil.",
                            player.getName(), itemToPlaceCopy.getType(), Shop.locationToString(session.getChestLocation())));
                    shopSetupGUIManager.openPriceQuantityAnvilGUI(player, pendingShop);
                } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
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
            String itemSelectTitleSer = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE);
            String shopTypeTitleSer = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE);
            if (closedViewTitle.equals(itemSelectTitleSer) || closedViewTitle.equals(shopTypeTitleSer)) {
                plugin.getLogger().warning("ShopSetupListener: " + player.getName() + " closed setup GUI '" + closedViewTitle + "' but session was null.");
            }
            return;
        }

        String sessionGuiTitle = session.getCurrentGuiTitle();
        Shop pendingShop = session.getPendingShop();

        if (sessionGuiTitle != null && !sessionGuiTitle.equals(closedViewTitle)) {
            String anvilTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT);
            String itemSelectTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE);

            if (closedViewTitle.equals(itemSelectTitleSerialized) && sessionGuiTitle.equals(anvilTitleSerialized)) {
                plugin.getLogger().fine("ShopSetupListener: Player " + player.getName() + " closed ITEM_SELECT_TITLE, but session already advanced to ANVIL. Auto-forwarding handled it.");
                return;
            }
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed GUI '%s' but session current title is '%s'. Might be normal transition or stale event.",
                    player.getName(), closedViewTitle, sessionGuiTitle));
            return;
        }

        if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE))) {
            if (pendingShop == null) {
                shopManager.cancelShopSetup(playerId);
                return;
            }
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_SELECT_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR && itemInSlot.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                player.getInventory().addItem(itemInSlot.clone());
                player.sendMessage(ChatColor.YELLOW + "Item from selection slot returned. Setup cancelled.");
                plugin.getLogger().warning("ShopSetupListener: Item " + itemInSlot.getType() + " found in ITEM_SELECT_PLACEMENT_SLOT on explicit close for " + player.getName() + ". Returning and cancelling.");
            }
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s explicitly closed ITEM_SELECT_TITLE. Cancelling setup.", player.getName()));
            shopManager.cancelShopSetup(playerId);

        } else if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.SHOP_TYPE_TITLE))) {
            if (pendingShop != null && !session.isIntentToAllowPlayerBuy() && !session.isIntentToAllowPlayerSell()) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE. No buy/sell intent selected. Cancelling.", player.getName()));
            } else if (pendingShop == null) {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE, but pendingShop is null (already cancelled or error).", player.getName()));
            } else {
                plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed SHOP_TYPE_TITLE after selecting intent. Cancelling.", player.getName()));
            }
            shopManager.cancelShopSetup(playerId);

        } else if (closedViewTitle.equals(LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE))) {
            plugin.getLogger().info(String.format("ShopSetupListener: Player %s closed CONFIRMATION_TITLE without action. Cancelling setup.", player.getName()));
            shopManager.cancelShopSetup(playerId);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(player.getUniqueId());

        if (session == null || session.getCurrentGuiTitle() == null) return;

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        String viewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        String itemSelectTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ITEM_SELECT_TITLE);

        if (!session.getCurrentGuiTitle().equals(viewTitle) || !viewTitle.equals(itemSelectTitleSerialized)) {
            return;
        }

        boolean affectsPlacementSlot = false;
        boolean affectsOtherSlots = false;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topInventory.getSize()) {
                if (rawSlot == ITEM_SELECT_PLACEMENT_SLOT) {
                    affectsPlacementSlot = true;
                } else {
                    affectsOtherSlots = true;
                }
            }
        }

        if (affectsOtherSlots || !affectsPlacementSlot) {
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

        event.setCancelled(true);
        player.sendMessage(ChatColor.YELLOW + "Price input via chat is being phased out in favor of the Anvil GUI.");
        player.sendMessage(ChatColor.YELLOW + "Shop setup via this method is cancelled. Please restart if needed.");
        plugin.getLogger().info("ShopSetupListener: Player " + player.getName() + " attempted chat price input (deprecated). Cancelling session.");
        shopManager.cancelShopSetup(playerId);
    }
}