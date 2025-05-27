// com/knemis/skyblock/skyblockcoreproject/gui/shopvisit/ShopVisitGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui.shopvisit;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject; // SkyBlockProject import (for plugin reference)
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // Added for stock information

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
                meta.displayName(Component.text(shopManager.shortenItemName(templateItem.getType().toString()), NamedTextColor.AQUA)); // Corrected method call
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
                currentStock = shopManager.countItemsInChest(chest, templateItem); // Count stock with templateItem
            }
            lore.add(Component.text("Stock: ", NamedTextColor.GRAY).append(Component.text(currentStock + " units", currentStock > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)));
            lore.add(Component.text(" "));
            lore.add(Component.text("Click to purchase!", Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC)));

            meta.lore(lore);
            saleItemDisplay.setItemMeta(meta);
        }

        // Fill other slots with placeholder item
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            placeholder.setItemMeta(placeholderMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (i != 13) { // Exclude slot 13 (middle)
                gui.setItem(i, placeholder.clone());
            }
        }

        gui.setItem(13, saleItemDisplay); // Product for sale in the middle slot

        player.openInventory(gui);
    }
}