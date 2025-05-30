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
        int rawSlot = event.getRawSlot();

        // Handle player inventory clicks
        if (event.getClickedInventory() != anvilInv) {
            if (event.isShiftClick()) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == templateItem.getType()) {
                    event.setCancelled(true);

                    // Verify the item is still there
                    if (clickedItem.getAmount() <= 0) {
                        return;
                    }

                    ItemStack toAdd = clickedItem.clone();
                    toAdd.setAmount(1);

                    // Add to anvil slot 0
                    ItemStack current = anvilInv.getItem(0);
                    if (current == null) {
                        anvilInv.setItem(0, toAdd);
                    } else if (current.getType() == templateItem.getType()) {
                        if (current.getAmount() < current.getMaxStackSize()) {
                            current.setAmount(current.getAmount() + 1);
                            anvilInv.setItem(0, current);
                        } else {
                            return;
                        }
                    }

                    // Remove from player inventory
                    if (clickedItem.getAmount() == 1) {
                        event.setCurrentItem(new ItemStack(Material.AIR));
                    } else {
                        clickedItem.setAmount(clickedItem.getAmount() - 1);
                        event.setCurrentItem(clickedItem);
                    }

                    // Update inventory immediately
                    player.updateInventory();

                    // Schedule the output update with a slight delay
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && player.getOpenInventory().getTopInventory() == anvilInv) {
                            updateAnvilOutputSlot(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                        }
                    }, 1L);
                }
            }
            return;
        }

        // Handle anvil inventory clicks
        event.setCancelled(true); // Cancel all anvil clicks by default

        if (rawSlot == 0) { // Input slot
            // Allow taking items out
            if (event.getAction() == InventoryAction.PICKUP_ALL ||
                    event.getAction() == InventoryAction.PICKUP_HALF ||
                    event.getAction() == InventoryAction.PICKUP_SOME ||
                    event.getAction() == InventoryAction.PICKUP_ONE) {

                event.setCancelled(false); // Let default behavior handle taking items
                updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
            }
        }
        else if (rawSlot == 2) { // Output slot
            ItemStack inputItem = anvilInv.getItem(0);
            if (inputItem == null || inputItem.getType() != templateItem.getType()) {
                player.sendMessage(ChatColor.RED + "Please place the correct item in the first slot.");
                return;
            }

            String priceText = anvilInv.getRenameText();
            if (priceText == null || priceText.trim().isEmpty()) {
                player.sendMessage(ChatColor.RED + "Please enter a valid price.");
                return;
            }

            try {
                // Parse the price input
                String[] priceParts = priceText.split(":");
                double buyPrice = -1;
                double sellPrice = -1;

                if (priceParts.length == 2) {
                    buyPrice = Double.parseDouble(priceParts[0].trim());
                    sellPrice = Double.parseDouble(priceParts[1].trim());
                } else if (priceParts.length == 1) {
                    if (session.isIntentToAllowPlayerBuy()) {
                        buyPrice = Double.parseDouble(priceParts[0].trim());
                    } else if (session.isIntentToAllowPlayerSell()) {
                        sellPrice = Double.parseDouble(priceParts[0].trim());
                    }
                }

                // Set the shop values
                pendingShop.setItemQuantityForPrice(inputItem.getAmount());
                if (buyPrice > 0) pendingShop.setPrice(buyPrice);
                if (sellPrice > 0) pendingShop.setSellPrice(sellPrice);

                // Proceed to confirmation
                shopSetupGUIManager.openConfirmationMenu(player, pendingShop, buyPrice, sellPrice);

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid price format! Use numbers only (e.g. '100' or '100:80').");
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

    private void updateAnvilOutputSlot(AnvilInventory anvilInv, Shop pendingShop, String currencySymbol) {        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) return;
        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) return;

        // Check if the viewer is still valid
        if (anvilInv.getViewers().isEmpty()) return;
        Player player = (Player) anvilInv.getViewers().get(0);
        if (player == null || !player.isOnline()) return;
        ItemStack quantityItem = anvilInv.getItem(0);
        Material expectedType = pendingShop.getTemplateItemStack().getType();
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
        if (quantityItem != null && quantityItem.getType() != expectedType) {
            anvilInv.setItem(2, null);
            return;
        }
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
        try {
            anvilInv.setItem(2, displayItem);
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating anvil output slot: " + e.getMessage());
            if (player.isOnline()) {
                player.sendMessage(ChatColor.RED + "An error occurred. Please try again.");
            }
        }
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