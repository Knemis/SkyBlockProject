// com/knemis/skyblock/skyblockcoreproject/listeners/ShopVisitListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Kullanıcının sağladığı
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import net.milkbowl.vault.economy.Economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        // Check if player is viewing a shop managed by our plugin
        if (!plugin.getPlayerViewingShopLocation().containsKey(buyer.getUniqueId())) {
            return; // Not in a shop visit GUI or state lost
        }

        // Prevent clicking in player's own inventory from being cancelled by default
        // and allow interaction with their inventory.
        if (event.getClickedInventory() == buyer.getOpenInventory().getBottomInventory()) {
            event.setCancelled(false); // Allow interaction with player's inventory
            return;
        }

        // If the click is in the top inventory (shop GUI), cancel the event by default.
        // Specific actions will be handled below.
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
        } else {
            // Click was outside both inventories or in some other context, ignore.
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return; // Clicked on an empty slot or non-actionable item
        }

        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyer.getUniqueId());
        // shopLocation should not be null here due to the initial check, but good practice:
        if (shopLocation == null) {
            buyer.sendMessage(ChatColor.RED + "Error: Shop location not found. Please reopen the shop.");
            closeShopForPlayer(buyer); // Close and clear state
            return;
        }

        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            buyer.sendMessage(ChatColor.RED + "This shop is not available or misconfigured.");
            closeShopForPlayer(buyer); // Close and clear state
            return;
        }

        // Prevent owner from interacting with their own shop via visit GUI
        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(ChatColor.YELLOW + "You cannot buy from or sell to your own shop this way.");
            // Optionally close, or just let them view. For now, just prevent action.
            return;
        }

        Shop.ShopMode mode = shop.getShopMode();
        int rawSlot = event.getRawSlot();

        if (mode == Shop.ShopMode.BANK_CHEST) {
            if (rawSlot == 20) { // Buy 1 Bundle
                if (shop.getBuyPrice() != -1) {
                    plugin.getShopManager().executePurchase(buyer, shop, 1);
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently selling this item.");
                }
            } else if (rawSlot == 24) { // Sell 1 Bundle
                if (shop.getSellPrice() != -1) {
                    plugin.getShopManager().executeSellToShop(buyer, shop, 1);
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently buying this item.");
                }
            }
        } else if (mode == Shop.ShopMode.MARKET_CHEST) {
            if (rawSlot == 20) { // Buy Custom Amount
                if (shop.getBuyPrice() != -1) {
                    buyer.closeInventory(); // Close GUI first
                    plugin.getPlayerEnteringBuyQuantity().put(buyer.getUniqueId(), shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to buy.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently selling this item.");
                }
            } else if (rawSlot == 24) { // Sell Custom Amount
                if (shop.getSellPrice() != -1) {
                    buyer.closeInventory(); // Close GUI first
                    plugin.getPlayerEnteringSellQuantity().put(buyer.getUniqueId(), shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to sell.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently buying this item.");
                }
            }
        }
        // Any other clicks in the GUI are already cancelled and do nothing.
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        if (plugin.getPlayerEnteringBuyQuantity().containsKey(playerId)) {
            event.setCancelled(true);
            Location shopLocation = plugin.getPlayerEnteringBuyQuantity().get(playerId);
            Shop shop = plugin.getShopManager().getActiveShop(shopLocation);

            if (shop == null || !shop.isSetupComplete()) {
                player.sendMessage(ChatColor.RED + "The shop you were interacting with is no longer available.");
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
                player.sendMessage(ChatColor.YELLOW + "Buy operation cancelled.");
                return;
            }

            try {
                int quantityInBundles = Integer.parseInt(message);
                if (quantityInBundles <= 0) {
                    player.sendMessage(ChatColor.RED + "Please enter a positive number of bundles.");
                    return;
                }
                // Execute purchase (ShopManager will handle messages for success/failure)
                plugin.getShopManager().executePurchase(player, shop, quantityInBundles);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number format. Please enter a whole number for bundles.");
            } finally {
                // Always remove player from state after processing, whether success or failure,
                // unless they need to re-enter due to bad input (handled by not removing here and returning).
                // For this flow, we'll remove them. They can re-click if they want to try again.
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
            }

        } else if (plugin.getPlayerEnteringSellQuantity().containsKey(playerId)) {
            event.setCancelled(true);
            Location shopLocation = plugin.getPlayerEnteringSellQuantity().get(playerId);
            Shop shop = plugin.getShopManager().getActiveShop(shopLocation);

            if (shop == null || !shop.isSetupComplete()) {
                player.sendMessage(ChatColor.RED + "The shop you were interacting with is no longer available.");
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
                player.sendMessage(ChatColor.YELLOW + "Sell operation cancelled.");
                return;
            }

            try {
                int quantityInBundles = Integer.parseInt(message);
                if (quantityInBundles <= 0) {
                    player.sendMessage(ChatColor.RED + "Please enter a positive number of bundles.");
                    return;
                }
                // Execute sell (ShopManager will handle messages)
                plugin.getShopManager().executeSellToShop(player, shop, quantityInBundles);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number format. Please enter a whole number for bundles.");
            } finally {
                plugin.getPlayerEnteringSellQuantity().remove(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        // Check if the closed inventory was one where the player was viewing a shop
        // This relies on the fact that ShopVisitGUIManager sets up the player in this map.
        // Note: The title check is removed as titles are dynamic.
        // The primary check is the presence of the player in the map.
        if (plugin.getPlayerViewingShopLocation().containsKey(player.getUniqueId())) {
            // Additional check: ensure the title isn't a setup GUI title if those also use a player state map (they do: getPlayerShopSetupState)
            // However, this listener is specifically for ShopVisitGUIs.
            // The map key `getPlayerViewingShopLocation` should be unique to visiting.
            plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
            // plugin.getLogger().fine(player.getName() + " closed a shop visit GUI. State cleared.");
        }
    }

    // Helper methods like getItemNameForMessages and getCurrencyName can be removed if
    // ShopManager and ShopVisitGUIManager handle all messaging and display formatting.
    // For now, keep closeShopForPlayer if it's used internally after failed critical operations.

    private void closeShopForPlayer(Player player) {
        // This ensures the inventory is closed AND the state is cleared.
        // Useful if a critical error occurs and we need to abort the shop view.
        player.closeInventory();
        plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
    }
}