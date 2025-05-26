// com/knemis/skyblock/skyblockcoreproject/gui/shopvisit/ShopVisitGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui.shopvisit;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject; // SkyBlockProject importu (plugin referansı için)
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // Stok bilgisi için eklendi

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

    public static final Component SHOP_VISIT_TITLE = Component.text("Mağazadan Satın Al", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD));
    private final SkyBlockProject plugin; // Plugin referansını tutmak için
    private final ShopManager shopManager; // Stok sayımı için

    // Constructor'a plugin ve shopManager parametrelerini ekle
    public ShopVisitGUIManager(SkyBlockProject plugin, ShopManager shopManager) { //
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    /**
     * Müşteri için mağaza alışveriş GUI'si açar.
     * @param player Ziyaret eden oyuncu.
     * @param shop Mağaza nesnesi.
     */
    public void openShopVisitMenu(Player player, Shop shop) {
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "Bu mağaza şu anda görüntülenemiyor veya eksik yapılandırılmış.");
            return;
        }

        Component guiTitle = SHOP_VISIT_TITLE;
        if (shop.getShopDisplayName() != null && !shop.getShopDisplayName().isEmpty()) {
            guiTitle = Component.text("Mağaza: ", NamedTextColor.GOLD).append(Component.text(shop.getShopDisplayName(), NamedTextColor.YELLOW));
        } else {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
            String ownerName = owner.getName() != null ? owner.getName() : "Bilinmeyen";
            guiTitle = Component.text(ownerName + "'in Mağazası", NamedTextColor.GOLD);
        }

        Inventory gui = Bukkit.createInventory(player, 27, guiTitle); // Envanter sahibi olarak alıcıyı ata

        ItemStack templateItem = shop.getTemplateItemStack();
        ItemStack saleItemDisplay = templateItem.clone(); // Her zaman klonla çalış
        saleItemDisplay.setAmount(shop.getItemQuantityForPrice()); // Fiyat başına düşen miktar

        ItemMeta meta = saleItemDisplay.getItemMeta();
        if (meta != null) {
            // Mevcut displayName'i koru veya yenisini ata
            if (!meta.hasDisplayName()) {
                // Corrected line:
                meta.displayName(Component.text(shopManager.shortenItemName(templateItem.getType().toString(), 25), NamedTextColor.AQUA));
                // Varsayılan ad
            }
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            if(lore == null) lore = new ArrayList<>(); // Null safety (pratikte yukarıdaki new ArrayList<>() ile gereksizleşir)

            lore.add(Component.text(" ")); // Boş satır - This line is likely correct, review error message source.
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(shop.getOwnerUUID()); // shopOwner'ı burada tanımla ve kullan
            String shopOwnerName = shopOwner.getName() != null ? shopOwner.getName() : "Bilinmeyen";
            lore.add(Component.text("Satıcı: ", NamedTextColor.GRAY).append(Component.text(shopOwnerName, NamedTextColor.DARK_AQUA)));
            lore.add(Component.text("Paket: ", NamedTextColor.GRAY).append(Component.text(shop.getItemQuantityForPrice() + " adet", NamedTextColor.WHITE)));

            String currencyName = (plugin.getEconomy() != null && plugin.getEconomy().currencyNamePlural() != null && !plugin.getEconomy().currencyNamePlural().isEmpty()) ? plugin.getEconomy().currencyNamePlural() : "$";
            // Düzeltilmiş Component.text çağrısı
            lore.add(Component.text("Fiyat: ", NamedTextColor.GRAY).append(Component.text(String.format("%.2f", shop.getPrice()) + " " + currencyName, Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))));

            // Stok Bilgisi
            int currentStock = 0;
            if (shop.getLocation().getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) shop.getLocation().getBlock().getState();
                currentStock = shopManager.countItemsInChest(chest, templateItem); // templateItem ile stok say
            }
            lore.add(Component.text("Stok: ", NamedTextColor.GRAY).append(Component.text(currentStock + " adet", currentStock > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)));
            lore.add(Component.text(" "));
            // Corrected line:
            lore.add(Component.text("Satın almak için tıkla!", Style.style(NamedTextColor.YELLOW, TextDecoration.ITALIC)));

            meta.lore(lore);
            saleItemDisplay.setItemMeta(meta);
        }

        // Diğer slotları doldurucu item ile doldur
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            placeholder.setItemMeta(placeholderMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (i != 13) { // Slot 13 (orta) hariç
                gui.setItem(i, placeholder.clone());
            }
        }

        gui.setItem(13, saleItemDisplay); // Ortadaki slotta satış için ürün

        player.openInventory(gui);
    }
}