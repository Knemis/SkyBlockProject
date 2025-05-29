// com/knemis/skyblock/skyblockcoreproject/listeners/ShopVisitListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
// import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager; // Not directly used if ShopManager handles all economy
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode; // <---  ADD THIS IMPORT

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

        if (!plugin.getPlayerViewingShopLocation().containsKey(buyer.getUniqueId())) {
            return;
        }

        if (event.getClickedInventory() == buyer.getOpenInventory().getBottomInventory()) {
            // event.setCancelled(false); // This was the line with the previous error, ensure it's correct now.
            // InventoryClickEvent is Cancellable. If this still errors, it's an API/project setup issue.
            return; // Allow interaction with player's inventory.
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
        } else {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyer.getUniqueId());
        if (shopLocation == null) {
            buyer.sendMessage(ChatColor.RED + "Error: Shop location not found. Please reopen the shop.");
            closeShopForPlayer(buyer);
            return;
        }

        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            buyer.sendMessage(ChatColor.RED + "This shop is not available or misconfigured.");
            closeShopForPlayer(buyer);
            return;
        }

        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(ChatColor.YELLOW + "You cannot buy from or sell to your own shop this way.");
            return;
        }

        // Now you can use ShopMode directly after importing it
        ShopMode mode = shop.getShopMode(); // No need for Shop.ShopMode prefix
        int rawSlot = event.getRawSlot();

        if (mode == ShopMode.BANK_CHEST) { // Direct comparison
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
        } else if (mode == ShopMode.MARKET_CHEST) { // Direct comparison
            if (rawSlot == 20) { // Buy Custom Amount
                if (shop.getBuyPrice() != -1) {
                    buyer.closeInventory();
                    plugin.getPlayerEnteringBuyQuantity().put(buyer.getUniqueId(), shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to buy.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently selling this item.");
                }
            } else if (rawSlot == 24) { // Sell Custom Amount
                if (shop.getSellPrice() != -1) {
                    buyer.closeInventory();
                    plugin.getPlayerEnteringSellQuantity().put(buyer.getUniqueId(), shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to sell.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently buying this item.");
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        if (plugin.getPlayerEnteringBuyQuantity().containsKey(playerId)) {
            // event.setCancelled(true); // Also ensure this resolves if it was an issue.
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
                plugin.getShopManager().executePurchase(player, shop, quantityInBundles);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number format. Please enter a whole number for bundles.");
            } finally {
                plugin.getPlayerEnteringBuyQuantity().remove(playerId);
            }

        } else if (plugin.getPlayerEnteringSellQuantity().containsKey(playerId)) {
            // event.setCancelled(true); // Also ensure this resolves if it was an issue.
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

        if (plugin.getPlayerViewingShopLocation().containsKey(player.getUniqueId())) {
            plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
        }
    }

    private void closeShopForPlayer(Player player) {
        player.closeInventory();
        plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
    }
}