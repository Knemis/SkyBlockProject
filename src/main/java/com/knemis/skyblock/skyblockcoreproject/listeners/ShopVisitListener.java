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
        UUID buyerId = buyer.getUniqueId();

        if (!plugin.getPlayerViewingShopLocation().containsKey(buyerId)) {
            return; // Not viewing a shop this listener should care about
        }

        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyerId);
        Shop shop = shopManager.getActiveShop(shopLocation); // Get shop early for logging

        String viewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        ItemStack clickedItemRaw = event.getCurrentItem();
        String clickedItemName = (clickedItemRaw != null && clickedItemRaw.hasItemMeta() && clickedItemRaw.getItemMeta().hasDisplayName()) ?
                                 LegacyComponentSerializer.legacySection().serialize(clickedItemRaw.getItemMeta().displayName()) :
                                 (clickedItemRaw != null ? clickedItemRaw.getType().name() : "null");
        String shopOwnerName = (shop != null && shop.getOwnerUUID() != null) ? Bukkit.getOfflinePlayer(shop.getOwnerUUID()).getName() : "Unknown";


        if (event.getClickedInventory() == buyer.getOpenInventory().getBottomInventory()) {
            // Allow interaction with player's inventory.
            // Could log here if specific interactions from player inv to shop GUI were expected, but generally not needed.
            return;
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) clicked in Shop Visit GUI: '%s' (ShopOwner: %s, ShopLoc: %s), Slot: %d, Item: %s",
                    buyer.getName(), buyerId, viewTitle, shopOwnerName, shopLocation, event.getRawSlot(), clickedItemName));
            event.setCancelled(true);
        } else {
            return; // Should not happen if top inventory is not null and not player inventory
        }

        if (clickedItemRaw == null || clickedItemRaw.getType() == Material.AIR) {
            plugin.getLogger().finer(String.format("ShopVisitListener: Player %s clicked on AIR or null item in shop %s. No action.", buyer.getName(), shopLocation));
            return;
        }

        if (shopLocation == null) { // Should have been caught by the containsKey check, but defensive
            buyer.sendMessage(ChatColor.RED + "Error: Shop location not found. Please reopen the shop.");
            plugin.getLogger().severe("ShopVisitListener: Player " + buyer.getName() + " was viewing a shop, but shopLocation became null unexpectedly.");
            closeShopForPlayer(buyer);
            return;
        }

        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            buyer.sendMessage(ChatColor.RED + "This shop is not available or misconfigured.");
            plugin.getLogger().warning(String.format("ShopVisitListener: Player %s (UUID: %s) tried to interact with shop at %s, but it's unavailable/misconfigured. Shop: %s, SetupComplete: %b, Template: %s",
                    buyer.getName(), buyerId, shopLocation, shop, (shop != null && shop.isSetupComplete()), (shop != null ? shop.getTemplateItemStack() : "null")));
            closeShopForPlayer(buyer);
            return;
        }

        if (shop.getOwnerUUID().equals(buyerId)) {
            buyer.sendMessage(ChatColor.YELLOW + "You cannot buy from or sell to your own shop this way.");
            plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) attempted to interact with their own shop at %s via visit GUI. Denied.",
                    buyer.getName(), buyerId, shopLocation));
            return;
        }

        ShopMode mode = shop.getShopMode();
        int rawSlot = event.getRawSlot();
        String actionType = "UNKNOWN";

        if (mode == ShopMode.BANK_CHEST) {
            if (rawSlot == 20) { // Buy 1 Bundle
                actionType = "BUY_BUNDLE_BANK";
                if (shop.getBuyPrice() != -1) {
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) trying to %s from BANK shop %s (Owner: %s) at %s.",
                            buyer.getName(), buyerId, actionType, shop.getShopId(), shopOwnerName, shopLocation));
                    plugin.getShopManager().executePurchase(buyer, shop, 1); // Success/failure logged by ShopManager
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently selling this item.");
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) tried to %s from shop %s, but it's not SELLING this item.",
                            buyer.getName(), buyerId, actionType, shop.getShopId()));
                }
            } else if (rawSlot == 24) { // Sell 1 Bundle
                actionType = "SELL_BUNDLE_BANK";
                if (shop.getSellPrice() != -1) {
                     plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) trying to %s to BANK shop %s (Owner: %s) at %s.",
                            buyer.getName(), buyerId, actionType, shop.getShopId(), shopOwnerName, shopLocation));
                    plugin.getShopManager().executeSellToShop(buyer, shop, 1); // Success/failure logged by ShopManager
                    plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // Refresh
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently buying this item.");
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) tried to %s to shop %s, but it's not BUYING this item.",
                            buyer.getName(), buyerId, actionType, shop.getShopId()));
                }
            }
        } else if (mode == ShopMode.MARKET_CHEST) {
            if (rawSlot == 20) { // Buy Custom Amount
                actionType = "INITIATE_CUSTOM_BUY_MARKET";
                if (shop.getBuyPrice() != -1) {
                    buyer.closeInventory(); // Close before chat prompt
                    plugin.getPlayerEnteringBuyQuantity().put(buyerId, shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to buy.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) %s for shop %s (Owner: %s) at %s. Awaiting quantity.",
                            buyer.getName(), buyerId, actionType, shop.getShopId(), shopOwnerName, shopLocation));
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently selling this item.");
                     plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) tried to %s from shop %s, but it's not SELLING this item.",
                            buyer.getName(), buyerId, actionType, shop.getShopId()));
                }
            } else if (rawSlot == 24) { // Sell Custom Amount
                actionType = "INITIATE_CUSTOM_SELL_MARKET";
                if (shop.getSellPrice() != -1) {
                    buyer.closeInventory(); // Close before chat prompt
                    plugin.getPlayerEnteringSellQuantity().put(buyerId, shopLocation);
                    buyer.sendMessage(ChatColor.GREEN + "Please enter the number of BUNDLES you wish to sell.");
                    buyer.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) %s for shop %s (Owner: %s) at %s. Awaiting quantity.",
                            buyer.getName(), buyerId, actionType, shop.getShopId(), shopOwnerName, shopLocation));
                } else {
                    buyer.sendMessage(ChatColor.RED + "This shop is not currently buying this item.");
                    plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) tried to %s to shop %s, but it's not BUYING this item.",
                            buyer.getName(), buyerId, actionType, shop.getShopId()));
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();
        String type = ""; // For logging

        Location shopLocation = null;
        Shop shop = null;

        if (plugin.getPlayerEnteringBuyQuantity().containsKey(playerId)) {
            type = "BUY";
            shopLocation = plugin.getPlayerEnteringBuyQuantity().get(playerId);
        } else if (plugin.getPlayerEnteringSellQuantity().containsKey(playerId)) {
            type = "SELL";
            shopLocation = plugin.getPlayerEnteringSellQuantity().get(playerId);
        } else {
            return; // Not in a shop quantity input state
        }

        event.setCancelled(true); // Cancel chat message from appearing publicly
        shop = (shopLocation != null) ? plugin.getShopManager().getActiveShop(shopLocation) : null;
        String shopOwnerName = (shop != null && shop.getOwnerUUID() != null) ? Bukkit.getOfflinePlayer(shop.getOwnerUUID()).getName() : "Unknown";


        plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) providing chat input for CUSTOM %s quantity for shop %s (Owner: %s) at %s. Message: '%s'",
                player.getName(), playerId, type, (shop != null ? shop.getShopId() : "UNKNOWN_SHOP_ID"), shopOwnerName, shopLocation, message));

        if (shop == null || !shop.isSetupComplete()) {
            player.sendMessage(ChatColor.RED + "The shop you were interacting with is no longer available.");
            plugin.getLogger().warning(String.format("ShopVisitListener: Player %s (UUID: %s) was entering custom %s quantity, but shop %s at %s became unavailable.",
                    player.getName(), playerId, type, (shop != null ? shop.getShopId() : "UNKNOWN_SHOP_ID"), shopLocation));
            clearPlayerQuantityState(playerId);
            return;
        }

        if (message.equalsIgnoreCase("cancel")) {
            clearPlayerQuantityState(playerId);
            player.sendMessage(ChatColor.YELLOW + type + " operation cancelled.");
            plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) cancelled custom %s operation for shop %s at %s.",
                    player.getName(), playerId, type, shop.getShopId(), shopLocation));
            return;
        }

        try {
            int quantityInBundles = Integer.parseInt(message);
            if (quantityInBundles <= 0) {
                player.sendMessage(ChatColor.RED + "Please enter a positive number of bundles.");
                plugin.getLogger().warning(String.format("ShopVisitListener: Player %s (UUID: %s) entered non-positive quantity '%s' for custom %s for shop %s at %s.",
                        player.getName(), playerId, message, type, shop.getShopId(), shopLocation));
                // Do not clear state here, let them try again or cancel.
                return;
            }

            // Execute after runnable to avoid async issues if ShopManager methods are not async-safe
            final int finalQuantity = quantityInBundles;
            final Shop finalShop = shop; // Effectively final for runnable
            final String finalType = type;
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if ("BUY".equals(finalType)) {
                        plugin.getShopManager().executePurchase(player, finalShop, finalQuantity);
                    } else if ("SELL".equals(finalType)) {
                        plugin.getShopManager().executeSellToShop(player, finalShop, finalQuantity);
                    }
                }
            }.runTask(plugin);

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format. Please enter a whole number for bundles.");
            plugin.getLogger().warning(String.format("ShopVisitListener: Player %s (UUID: %s) entered invalid quantity '%s' for custom %s for shop %s at %s. Error: %s",
                    player.getName(), playerId, message, type, shop.getShopId(), shopLocation, e.getMessage()));
            // Do not clear state here, let them try again or cancel.
            return; // Return here to prevent finally block from clearing state, allowing retry
        }
        // Clear state only after successful processing or explicit cancel.
        // If an error occurs that requires retry (like NumberFormatException), state should persist.
        // For this structure, moving clear state into success path of execute or if they cancel.
        // However, the original code cleared in `finally`. Let's assume for now that after any attempt (valid or invalid number), the state is cleared.
        // This means they have to click the button again to re-enter quantity.
        clearPlayerQuantityState(playerId);
    }

    private void clearPlayerQuantityState(UUID playerId){
        plugin.getPlayerEnteringBuyQuantity().remove(playerId);
        plugin.getPlayerEnteringSellQuantity().remove(playerId);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Store location before removing, for logging purposes
        Location removedLocation = plugin.getPlayerViewingShopLocation().get(playerId);

        if (plugin.getPlayerViewingShopLocation().remove(playerId) != null) {
            plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) closed Shop Visit GUI. Cleared viewing state for shop at %s.",
                    player.getName(), playerId, removedLocation));
        }
        // Also ensure any pending quantity inputs are cleared if they close GUI instead of typing
        if (plugin.getPlayerEnteringBuyQuantity().containsKey(playerId)) {
            plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) closed inventory while entering BUY quantity for shop at %s. Buy operation cancelled.",
                    player.getName(), playerId, plugin.getPlayerEnteringBuyQuantity().get(playerId)));
            plugin.getPlayerEnteringBuyQuantity().remove(playerId);
        }
        if (plugin.getPlayerEnteringSellQuantity().containsKey(playerId)) {
             plugin.getLogger().info(String.format("ShopVisitListener: Player %s (UUID: %s) closed inventory while entering SELL quantity for shop at %s. Sell operation cancelled.",
                    player.getName(), playerId, plugin.getPlayerEnteringSellQuantity().get(playerId)));
            plugin.getPlayerEnteringSellQuantity().remove(playerId);
        }
    }

    private void closeShopForPlayer(Player player) {
        Location shopLocation = plugin.getPlayerViewingShopLocation().get(player.getUniqueId()); // Get for logging
        player.closeInventory(); // This will trigger onInventoryClose which handles removal and logging
        plugin.getLogger().info(String.format("ShopVisitListener: Closing shop for player %s (UUID: %s) due to an issue with shop at %s.",
                player.getName(), player.getUniqueId(), shopLocation));
        // The actual removal from map is now in onInventoryClose
    }
}