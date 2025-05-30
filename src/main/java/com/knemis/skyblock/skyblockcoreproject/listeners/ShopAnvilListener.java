package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration; // Required for TextDecoration.ITALIC
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

        if (event.getClickedInventory() != anvilInv) { // Clicked in player's inventory
            if (event.isShiftClick()) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == templateItem.getType()) {
                    ItemStack currentAnvilItem = anvilInv.getItem(0);
                    int amountToAdd = clickedItem.getAmount();
                    if (currentAnvilItem == null || currentAnvilItem.getType() == Material.AIR) {
                        ItemStack toPlace = clickedItem.clone(); // Clone before modifying/setting
                        anvilInv.setItem(0, toPlace);
                        event.setCurrentItem(null);
                    } else if (currentAnvilItem.getType() == templateItem.getType() && currentAnvilItem.getAmount() < currentAnvilItem.getMaxStackSize()) {
                        int canAdd = currentAnvilItem.getMaxStackSize() - currentAnvilItem.getAmount();
                        if (amountToAdd > canAdd) {
                            currentAnvilItem.setAmount(currentAnvilItem.getMaxStackSize());
                            clickedItem.setAmount(amountToAdd - canAdd);
                        } else {
                            currentAnvilItem.setAmount(currentAnvilItem.getAmount() + amountToAdd);
                            event.setCurrentItem(null);
                        }
                        anvilInv.setItem(0, currentAnvilItem); // Re-set if modified
                    }
                    event.setCancelled(true);
                    updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                } else if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        int rawSlot = event.getRawSlot();

        if (rawSlot == 1) { // Middle slot
            event.setCancelled(true);
        } else if (rawSlot == 0) { // Quantity input slot
            ItemStack cursorItem = event.getCursor();
            boolean isPlacingAction = event.getAction() == InventoryAction.PLACE_ONE ||
                    event.getAction() == InventoryAction.PLACE_ALL ||
                    event.getAction() == InventoryAction.PLACE_SOME;
            boolean isSwappingMatching = event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() == templateItem.getType();

            if (isPlacingAction || isSwappingMatching) {
                if (cursorItem != null && cursorItem.getType() != templateItem.getType()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only place " + templateItem.getType().toString().replace("_", " ") + " here.");
                } else {
                    event.setCancelled(false);
                    updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                }
            } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                    (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && (cursorItem == null || cursorItem.getType() == Material.AIR))) {
                event.setCancelled(false);
                updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
            } else {
                event.setCancelled(true);
            }
        } else if (rawSlot == 2) { // Output slot
            event.setCancelled(true);

            ItemStack quantityItem = anvilInv.getItem(0);
            if (quantityItem == null || quantityItem.getType() == Material.AIR || quantityItem.getAmount() <= 0) {
                player.sendMessage(ChatColor.RED + "Please place items in the first slot to set quantity.");
                return;
            }
            int quantity = quantityItem.getAmount();

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
                        if ((buyPrice < 0 && buyPrice != -1) || (sellPrice < 0 && sellPrice != -1)) {
                            player.sendMessage(ChatColor.RED + "Prices must be positive or -1 to disable.");
                        } else if (buyPrice == -1 && sellPrice == -1) {
                            player.sendMessage(ChatColor.RED + "Cannot disable both buying and selling.");
                        } else {
                            priceValid = true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid format for Buy/Sell. Use BUY_PRICE:SELL_PRICE (e.g., 100:80 or -1:80).");
                    }
                } else if (session.isIntentToAllowPlayerBuy()) {
                    buyPrice = Double.parseDouble(renameText.trim());
                    if (buyPrice >= 0) {
                        priceValid = true;
                        sellPrice = -1;
                    } else {
                        player.sendMessage(ChatColor.RED + "Buy price must be a positive number.");
                    }
                } else if (session.isIntentToAllowPlayerSell()) {
                    sellPrice = Double.parseDouble(renameText.trim());
                    if (sellPrice >= 0) {
                        priceValid = true;
                        buyPrice = -1;
                    } else {
                        player.sendMessage(ChatColor.RED + "Sell price must be a positive number.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Error: Shop type (buy/sell intent) not properly set in session.");
                }

                if (priceValid) {
                    pendingShop.setItemQuantityForPrice(quantity);
                    pendingShop.setBuyPrice(buyPrice);
                    pendingShop.setSellPrice(sellPrice);

                    plugin.getLogger().info(String.format("ShopAnvilListener: Player %s confirmed Price/Quantity for shop at %s. Item: %s, Qty: %d, BuyPrice: %.2f, SellPrice: %.2f",
                            player.getName(), Shop.locationToString(pendingShop.getLocation()), templateItem.getType(), quantity, buyPrice, sellPrice));

                    final Player finalPlayer = player;
                    final Shop finalPendingShop = pendingShop;
                    final double finalBuyPrice = buyPrice; // Effectively final for runnable
                    final double finalSellPrice = sellPrice; // Effectively final for runnable

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            shopSetupGUIManager.openConfirmationMenu(finalPlayer, finalPendingShop, finalBuyPrice, finalSellPrice);
                        }
                    }.runTask(plugin);
                    player.closeInventory();

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
                // Ensure player is still online and has an Anvil inventory open (though type check might be enough)
                Player player = (Player) anvilInv.getViewers().get(0);
                if (player != null && player.getOpenInventory().getTopInventory() instanceof AnvilInventory) {
                    updateAnvilOutputSlot((AnvilInventory) player.getOpenInventory().getTopInventory(), pendingShop, currencySymbol);
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
        if (owner == null) return; // Should not happen if session is valid
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
                lore.add(Component.text("Format: BUY_PRICE:SELL_PRICE", NamedTextColor.DARK_AQUA));
                lore.add(Component.text("Example: 100:80 or -1:70", NamedTextColor.DARK_AQUA));
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

        if (sessionGuiTitle != null && sessionGuiTitle.equals(confirmationTitleSerialized)) {
            plugin.getLogger().fine("ShopAnvilListener: Anvil closed for " + player.getName() + ", but setup progressed to confirmation. Normal closure.");
            return;
        }

        if (session.getExpectedInputType() != ShopSetupGUIManager.InputType.ANVIL_PRICE_QUANTITY) {
            plugin.getLogger().fine("ShopAnvilListener: Anvil closed for " + player.getName() + ", but expected input type changed. Normal closure after confirmation or other transition.");
            return;
        }

        plugin.getLogger().info("ShopAnvilListener: Anvil GUI closed prematurely by " + player.getName() + ". Reverting to item selection.");

        ItemStack itemsInSlot0 = event.getInventory().getItem(0);
        if (itemsInSlot0 != null && itemsInSlot0.getType() != Material.AIR) {
            player.getInventory().addItem(itemsInSlot0.clone());
            player.sendMessage(ChatColor.YELLOW + "Items from Anvil slot returned to your inventory.");
        }

        final Shop pendingShop = session.getPendingShop();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) { // Ensure player is still online
                    shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
                }
            }
        }.runTask(plugin);
    }
}