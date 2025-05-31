// com/knemis/skyblock/skyblockcoreproject/gui/ShopAdminGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
// import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer; // Not directly used after changes

import org.bukkit.Bukkit;
import org.bukkit.ChatColor; // Keep for messages
import org.bukkit.Location; // Added import for Location
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType; // Added for Anvil GUI
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable; // For chat input timeout or next step GUI opening

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; // Added for new maps
import java.util.List;
import java.util.Map; // Added for new maps
import java.util.UUID;

public class PlayerShopAdminGUIManager { // Renamed class

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;

    public static final Component SHOP_ADMIN_TITLE = Component.text("Mağaza Yönetimi", Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD));

    // Constants for slot numbers in the admin GUI
    public static final int DISPLAY_NAME_SLOT = 11;
    public static final int PRICE_SLOT = 13;
    private static final int INFO_SLOT = 4; // General info/current item display
    // Add more slots as needed for future functions

    private final ItemStack PLACEHOLDER_ITEM_BLACK;

    // Added fields for player state tracking
    private final Map<UUID, Location> playerAdministeringShop = new HashMap<>();
    private final Map<UUID, PlayerShopAdminGUIManager.AdminInputType> playerWaitingForAdminInput = new HashMap<>();

    // Constants for Anvil GUI titles
    public static final Component ANVIL_DISPLAY_NAME_TITLE = Component.text("Set Shop Name");
    public static final Component ANVIL_PRICE_TITLE = Component.text("Set Shop Price");

    public PlayerShopAdminGUIManager(SkyBlockProject plugin, ShopManager shopManager) { // Renamed constructor
        System.out.println("[TRACE] In PlayerShopAdminGUIManager constructor. Plugin is " + (plugin == null ? "null" : "not null") + ", ShopManager is " + (shopManager == null ? "null" : "not null"));
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
        System.out.println("[TRACE] In PlayerShopAdminGUIManager.openAdminMenu for player " + player.getName() + " and shop " + (shop != null ? shop.getShopId() : "null")); // Renamed trace
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
        this.playerAdministeringShop.put(player.getUniqueId(), shop.getLocation()); // Use local map
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
        plugin.getLogger().info("[PlayerShopAdminGUIManager] Player " + player.getName() + " initiating display name change for shop " + shop.getShopId() + " via Anvil.");
        this.getPlayerWaitingForAdminInput().put(player.getUniqueId(), AdminInputType.SHOP_DISPLAY_NAME_ANVIL);
        // Actual Anvil opening will be done by a new method, called here or directly by listener.
        // For now, just set state. The actual opening will be added in this method.
        openAnvilForShopSetting(player, shop, AdminInputType.SHOP_DISPLAY_NAME_ANVIL);
    }

    /**
     * Handles the process of a player changing the shop's price.
     * Called from the listener when the "Change Price" item is clicked.
     * @param player The player.
     * @param shop The shop being administered.
     */
    public void initiatePriceChange(Player player, Shop shop) {
        plugin.getLogger().info("[PlayerShopAdminGUIManager] Player " + player.getName() + " initiating price change for shop " + shop.getShopId() + " via Anvil.");
        this.getPlayerWaitingForAdminInput().put(player.getUniqueId(), AdminInputType.SHOP_PRICE_ANVIL);
        openAnvilForShopSetting(player, shop, AdminInputType.SHOP_PRICE_ANVIL);
    }

    private void openAnvilForShopSetting(Player player, Shop shop, AdminInputType inputType) {
        Inventory anvilGui;
        ItemStack firstSlotItem = new ItemStack(Material.PAPER); // Default item
        ItemMeta firstSlotMeta = firstSlotItem.getItemMeta();

        if (inputType == AdminInputType.SHOP_DISPLAY_NAME_ANVIL) {
            anvilGui = Bukkit.createInventory(player, InventoryType.ANVIL, ANVIL_DISPLAY_NAME_TITLE);
            if (firstSlotMeta != null) {
                firstSlotMeta.displayName(Component.text(shop.getShopDisplayName() != null ? shop.getShopDisplayName() : "Current Name"));
            }
            firstSlotItem.setType(Material.NAME_TAG);
        } else if (inputType == AdminInputType.SHOP_PRICE_ANVIL) {
            anvilGui = Bukkit.createInventory(player, InventoryType.ANVIL, ANVIL_PRICE_TITLE);
             if (firstSlotMeta != null) {
                firstSlotMeta.displayName(Component.text(String.format("%.2f", shop.getPrice()))); // Using getPrice() which is buy price
            }
            firstSlotItem.setType(Material.EMERALD);
        } else {
            return; // Should not happen
        }

        if (firstSlotMeta != null) firstSlotItem.setItemMeta(firstSlotMeta);
        anvilGui.setItem(0, firstSlotItem);
        // Slot 1 is empty, Slot 2 (result) is managed by Anvil/Listener

        player.openInventory(anvilGui);
    }


    // Enum to track what kind of admin input we are waiting for from the player in chat
    public enum AdminInputType {
        SHOP_DISPLAY_NAME_ANVIL, // For Anvil GUI
        SHOP_PRICE_ANVIL         // For Anvil GUI
    }

    // Getter methods for the new maps
    public Map<UUID, Location> getPlayerAdministeringShop() {
        return playerAdministeringShop;
    }

    public Map<UUID, PlayerShopAdminGUIManager.AdminInputType> getPlayerWaitingForAdminInput() {
        return playerWaitingForAdminInput;
    }

    public void processAnvilDisplayNameInput(Player player, String newName) {
        Shop shop = getShopForAdmin(player);
        if (shop == null) return; // Error already sent by getShopForAdmin
        plugin.getLogger().info("[PlayerShopAdminGUIManager] Processing Anvil display name input for " + player.getName() + ", shop " + shop.getShopId() + ". New name: " + newName);
        shop.setShopDisplayName(newName);
        shopManager.saveShop(shop); // Make sure shopManager is accessible
        player.sendMessage(ChatColor.GREEN + "Shop display name updated to: " + newName);
        // Optionally, re-open the admin menu or close inventory.
        // For now, let listener handle inventory closure.
        this.getPlayerWaitingForAdminInput().remove(player.getUniqueId());
         // Re-open admin menu to show updated info
        openAdminMenu(player, shop);
    }

    public void processAnvilPriceInput(Player player, String newPriceStr) {
        Shop shop = getShopForAdmin(player);
        if (shop == null) return;
         plugin.getLogger().info("[PlayerShopAdminGUIManager] Processing Anvil price input for " + player.getName() + ", shop " + shop.getShopId() + ". New price string: " + newPriceStr);
        try {
            double newPrice = Double.parseDouble(newPriceStr);
            if (newPrice < 0) {
                player.sendMessage(ChatColor.RED + "Price cannot be negative.");
                openAnvilForShopSetting(player, shop, AdminInputType.SHOP_PRICE_ANVIL); // Re-open anvil
                return;
            }
            // Assuming this is for the BUY price. If SELL price needs separate editing, more logic is needed.
            shop.setBuyPrice(newPrice); // Or setPrice() if that's the primary one
            shopManager.saveShop(shop);
            player.sendMessage(ChatColor.GREEN + "Shop price updated to: " + String.format("%.2f", newPrice));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid price format. Please enter a number.");
            openAnvilForShopSetting(player, shop, AdminInputType.SHOP_PRICE_ANVIL); // Re-open anvil
            return;
        }
        this.getPlayerWaitingForAdminInput().remove(player.getUniqueId());
        openAdminMenu(player, shop);
    }

    // Helper to get shop, reduces boilerplate in process methods
    private Shop getShopForAdmin(Player player) {
        Location shopLocation = this.getPlayerAdministeringShop().get(player.getUniqueId());
        if (shopLocation == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop administration session expired.");
            this.getPlayerWaitingForAdminInput().remove(player.getUniqueId()); // Clean up state
            return null;
        }
        Shop shop = shopManager.getActiveShop(shopLocation); // Ensure shopManager is accessible
        if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Error: Shop not found or you do not own this shop.");
             this.getPlayerAdministeringShop().remove(player.getUniqueId()); // Clean up state
             this.getPlayerWaitingForAdminInput().remove(player.getUniqueId());
            return null;
        }
        return shop;
    }
}