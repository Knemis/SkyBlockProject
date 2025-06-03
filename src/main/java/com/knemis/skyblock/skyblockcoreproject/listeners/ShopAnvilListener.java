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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent; // Added import
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // Added import
import java.util.UUID;

public class ShopAnvilListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopSetupGUIManager shopSetupGUIManager;
    private final Map<UUID, String> playerAnvilInputs = new HashMap<>(); // Added map

    public ShopAnvilListener(SkyBlockProject plugin, ShopSetupGUIManager shopSetupGUIManager) {
        this.plugin = plugin;
        this.shopSetupGUIManager = shopSetupGUIManager;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) return;
        Player player = (Player) event.getView().getPlayer();
        // AnvilInventory anvilInventory = event.getInventory(); // Not needed if using event.getRenameText()
        String renameText = event.getRenameText(); // Use direct event method

        // Store the input, even if it's empty, to reflect user clearing text
        // Only store if it's relevant to our shop setup
        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(player.getUniqueId());
        if (session != null && session.getExpectedInputType() == ShopSetupGUIManager.InputType.ANVIL_PRICE_QUANTITY) {
            playerAnvilInputs.put(player.getUniqueId(), renameText != null ? renameText : "");
        }
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
            player.sendMessage(Component.text("Shop setup session expired or invalid. Please start over.", NamedTextColor.RED));
            playerAnvilInputs.remove(player.getUniqueId()); // Clean up map
            return;
        }

        if (session.getExpectedInputType() != ShopSetupGUIManager.InputType.ANVIL_PRICE_QUANTITY) {
            // Not expecting anvil input for shop setup, but might be for other anvil uses, so don't clear map yet.
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
                    if (clickedItem.getAmount() <= 0) return;
                    ItemStack toAdd = clickedItem.clone();
                    toAdd.setAmount(1);
                    ItemStack current = anvilInv.getItem(0);
                    if (current == null) {
                        anvilInv.setItem(0, toAdd);
                    } else if (current.getType() == templateItem.getType()) {
                        if (current.getAmount() < current.getMaxStackSize()) {
                            current.setAmount(current.getAmount() + 1);
                            anvilInv.setItem(0, current);
                        } else return;
                    }
                    if (clickedItem.getAmount() == 1) event.setCurrentItem(new ItemStack(Material.AIR));
                    else clickedItem.setAmount(clickedItem.getAmount() - 1);
                    player.updateInventory();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && player.getOpenInventory().getTopInventory() == anvilInv) {
                            updateAnvilOutputSlot(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                        }
                    }, 1L);
                }
            }
            return;
        }

        if (rawSlot == 0) {
            InventoryAction action = event.getAction();
            if (action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_ONE ||
                action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR) {
                if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR) {
                    ItemStack cursorItem = event.getCursor();
                    if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                        if (cursorItem.getType() != templateItem.getType()) {
                            event.setCancelled(true);
                            player.sendMessage(Component.text("You can only place " + templateItem.getType() + " in this slot.", NamedTextColor.RED));
                        } else event.setCancelled(false);
                    } else event.setCancelled(false);
                } else event.setCancelled(false);
                if (!event.isCancelled()) {
                    updateAnvilOutputSlotWithDelay(anvilInv, session.getPendingShop(), plugin.getShopManager().getCurrencySymbol());
                }
            } else event.setCancelled(true);
        } else if (rawSlot == 1) {
            event.setCancelled(true);
        } else if (rawSlot == 2) {
            event.setCancelled(true);
            ItemStack inputItem = anvilInv.getItem(0);
            Shop pendingShop = session.getPendingShop();

            if (inputItem == null || inputItem.getType() == Material.AIR) {
                player.sendMessage(Component.text("Please place the item to be traded in the first slot.", NamedTextColor.RED));
                return; // Don't clear map here, player might still be editing
            }
            if (inputItem.getType() != pendingShop.getTemplateItemStack().getType()) {
                player.sendMessage(Component.text("The item in the first slot does not match the required type: " + pendingShop.getTemplateItemStack().getType(), NamedTextColor.RED));
                return; // Don't clear map
            }

            String priceText = playerAnvilInputs.get(player.getUniqueId());
            if (priceText == null || priceText.trim().isEmpty()) {
                player.sendMessage(Component.text("Please enter the price in the rename field.", NamedTextColor.RED));
                // playerAnvilInputs.remove(player.getUniqueId()); // Not necessarily, could be transient issue
                return;
            }

            double buyPrice = -1, sellPrice = -1;
            boolean priceFormatError = false;
            try {
                String[] priceParts = priceText.split(":");
                if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
                    if (priceParts.length == 2) {
                        buyPrice = Double.parseDouble(priceParts[0].trim());
                        sellPrice = Double.parseDouble(priceParts[1].trim());
                        if (buyPrice <= 0 || sellPrice <= 0) {
                            player.sendMessage(Component.text("Both buy and sell prices must be positive.", NamedTextColor.RED)); priceFormatError = true;
                        }
                    } else {
                        player.sendMessage(Component.text("For Buy/Sell shops, use format: BUY_PRICE:SELL_PRICE (e.g., 100:80)", NamedTextColor.RED)); priceFormatError = true;
                    }
                } else if (session.isIntentToAllowPlayerBuy()) {
                    if (priceParts.length == 1) {
                        buyPrice = Double.parseDouble(priceParts[0].trim());
                        if (buyPrice <= 0) { player.sendMessage(Component.text("Buy price must be positive.", NamedTextColor.RED)); priceFormatError = true; }
                    } else {
                         player.sendMessage(Component.text("For 'Owner Sells' shops, just enter the buy price (e.g., 100)", NamedTextColor.RED)); priceFormatError = true;
                    }
                } else if (session.isIntentToAllowPlayerSell()) {
                    if (priceParts.length == 1) {
                        sellPrice = Double.parseDouble(priceParts[0].trim());
                        if (sellPrice <= 0) { player.sendMessage(Component.text("Sell price must be positive.", NamedTextColor.RED)); priceFormatError = true; }
                    } else {
                        player.sendMessage(Component.text("For 'Owner Buys' shops, just enter the sell price (e.g., 80)", NamedTextColor.RED)); priceFormatError = true;
                    }
                } else {
                    player.sendMessage(Component.text("Error: Shop intent (buy/sell) not properly set in session.", NamedTextColor.RED));
                    playerAnvilInputs.remove(player.getUniqueId()); // Clean map on critical error
                    return;
                }
                if (priceFormatError) {
                    // playerAnvilInputs.remove(player.getUniqueId()); // Don't remove, let them correct
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid price format! Use numbers only (e.g., '100' or '100:80').", NamedTextColor.RED));
                // playerAnvilInputs.remove(player.getUniqueId()); // Don't remove
                return;
            }

            pendingShop.setItemQuantityForPrice(inputItem.getAmount());
            if (session.isIntentToAllowPlayerBuy()) pendingShop.setBuyPrice(buyPrice); else pendingShop.setBuyPrice(-1);
            if (session.isIntentToAllowPlayerSell()) pendingShop.setSellPrice(sellPrice); else pendingShop.setSellPrice(-1);

            playerAnvilInputs.remove(player.getUniqueId()); // Clean up map after successful processing
            shopSetupGUIManager.openConfirmationMenu(player, pendingShop, buyPrice, sellPrice);
        }
    }

    private void updateAnvilOutputSlotWithDelay(AnvilInventory anvilInv, Shop pendingShop, String currencySymbol) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (anvilInv.getViewers().isEmpty()) return;
                Player player = (Player) anvilInv.getViewers().get(0);
                if (player != null && player.isOnline() && player.getOpenInventory().getTopInventory() == anvilInv) {
                    updateAnvilOutputSlot(anvilInv, pendingShop, currencySymbol);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void updateAnvilOutputSlot(AnvilInventory anvilInv, Shop pendingShop, String currencySymbol) {
        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) return;
        if (anvilInv.getViewers().isEmpty()) return;
        Player player = (Player) anvilInv.getViewers().get(0);
        if (player == null || !player.isOnline()) return;

        ItemStack quantityItem = anvilInv.getItem(0);
        Material expectedType = pendingShop.getTemplateItemStack().getType();
        int quantity = (quantityItem != null && quantityItem.getType() == expectedType) ? quantityItem.getAmount() : 0;

        // For display, use the live text from anvil input field for responsiveness
        // String currentAnvilText = anvilInv.getRenameText(); // Reverted - Use stored value
        String currentAnvilText = playerAnvilInputs.get(player.getUniqueId());
        if (currentAnvilText == null) currentAnvilText = ""; // Default to empty if null

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
                lore.add(Component.text("Current: " + currentAnvilText + " " + currencySymbol, NamedTextColor.AQUA));
            } else if (session.isIntentToAllowPlayerBuy()) {
                meta.displayName(Component.text("Owner Sells: " + quantity + " x " + itemName, NamedTextColor.YELLOW));
                lore.add(Component.text("Player Buys For: " + currentAnvilText + " " + currencySymbol, NamedTextColor.AQUA));
            } else if (session.isIntentToAllowPlayerSell()) {
                meta.displayName(Component.text("Owner Buys: " + quantity + " x " + itemName, NamedTextColor.YELLOW));
                lore.add(Component.text("Player Sells For: " + currentAnvilText + " " + currencySymbol, NamedTextColor.AQUA));
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
        UUID playerId = player.getUniqueId();

        if (!(event.getInventory() instanceof AnvilInventory)) return;

        String currentViewTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        String expectedAnvilTitle = LegacyComponentSerializer.legacySection().serialize(ShopSetupGUIManager.ANVIL_TITLE_COMPONENT);

        if (!currentViewTitle.equals(expectedAnvilTitle)) {
            return;
        }

        // Always remove from map if this was our anvil GUI, regardless of how it was closed.
        playerAnvilInputs.remove(playerId);

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
            ItemStack itemToReturn = itemsInSlot0.clone();
            event.getInventory().setItem(0, null);
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemToReturn);
            if (!leftovers.isEmpty()) {
                for (ItemStack leftoverItem : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                }
                player.sendMessage(Component.text("Some items from the Anvil couldn't fit and were dropped!", NamedTextColor.YELLOW));
            }
            player.sendMessage(Component.text("Items from Anvil slot returned to your inventory.", NamedTextColor.YELLOW));
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