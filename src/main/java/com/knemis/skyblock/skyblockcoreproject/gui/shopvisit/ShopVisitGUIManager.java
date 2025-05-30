// com/knemis/skyblock/skyblockcoreproject/gui/shopvisit/ShopVisitGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui.shopvisit;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject; // SkyBlockProject import (for plugin reference)
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopInventoryManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // Added for stock information
import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode; // Required for checking shop mode

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer; // Import OfflinePlayer
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopVisitGUIManager {

    public static final Component SHOP_VISIT_TITLE = Component.text("Buy from Shop", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD));
    private final SkyBlockProject plugin; // To hold the plugin reference
    private final ShopManager shopManager; // For stock counting

    // Add plugin and shopManager parameters to the constructor
    public ShopVisitGUIManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    /**
     * Opens the shop purchase GUI for the customer.
     * @param player The visiting player.
     * @param shop The shop object.
     */
    public void openShopVisitMenu(Player player, Shop shop) {
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "This shop cannot be viewed currently or is misconfigured.");
            return;
        }

        Component guiTitle = SHOP_VISIT_TITLE;
        if (shop.getShopDisplayName() != null && !shop.getShopDisplayName().isEmpty()) {
            guiTitle = Component.text("Shop: ", NamedTextColor.GOLD).append(Component.text(shop.getShopDisplayName(), NamedTextColor.YELLOW));
        } else {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
            String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
            guiTitle = Component.text(ownerName + "'s Shop", NamedTextColor.GOLD);
        }

        Inventory gui = Bukkit.createInventory(player, 27, guiTitle); // Assign buyer as inventory owner

        ItemStack templateItem = shop.getTemplateItemStack();
        ItemStack saleItemDisplay = templateItem.clone(); // Always work with a clone
        saleItemDisplay.setAmount(shop.getItemQuantityForPrice()); // Amount per price

        ItemMeta meta = saleItemDisplay.getItemMeta();
        if (meta != null) {
            // Keep existing displayName or assign a new one
            if (!meta.hasDisplayName()) {
                meta.displayName(Component.text(this.shopManager.getShopSignManager().shortenItemName(templateItem.getType().toString()), NamedTextColor.AQUA)); // Corrected method call
                // Default name
            }
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            if(lore == null) lore = new ArrayList<>(); // Null safety (practically made redundant by new ArrayList<>() above)

            lore.add(Component.text(" ")); // Empty line
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(shop.getOwnerUUID()); // Define and use shopOwner here
            String shopOwnerName = shopOwner.getName() != null ? shopOwner.getName() : "Unknown";
            lore.add(Component.text("Seller: ", NamedTextColor.GRAY).append(Component.text(shopOwnerName, NamedTextColor.DARK_AQUA)));
            lore.add(Component.text("Bundle: ", NamedTextColor.GRAY).append(Component.text(shop.getItemQuantityForPrice() + " units", NamedTextColor.WHITE)));

            String currencyName = (plugin.getEconomy() != null && plugin.getEconomy().currencyNamePlural() != null && !plugin.getEconomy().currencyNamePlural().isEmpty()) ? plugin.getEconomy().currencyNamePlural() : "$";
            // Corrected Component.text call
            lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text(String.format("%.2f", shop.getPrice()) + " " + currencyName, Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))));

            // Stock Information
            int currentStock = 0;
            if (shop.getLocation().getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) shop.getLocation().getBlock().getState();
                currentStock = ShopInventoryManager.countItemsInChest(chest, templateItem); // Count stock with templateItem
            }
            lore.add(Component.text("Stock: ", NamedTextColor.GRAY).append(Component.text(currentStock + " units", currentStock > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)));
            lore.add(Component.text(" ")); // Empty line before specific instructions

            // Default placeholder item
            ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            if (placeholderMeta != null) {
                placeholderMeta.displayName(Component.text(" "));
                placeholder.setItemMeta(placeholderMeta);
            }
            for (int i = 0; i < gui.getSize(); i++) {
                 gui.setItem(i, placeholder.clone()); // Fill all slots initially
            }


            if (shop.getShopMode() == ShopMode.BANK_CHEST) {
                lore.add(Component.text("This is a BANK Chest shop.", NamedTextColor.AQUA));
                // Buttons for BANK_CHEST mode
                if (shop.getBuyPrice() >= 0) { // Valid buy price
                    ItemStack buyButton = new ItemStack(Material.GREEN_WOOL);
                    ItemMeta buyMeta = buyButton.getItemMeta();
                    buyMeta.displayName(Component.text("Buy 1 Bundle", NamedTextColor.GREEN, TextDecoration.BOLD));
                    List<Component> buyLore = new ArrayList<>();
                    buyLore.add(Component.text("Click to buy ", NamedTextColor.GRAY)
                        .append(Component.text(shop.getItemQuantityForPrice() + " " + shopManager.getShopSignManager().shortenItemName(templateItem.getType().toString()), NamedTextColor.WHITE))
                        .append(Component.text(" for ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.2f %s", shop.getPrice(), currencyName), NamedTextColor.GOLD)));
                    buyMeta.lore(buyLore);
                    buyButton.setItemMeta(buyMeta);
                    gui.setItem(20, buyButton);
                } else {
                    // Optional: Placeholder or info item if not buyable
                    ItemStack notBuyable = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta notBuyableMeta = notBuyable.getItemMeta();
                    notBuyableMeta.displayName(Component.text("Not for Sale", NamedTextColor.RED));
                    notBuyable.setItemMeta(notBuyableMeta);
                    gui.setItem(20, notBuyable);
                }

                if (shop.getSellPrice() >= 0) { // Valid sell price
                    ItemStack sellButton = new ItemStack(Material.RED_WOOL);
                    ItemMeta sellMeta = sellButton.getItemMeta();
                    sellMeta.displayName(Component.text("Sell 1 Bundle", NamedTextColor.RED, TextDecoration.BOLD));
                    List<Component> sellLore = new ArrayList<>();
                    sellLore.add(Component.text("Click to sell ", NamedTextColor.GRAY)
                        .append(Component.text(shop.getItemQuantityForPrice() + " " + shopManager.getShopSignManager().shortenItemName(templateItem.getType().toString()), NamedTextColor.WHITE))
                        .append(Component.text(" for ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.2f %s", shop.getSellPrice(), currencyName), NamedTextColor.GOLD)));
                    sellMeta.lore(sellLore);
                    sellButton.setItemMeta(sellMeta);
                    gui.setItem(24, sellButton);
                } else {
                    // Optional: Placeholder or info item if not sellable
                    ItemStack notSellable = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta notSellableMeta = notSellable.getItemMeta();
                    notSellableMeta.displayName(Component.text("Not Buying", NamedTextColor.DARK_RED));
                    notSellable.setItemMeta(notSellableMeta);
                    gui.setItem(24, notSellable);
                }
                // The main item display in BANK_CHEST mode doesn't need specific click instructions, as buttons are used.
                // Remove the generic "Click to purchase!" for BANK_CHEST if buttons are present.
                // lore.remove(lore.size() -1); // If "Click to purchase!" was the last added generic line.
                // For BANK_CHEST, the general info on item at slot 13 is enough.

            } else if (shop.getShopMode() == ShopMode.MARKET_CHEST) {
                lore.add(Component.text("This is a MARKET Chest shop.", NamedTextColor.LIGHT_PURPLE));
                if (shop.getBuyPrice() >= 0) {
                    lore.add(Component.text("Left-click to enter buy quantity in chat.", Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC)));
                }
                if (shop.getSellPrice() >= 0) {
                    lore.add(Component.text("Right-click to enter sell quantity in chat.", Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC)));
                }
                // For MARKET_CHEST, slots 20 and 24 are not used as per task description for this fix.
                // Listener handles clicks on slot 13 to initiate chat.
            } else { // Default or other modes
                // The old "Click to purchase!" might be relevant here or a more generic message.
                 lore.add(Component.text("Click the item to interact.", Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC)));
            }

            meta.lore(lore);
            saleItemDisplay.setItemMeta(meta);
        }
        // Ensure slot 13 is set after potential modifications by mode-specific logic
        gui.setItem(13, saleItemDisplay);

        player.openInventory(gui);
    }
}