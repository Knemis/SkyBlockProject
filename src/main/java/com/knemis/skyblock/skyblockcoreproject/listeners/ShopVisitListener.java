// com/knemis/skyblock/skyblockcoreproject/listeners/ShopVisitListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
// import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager; // Not directly used if ShopManager handles all economy
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode; // <---  ADD THIS IMPORT

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Level;

public class ShopVisitListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopVisitGUIManager shopVisitGUIManager; // GUI'yi yeniden açmak için

    public ShopVisitListener(SkyBlockProject plugin, ShopManager shopManager, ShopVisitGUIManager shopVisitGUIManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopVisitGUIManager = shopVisitGUIManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player buyer = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String clickedItemName = (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) ?
                                 LegacyComponentSerializer.legacySection().serialize(clickedItem.getItemMeta().displayName()) :
                                 (clickedItem != null ? clickedItem.getType().name() : "null");
        String viewTitle = event.getView().getTitle(); // Using Bukkit's getTitle
        System.out.println("[TRACE] In ShopVisitListener.onInventoryClick by " + buyer.getName() + ". Title: '" + viewTitle + "', Slot: " + event.getRawSlot() + ", Item: " + clickedItemName);


        if (!plugin.getPlayerViewingShopLocation().containsKey(buyer.getUniqueId())) {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Player " + buyer.getName() + " not in viewing shop state. Returning.");
            return;
        }

        if (event.getClickedInventory() == buyer.getOpenInventory().getBottomInventory()) {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Clicked in player's bottom inventory. Allowing. Player: " + buyer.getName());
            // event.setCancelled(false); // This was the line with the previous error, ensure it's correct now.
            // InventoryClickEvent is Cancellable. If this still errors, it's an API/project setup issue.
            return; // Allow interaction with player's inventory.
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Clicked in top inventory (shop GUI). Setting cancelled true. Player: " + buyer.getName());
            event.setCancelled(true);
        } else {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Clicked inventory is neither top nor bottom. Odd state. Returning. Player: " + buyer.getName());
            return;
        }

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Clicked item is null or AIR. Returning. Player: " + buyer.getName());
            return;
        }

        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyer.getUniqueId());
        if (shopLocation == null) {
            buyer.sendMessage(Component.text("Error: Shop location not found. Please reopen the shop.", NamedTextColor.RED));
            closeShopForPlayer(buyer);
            return;
        }

        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            buyer.sendMessage(Component.text("This shop is not available or misconfigured.", NamedTextColor.RED));
            closeShopForPlayer(buyer);
            return;
        }

        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(Component.text("You cannot buy from or sell to your own shop this way.", NamedTextColor.YELLOW));
            return;
        }

        // Now you can use ShopMode directly after importing it
        ShopMode mode = shop.getShopMode(); // No need for Shop.ShopMode prefix
        int rawSlot = event.getRawSlot();
        System.out.println("[TRACE] ShopVisitListener.onInventoryClick: Processing click for shop " + shop.getShopId() + ", Mode: " + mode + ", Slot: " + rawSlot + ", Player: " + buyer.getName());

        if (mode == ShopMode.BANK_CHEST) { // Direct comparison
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: BANK_CHEST mode. Player: " + buyer.getName());
            if (rawSlot == 20) { // Buy 1 Bundle (Slot 20 for BANK_CHEST buy button)
                System.out.println("[TRACE] ShopVisitListener.onInventoryClick: BANK_CHEST - Buy 1 Bundle clicked. Player: " + buyer.getName());
                if (shop.getBuyPrice() != -1) {
                    plugin.getShopManager().executePurchase(buyer, shop, 1);
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(Component.text("This shop is not currently selling this item.", NamedTextColor.RED));
                }
            } else if (rawSlot == 24) { // Sell 1 Bundle (Slot 24 for BANK_CHEST sell button)
                System.out.println("[TRACE] ShopVisitListener.onInventoryClick: BANK_CHEST - Sell 1 Bundle clicked. Player: " + buyer.getName());
                if (shop.getSellPrice() != -1) {
                    plugin.getShopManager().executeSellToShop(buyer, shop, 1);
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(Component.text("This shop is not currently buying this item.", NamedTextColor.RED));
                }
            }
        } else if (mode == ShopMode.MARKET_CHEST) { // Direct comparison
            System.out.println("[TRACE] ShopVisitListener.onInventoryClick: MARKET_CHEST mode. Player: " + buyer.getName() + ", Slot: " + rawSlot);
            if (rawSlot == 13) { // Interaction is now on the item display at slot 13 for MARKET_CHEST
                if (event.isLeftClick()) { // Buy action
                    System.out.println("[TRACE] ShopVisitListener.onInventoryClick: MARKET_CHEST - Slot 13 Left-Clicked (Buy). Player: " + buyer.getName());
                    if (shop.getBuyPrice() != -1) {
                        buyer.closeInventory();
                        plugin.getPlayerEnteringBuyQuantity().put(buyer.getUniqueId(), shopLocation);
                        buyer.sendMessage(Component.text("Please enter the number of BUNDLES you wish to buy.", NamedTextColor.GREEN));
                        buyer.sendMessage(Component.text("Type 'cancel' to abort.", NamedTextColor.GRAY));
                    } else {
                        buyer.sendMessage(Component.text("This shop is not currently selling this item.", NamedTextColor.RED));
                    }
                } else if (event.isRightClick()) { // Sell action
                    System.out.println("[TRACE] ShopVisitListener.onInventoryClick: MARKET_CHEST - Slot 13 Right-Clicked (Sell). Player: " + buyer.getName());
                    if (shop.getSellPrice() != -1) {
                        buyer.closeInventory();
                        plugin.getPlayerEnteringSellQuantity().put(buyer.getUniqueId(), shopLocation);
                        buyer.sendMessage(Component.text("Please enter the number of BUNDLES you wish to sell.", NamedTextColor.GREEN));
                        buyer.sendMessage(Component.text("Type 'cancel' to abort.", NamedTextColor.GRAY));
                    } else {
                        buyer.sendMessage(Component.text("This shop is not currently buying this item.", NamedTextColor.RED));
                    }
                }
                // Other click types on slot 13 in MARKET_CHEST mode are ignored.
            }
            // Slots 20 and 24 are no longer specifically handled for MARKET_CHEST mode here, as per task alignment.
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();
        System.out.println("[TRACE] In ShopVisitListener.onAsyncPlayerChat by " + player.getName() + ". Message: '" + message + "'");

        if (plugin.getPlayerEnteringBuyQuantity().containsKey(playerId)) {
            System.out.println("[TRACE] ShopVisitListener.onAsyncPlayerChat: Player " + player.getName() + " is entering buy quantity.");
            // event.setCancelled(true); // Also ensure this resolves if it was an issue.
            Location shopLocation = plugin.getPlayerEnteringBuyQuantity().get(playerId);
            Shop shop = plugin.getShopManager().getActiveShop(shopLocation);

            if (shop == null || !shop.isSetupComplete()) {
                player.sendMessage(Component.text("The shop you were interacting with is no longer available.", NamedTextColor.RED));
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("cancel")) {
                System.out.println("[TRACE] ShopVisitListener.onAsyncPlayerChat: Buy operation cancelled by " + player.getName());
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
                player.sendMessage(Component.text("Buy operation cancelled.", NamedTextColor.YELLOW));
                return;
            }

            try {
                int quantityInBundles = Integer.parseInt(message);
                if (quantityInBundles <= 0) {
                    player.sendMessage(Component.text("Please enter a positive number of bundles.", NamedTextColor.RED));
                    return;
                }
                plugin.getShopManager().executePurchase(player, shop, quantityInBundles);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid number format. Please enter a whole number for bundles.", NamedTextColor.RED));
            } finally {
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
            }

        } else if (plugin.getPlayerEnteringSellQuantity().containsKey(playerId)) {
            System.out.println("[TRACE] ShopVisitListener.onAsyncPlayerChat: Player " + player.getName() + " is entering sell quantity.");
            // event.setCancelled(true); // Also ensure this resolves if it was an issue.
            Location shopLocation = plugin.getPlayerEnteringSellQuantity().get(playerId);
            Shop shop = plugin.getShopManager().getActiveShop(shopLocation);

            if (shop == null || !shop.isSetupComplete()) {
                player.sendMessage(Component.text("The shop you were interacting with is no longer available.", NamedTextColor.RED));
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("cancel")) {
                System.out.println("[TRACE] ShopVisitListener.onAsyncPlayerChat: Sell operation cancelled by " + player.getName());
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
                player.sendMessage(Component.text("Sell operation cancelled.", NamedTextColor.YELLOW));
                return;
            }

            try {
                int quantityInBundles = Integer.parseInt(message);
                if (quantityInBundles <= 0) {
                    player.sendMessage(Component.text("Please enter a positive number of bundles.", NamedTextColor.RED));
                    return;
                }
                plugin.getShopManager().executeSellToShop(player, shop, quantityInBundles);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid number format. Please enter a whole number for bundles.", NamedTextColor.RED));
            } finally {
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String viewTitle = event.getView().getTitle();
        System.out.println("[TRACE] In ShopVisitListener.onInventoryClose by " + player.getName() + ". Title: '" + viewTitle + "'");

        if (plugin.getPlayerViewingShopLocation().containsKey(player.getUniqueId())) {
            System.out.println("[TRACE] ShopVisitListener.onInventoryClose: Player " + player.getName() + " was viewing a shop. Removing from map.");
            plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
        }
    }

    private void closeShopForPlayer(Player player) {
        System.out.println("[TRACE] In ShopVisitListener.closeShopForPlayer for " + player.getName());
        player.closeInventory();
        plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
    }
}