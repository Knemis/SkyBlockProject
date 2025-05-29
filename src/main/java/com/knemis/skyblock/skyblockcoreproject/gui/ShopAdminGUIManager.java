// com/knemis/skyblock/skyblockcoreproject/gui/ShopAdminGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable; // For chat input timeout or next step GUI opening

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ShopAdminGUIManager {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;

    public static final Component SHOP_ADMIN_TITLE = Component.text("Mağaza Yönetimi", Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD));

    // Constants for slot numbers in the admin GUI
    public static final int DISPLAY_NAME_SLOT = 11;
    public static final int PRICE_SLOT = 13;
    private static final int INFO_SLOT = 4; // General info/current item display
    // Add more slots as needed for future functions

    private final ItemStack PLACEHOLDER_ITEM_BLACK;

    public ShopAdminGUIManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;

        PLACEHOLDER_ITEM_BLACK = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = PLACEHOLDER_ITEM_BLACK.getItemMeta();
        if (blackMeta != null) {
            blackMeta.displayName(Component.text(" "));
            PLACEHOLDER_ITEM_BLACK.setItemMeta(blackMeta);
        }
    }

    /**
     * Opens the main shop administration GUI for the shop owner.
     * @param player The shop owner.
     * @param shop The shop to be administered.
     */
    public void openAdminMenu(Player player, Shop shop) {
        if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bu mağazayı yönetme yetkiniz yok veya mağaza bulunamadı.");
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 27, SHOP_ADMIN_TITLE); // 3 rows

        // 1. Info Item (Displays current item being sold)
        ItemStack infoItem = shop.getTemplateItemStack() != null ? shop.getTemplateItemStack().clone() : new ItemStack(Material.BARRIER);
        if (shop.getTemplateItemStack() != null) infoItem.setAmount(shop.getItemQuantityForPrice());
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            Component currentDisplayName = infoMeta.hasDisplayName() ? infoMeta.displayName() : Component.text(infoItem.getType().toString(), NamedTextColor.AQUA);
            infoMeta.displayName(Component.text("Satılan Eşya: ").color(NamedTextColor.YELLOW).append(currentDisplayName));
            List<Component> infoLore = new ArrayList<>();
            infoLore.add(Component.text("Bu, mağazanızda sattığınız eşyadır.").color(NamedTextColor.GRAY));
            infoMeta.lore(infoLore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(INFO_SLOT, infoItem);


        // 2. Change Display Name Item
        ItemStack displayNameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta displayNameMeta = displayNameItem.getItemMeta();
        if (displayNameMeta != null) {
            displayNameMeta.displayName(Component.text("Mağaza Adını Değiştir", NamedTextColor.GOLD));
            List<Component> dnLore = new ArrayList<>();
            dnLore.add(Component.text("Mevcut Ad: ").color(NamedTextColor.GRAY)
                    .append(Component.text(shop.getShopDisplayName() != null && !shop.getShopDisplayName().isEmpty() ? shop.getShopDisplayName() : "Yok", NamedTextColor.WHITE)));
            dnLore.add(Component.text(" "));
            dnLore.add(Component.text("Mağazanız için özel bir isim belirleyin.", NamedTextColor.AQUA));
            dnLore.add(Component.text("Tabela veya listelerde görünebilir.", NamedTextColor.AQUA));
            dnLore.add(Component.text("Tıklayın ve yeni ismi chat'e yazın.", NamedTextColor.YELLOW));
            displayNameMeta.lore(dnLore);
            displayNameItem.setItemMeta(displayNameMeta);
        }
        gui.setItem(DISPLAY_NAME_SLOT, displayNameItem);

        // 3. Change Price Item
        ItemStack priceItem = new ItemStack(Material.EMERALD); // Or GOLD_INGOT
        ItemMeta priceMeta = priceItem.getItemMeta();
        if (priceMeta != null) {
            priceMeta.displayName(Component.text("Fiyatı Değiştir", NamedTextColor.GREEN));
            List<Component> priceLore = new ArrayList<>();
            String currencySymbol = shopManager.getCurrencySymbol(); // Get symbol from ShopManager
            priceLore.add(Component.text("Mevcut Fiyat: ").color(NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.2f%s / %d adet", shop.getPrice(), currencySymbol, shop.getItemQuantityForPrice()), NamedTextColor.WHITE)));
            priceLore.add(Component.text(" "));
            priceLore.add(Component.text("Mağazanızdaki ürünün paket fiyatını değiştirin.", NamedTextColor.AQUA));
            priceLore.add(Component.text("Tıklayın ve yeni fiyatı chat'e yazın.", NamedTextColor.YELLOW));
            priceMeta.lore(priceLore);
            priceItem.setItemMeta(priceMeta);
        }
        gui.setItem(PRICE_SLOT, priceItem);

        // Fill background
        fillGuiBackground(gui, PLACEHOLDER_ITEM_BLACK, INFO_SLOT, DISPLAY_NAME_SLOT, PRICE_SLOT);

        player.openInventory(gui);
        // Store that the player is in an admin GUI for this shop
        plugin.getPlayerAdministeringShop().put(player.getUniqueId(), shop.getLocation());
    }

    /**
     * Fills the GUI background with a placeholder item, excluding specified slots.
     */
    private void fillGuiBackground(Inventory gui, ItemStack placeholder, Integer... excludedSlots) {
        List<Integer> excluded = Arrays.asList(excludedSlots);
        for (int i = 0; i < gui.getSize(); i++) {
            if (!excluded.contains(i) && gui.getItem(i) == null) {
                gui.setItem(i, placeholder.clone());
            }
        }
    }

    /**
     * Handles the process of a player changing the shop's display name.
     * Called from the listener when the "Change Display Name" item is clicked.
     * @param player The player.
     * @param shop The shop being administered.
     */
    public void initiateDisplayNameChange(Player player, Shop shop) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "===== Mağaza Adı Değiştirme =====");
        player.sendMessage(ChatColor.YELLOW + "Yeni mağaza adını chat'e yazın.");
        player.sendMessage(ChatColor.GRAY + "İptal etmek için '" + ChatColor.RED + "iptal" + ChatColor.GRAY + "' yazın.");
        plugin.getPlayerWaitingForAdminInput().put(player.getUniqueId(), AdminInputType.SHOP_DISPLAY_NAME);
        // A timeout for this input could be added with a BukkitRunnable
    }

    /**
     * Handles the process of a player changing the shop's price.
     * Called from the listener when the "Change Price" item is clicked.
     * @param player The player.
     * @param shop The shop being administered.
     */
    public void initiatePriceChange(Player player, Shop shop) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "===== Mağaza Fiyatı Değiştirme =====");
        player.sendMessage(ChatColor.YELLOW + "Yeni paket fiyatını chat'e yazın (örn: 100.50 veya 50).");
        player.sendMessage(ChatColor.GRAY + "Bu fiyat, " + shop.getItemQuantityForPrice() + " adet " +
                (shop.getTemplateItemStack() != null ? shop.getTemplateItemStack().getType().toString() : "eşya") + " içindir.");
        player.sendMessage(ChatColor.GRAY + "İptal etmek için '" + ChatColor.RED + "iptal" + ChatColor.GRAY + "' yazın.");
        plugin.getPlayerWaitingForAdminInput().put(player.getUniqueId(), AdminInputType.SHOP_PRICE);
        // A timeout for this input could be added
    }

    // Enum to track what kind of admin input we are waiting for from the player in chat
    public enum AdminInputType {
        SHOP_DISPLAY_NAME,
        SHOP_PRICE
        // Add more as new editable fields are introduced
    }

    // Additional functions to be added incrementally:
    // - public void processDisplayNameInput(Player player, Shop shop, String input)
    // - public void processPriceInput(Player player, Shop shop, String input)
    // - Functions to handle other admin actions (e.g., changing item, managing stock GUI, etc.)
    // - Helper function to create styled ItemStacks for the GUI more easily
    // - ... and more, as we expand features.
}