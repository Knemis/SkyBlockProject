package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap; // Added for leftover handling
import java.util.List;
import java.util.UUID;

public class ShopAnvilListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopSetupGUIManager shopSetupGUIManager;

    public ShopAnvilListener(SkyBlockProject plugin, ShopSetupGUIManager shopSetupGUIManager) {
        this.plugin = plugin;
        this.shopSetupGUIManager = shopSetupGUIManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!(event.getInventory() instanceof AnvilInventory)) return;

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(player.getUniqueId());
        if (session == null || session.getPendingShop() == null || session.getPendingShop().getTemplateItemStack() == null) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Shop setup session expired or invalid. Please start over.");
            return;
        }

        if (session.getExpectedInputType() != ShopSetupGUIManager.InputType.ANVIL_PRICE_QUANTITY) {
            return;
        }

        AnvilInventory anvilInv = (AnvilInventory) event.getInventory();
        ItemStack templateItem = session.getPendingShop().getTemplateItemStack();

        // Handle clicks in player's main inventory for shift-clicking into Anvil
        if (event.getClickedInventory() != anvilInv) {
            if (event.isShiftClick()) {
                ItemStack clickedPlayerItem = event.getCurrentItem();
                if (clickedPlayerItem != null && clickedPlayerItem.getType() == templateItem.getType()) {
                    event.setCancelled(true); // Manual handling of shift-click
                    ItemStack currentAnvilSlot0Item = anvilInv.getItem(0);
                    int amountToAdd = clickedPlayerItem.getAmount();

                    if (currentAnvilSlot0Item == null || currentAnvilSlot0Item.getType() == Material.AIR) {
                        ItemStack toPlaceInAnvil = clickedPlayerItem.clone();
                        anvilInv.setItem(0, toPlaceInAnvil);
                        event.setCurrentItem(null); // Clear item from player's inventory slot
                    } else if (currentAnvilSlot0Item.getType() == templateItem.getType() && currentAnvilSlot0Item.getAmount() < currentAnvilSlot0Item.getMaxStackSize()) {
                        int canPlaceInStack = currentAnvilSlot0Item.getMaxStackSize() - currentAnvilSlot0Item.getAmount();
                        int actualPlaceAmount = Math.min(amountToAdd, canPlaceInStack);

                        currentAnvilSlot0Item.setAmount(currentAnvilSlot0Item.getAmount() + actualPlaceAmount);
                        anvilInv.setItem(0, currentAnvilSlot0Item); // Update the anvil slot

                        if (actualPlaceAmount == amountToAdd) {
                            event.setCurrentItem(null); // All items moved
                        } else {
                            clickedPlayerItem.setAmount(amountToAdd - actualPlaceAmount); // Reduce player's stack
                            event.setCurrentItem(clickedPlayerItem); // Update player's inventory slot
                        }
                    } // else, cannot place more or different item, do nothing to anvil slot 0
                    updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                } else if (clickedPlayerItem != null && clickedPlayerItem.getType() != Material.AIR) {
                    event.setCancelled(true); // Prevent shift-clicking non-matching items
                }
            }
            // Allow normal player inventory interactions if not shift-clicking into anvil
            return;
        }

        // Clicks within the Anvil GUI itself
        int rawSlot = event.getRawSlot();

        if (rawSlot == 1) { // Middle/Sacrifice slot - always disable
            event.setCancelled(true);
        } else if (rawSlot == 0) { // Quantity input slot (left)
            ItemStack cursorItem = event.getCursor();
            boolean isPlacingAction = event.getAction() == InventoryAction.PLACE_ONE ||
                    event.getAction() == InventoryAction.PLACE_ALL ||
                    event.getAction() == InventoryAction.PLACE_SOME;
            boolean isSwappingMatching = event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() == templateItem.getType();
            boolean isPickingOrMoving = event.getAction().name().startsWith("PICKUP_") ||
                    event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                    (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && (cursorItem == null || cursorItem.getType() == Material.AIR));

            if (isPlacingAction || isSwappingMatching) {
                if (cursorItem != null && cursorItem.getType() != templateItem.getType()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only place " + templateItem.getType().toString().replace("_", " ") + " here.");
                } else {
                    event.setCancelled(false); // Allow Bukkit to handle the visual placement
                    updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                }
            } else if (isPickingOrMoving) {
                event.setCancelled(false); // Allow Bukkit to handle the visual removal
                updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
            } else {
                event.setCancelled(true); // Disallow other actions like clone stack, etc.
            }
        } else if (rawSlot == 2) { // Output slot (right)
            event.setCancelled(true); // Player cannot take from this slot

            ItemStack quantityItemInSlot0 = anvilInv.getItem(0);
            if (quantityItemInSlot0 == null || quantityItemInSlot0.getType() == Material.AIR || quantityItemInSlot0.getAmount() <= 0) {
                player.sendMessage(ChatColor.RED + "Please place items in the first slot to set quantity.");
                return;
            }
            int quantity = quantityItemInSlot0.getAmount();

            String renameText = anvilInv.getRenameText();
            if (renameText == null || renameText.trim().isEmpty()) {
                player.sendMessage(ChatColor.RED + "Please enter a price in the rename field.");
                return;
            }

            Shop pendingShop = session.getPendingShop();
            double buyPrice = -1;
            double sellPrice = -1;
            boolean priceValid = false;

            try {
                if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                    String[] parts = renameText.split(":");
                    if (parts.length == 2) {
                        buyPrice = Double.parseDouble(parts[0].trim());
                        sellPrice = Double.parseDouble(parts[1].trim());
                        if (!((buyPrice >= 0 || buyPrice == -1) && (sellPrice >= 0 || sellPrice == -1) && !(buyPrice == -1 && sellPrice == -1))) {
                            player.sendMessage(ChatColor.RED + "Prices must be positive or -1 (disabled), and not both -1.");
                        } else {
                            priceValid = true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid format for Buy/Sell. Use BUY_PRICE:SELL_PRICE (e.g., 100:80 or -1:80).");
                    }
                } else if (session.isIntentToAllowPlayerBuy()) { // Owner Sells to Player
                    buyPrice = Double.parseDouble(renameText.trim());
                    if (buyPrice >= 0) {
                        priceValid = true;
                        sellPrice = -1; // Explicitly disable sell price
                    } else {
                        player.sendMessage(ChatColor.RED + "Buy price must be a positive number.");
                    }
                } else if (session.isIntentToAllowPlayerSell()) { // Owner Buys from Player
                    sellPrice = Double.parseDouble(renameText.trim());
                    if (sellPrice >= 0) {
                        priceValid = true;
                        buyPrice = -1; // Explicitly disable buy price
                    } else {
                        player.sendMessage(ChatColor.RED + "Sell price must be a positive number.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Error: Shop type (buy/sell intent) not properly set in session.");
                }

                if (priceValid) {
                    // Return items from Slot 0 BEFORE closing inventory
                    ItemStack itemToReturnFromAnvil = anvilInv.getItem(0);
                    if (itemToReturnFromAnvil != null && itemToReturnFromAnvil.getType() != Material.AIR) {
                        ItemStack clonedItemToReturn = itemToReturnFromAnvil.clone();
                        anvilInv.setItem(0, null); // Clear slot 0 in Anvil
                        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(clonedItemToReturn);
                        if (!leftovers.isEmpty()) {
                            for (ItemStack leftoverItem : leftovers.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                            }
                            player.sendMessage(ChatColor.YELLOW + "Some items from the Anvil couldn't fit and were dropped!");
                        }
                        plugin.getLogger().info(String.format("ShopAnvilListener: Returned %d x %s to player %s from Anvil Slot 0 after P/Q confirmation.",
                                clonedItemToReturn.getAmount(), clonedItemToReturn.getType(), player.getName()));
                    }

                    pendingShop.setItemQuantityForPrice(quantity);
                    pendingShop.setBuyPrice(buyPrice);
                    pendingShop.setSellPrice(sellPrice);

                    plugin.getLogger().info(String.format("ShopAnvilListener: Player %s confirmed Price/Quantity for shop at %s. Item: %s, Qty: %d, BuyPrice: %.2f, SellPrice: %.2f",
                            player.getName(), Shop.locationToString(pendingShop.getLocation()), templateItem.getType(), quantity, buyPrice, sellPrice));

                    // Close inventory and open next GUI in a delayed task to avoid conflicts
                    final Player finalPlayer = player;
                    final Shop finalPendingShop = pendingShop;
                    final double finalBuyPrice = buyPrice;
                    final double finalSellPrice = sellPrice;

                    player.closeInventory(); // Close current Anvil GUI
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            shopSetupGUIManager.openConfirmationMenu(finalPlayer, finalPendingShop, finalBuyPrice, finalSellPrice);
                        }
                    }.runTask(plugin); // Run on next tick
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid price format. Please enter a valid number or BUY:SELL prices (e.g. 100 or 100:80).");
            }
        }
    }

    private void updateAnvilOutputSlotWithDelay(AnvilInventory anvilInv, Shop pendingShop, String currencySymbol) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (anvilInv.getViewers().isEmpty()) return; // No one is viewing, perhaps player closed it
                Player player = (Player) anvilInv.getViewers().get(0);
                // Ensure player is still online and has THIS Anvil inventory open
                if (player != null && player.getOpenInventory().getTopInventory() == anvilInv) {
                    updateAnvilOutputSlot(anvilInv, pendingShop, currencySymbol);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void updateAnvilOutputSlot(AnvilInventory anvilInv, Shop pendingShop, String currencySymbol) {
        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) return;

        ItemStack quantityItem = anvilInv.getItem(0);
        int quantity = (quantityItem != null && quantityItem.getType() == pendingShop.getTemplateItemStack().getType()) ? quantityItem.getAmount() : 0;
        String priceText = anvilInv.getRenameText();
        if (priceText == null) priceText = "";

        Player owner = Bukkit.getPlayer(pendingShop.getOwnerUUID());
        if (owner == null) return;
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(owner.getUniqueId());
        if (session == null) return;

        ItemStack displayItem = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = displayItem.getItemMeta();
        List<Component> lore = new ArrayList<>();

        String itemName = pendingShop.getTemplateItemStack().getType().toString().replace("_", " ").toLowerCase();
        itemName = Character.toUpperCase(itemName.charAt(0)) + itemName.substring(1);

        if (quantity > 0) {
            if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                meta.displayName(Component.text(quantity + " x " + itemName, NamedTextColor.YELLOW));
                lore.add(Component.text("Prices (Owner Sells : Owner Buys)", NamedTextColor.GRAY));
                lore.add(Component.text("Format: YOUR_SELL_PRICE:YOUR_BUY_PRICE", NamedTextColor.DARK_AQUA));
                lore.add(Component.text("Example: 100:80 (You sell for 100, buy for 80)", NamedTextColor.DARK_AQUA));
                lore.add(Component.text("Current: " + priceText + " " + currencySymbol, NamedTextColor.AQUA));
            } else if (session.isIntentToAllowPlayerBuy()) { // Owner Sells to Player
                meta.displayName(Component.text("Owner Sells: " + quantity + " x " + itemName, NamedTextColor.YELLOW));
                lore.add(Component.text("Player Buys For: " + priceText + " " + currencySymbol, NamedTextColor.AQUA));
            } else if (session.isIntentToAllowPlayerSell()) { // Owner Buys from Player
                meta.displayName(Component.text("Owner Buys: " + quantity + " x " + itemName, NamedTextColor.YELLOW));
                lore.add(Component.text("Player Sells For: " + priceText + " " + currencySymbol, NamedTextColor.AQUA));
            } else {
                meta.displayName(Component.text("Set Price & Quantity", NamedTextColor.RED));
                lore.add(Component.text("Error: Shop intent not set!", NamedTextColor.RED));
            }
            lore.add(Component.text(" "));
            lore.add(Component.text("Click to Confirm Values", NamedTextColor.GREEN, TextDecoration.ITALIC));
        } else {
            displayItem.setType(Material.BARRIER);
            meta.displayName(Component.text("Invalid Quantity", NamedTextColor.RED));
            lore.add(Component.text("Place " + itemName + " in the first slot.", NamedTextColor.GRAY));
            lore.add(Component.text("Then, type the price in the rename field above.", NamedTextColor.GRAY));
        }
        meta.lore(lore);
        displayItem.setItemMeta(meta);
        anvilInv.setItem(2, displayItem);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (!(event.getInventory() instanceof AnvilInventory)) return;

        String currentViewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        String expectedAnvilTitle = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT);

        if (!currentViewTitle.equals(expectedAnvilTitle)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);
        if (session == null || session.getPendingShop() == null) {
            return;
        }

        String sessionGuiTitle = session.getCurrentGuiTitle();
        String confirmationTitleSerialized = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.CONFIRMATION_TITLE);

        // If current title in session is the confirmation one, it means we clicked slot 2 and proceeded.
        if (sessionGuiTitle != null && sessionGuiTitle.equals(confirmationTitleSerialized)) {
            plugin.getLogger().fine("ShopAnvilListener: Anvil closed for " + player.getName() + ", but setup progressed to confirmation. Normal closure.");
            return;
        }

        // If expected input type is no longer ANVIL_PRICE_QUANTITY, it means it was handled (e.g. confirmed)
        if (session.getExpectedInputType() != ShopSetupGUIManager.InputType.ANVIL_PRICE_QUANTITY) {
            plugin.getLogger().fine("ShopAnvilListener: Anvil closed for " + player.getName() + ", but expected input type changed. Normal closure after confirmation or other transition.");
            return;
        }

        plugin.getLogger().info("ShopAnvilListener: Anvil GUI closed prematurely by " + player.getName() + ". Reverting to item selection.");

        ItemStack itemsInSlot0 = event.getInventory().getItem(0);
        if (itemsInSlot0 != null && itemsInSlot0.getType() != Material.AIR) {
            ItemStack itemToReturn = itemsInSlot0.clone();
            event.getInventory().setItem(0, null); // Clear from Anvil before giving back
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemToReturn);
            if (!leftovers.isEmpty()) {
                for (ItemStack leftoverItem : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                }
                player.sendMessage(ChatColor.YELLOW + "Some items from the Anvil couldn't fit and were dropped!");
            }
            player.sendMessage(ChatColor.YELLOW + "Items from Anvil slot returned to your inventory.");
        }

        final Shop pendingShop = session.getPendingShop();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
                }
            }
        }.runTask(plugin);
    }
}