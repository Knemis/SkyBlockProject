// com/knemis/skyblock/skyblockcoreproject/gui/ShopSetupGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // ShopManager'a erişim için
// ShopType import'u burada doğrudan gerekmeyebilir, çünkü Shop nesnesi üzerinden geliyor.

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor; // Hala bazı mesajlarda kullanılabilir, isteğe bağlı
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class ShopSetupGUIManager {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;

    // GUI Başlıkları (Component olarak)
    public static final Component SHOP_TYPE_TITLE = Component.text("Mağaza Türü Seçin", Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
    public static final Component ITEM_SELECT_TITLE = Component.text("Satılacak Eşyayı Koyun", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD));
    public static final Component QUANTITY_INPUT_TITLE = Component.text("Birim Miktarı Belirleyin", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD));

    public ShopSetupGUIManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }
    private void fillGuiBackground(Inventory gui, ItemStack placeholder) {
        if (placeholder == null) {
            placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(" ")); // İsimsiz
                placeholder.setItemMeta(meta);
            }
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) { // Sadece boş slotları doldur
                gui.setItem(i, placeholder);
            }
        }
    }

    /**
     * Mağaza türü seçimi için ilk GUI'yi açar.
     * @param player GUI'yi açacak oyuncu.
     * @param chestLocation Mağazanın kurulacağı sandığın konumu.
     */
    public void openShopTypeSelectionMenu(Player player, Location chestLocation) {
        Inventory gui = Bukkit.createInventory(null, 27, SHOP_TYPE_TITLE);

        ItemStack tradeChestItem = new ItemStack(Material.CHEST);
        ItemMeta tradeMeta = tradeChestItem.getItemMeta();
        if (tradeMeta != null) {
            tradeMeta.displayName(Component.text("Alışveriş Sandığı", NamedTextColor.GOLD));
            tradeMeta.lore(Collections.singletonList(Component.text("Oyuncular belirli miktarda eşya alır.", NamedTextColor.GRAY)));
            tradeChestItem.setItemMeta(tradeMeta);
        }

        ItemStack bankChestItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta bankMeta = bankChestItem.getItemMeta();
        if (bankMeta != null) {
            bankMeta.displayName(Component.text("Banka Sandığı", NamedTextColor.LIGHT_PURPLE));
            bankMeta.lore(Collections.singletonList(Component.text("Oyuncular tüm stoğu veya yettiği kadar alır.", NamedTextColor.GRAY)));
            bankChestItem.setItemMeta(bankMeta);
        }

        gui.setItem(11, tradeChestItem); // Ortaya yakın
        gui.setItem(15, bankChestItem); // Ortaya yakın

        // Kurulumu devam eden mağazanın konumunu oyuncu state'ine kaydet
        plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
        player.openInventory(gui);
    }

    /**
     * Satılacak eşyanın seçileceği GUI'yi açar.
     * @param player Oyuncu.
     * @param shop Kurulumu devam eden Shop nesnesi.
     */
    public void openItemSelectionMenu(Player player, Shop shop) {
        Inventory gui = Bukkit.createInventory(null, 27, ITEM_SELECT_TITLE); // 3x9'luk GUI

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Bilgi", NamedTextColor.YELLOW));
            infoMeta.lore(Arrays.asList(
                    Component.text("Satmak istediğiniz eşyadan ", NamedTextColor.GRAY)
                            .append(Component.text("bir adet", NamedTextColor.AQUA)),
                    Component.text("aşağıdaki boş slota yerleştirin.", NamedTextColor.GRAY),
                    Component.text("Bu eşyanın türü mağazanız için kullanılacaktır.", NamedTextColor.DARK_GRAY)
            ));
            infoItem.setItemMeta(infoMeta);
        }
        // Slot 13 (orta slot) item koymak için boş olacak.
        // Bilgi itemini farklı bir yere koyalım, örn: slot 4
        gui.setItem(4, infoItem);

        // Geri kalan slotları placeholder ile doldur
        fillGuiBackground(gui, null); // Slot 13 ve 4 hariç dolacak (çünkü onlar önceden ayarlandı veya boş bırakıldı)
        // fillGuiBackground önce çağrılıp sonra itemler set edilirse daha doğru olur.

        // Düzeltilmiş sıralama:
        Inventory cleanGui = Bukkit.createInventory(null, 27, ITEM_SELECT_TITLE);
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            placeholder.setItemMeta(placeholderMeta);
        }
        for (int i = 0; i < cleanGui.getSize(); i++) {
            if (i != 13) { // Slot 13 hariç her yeri doldur
                cleanGui.setItem(i, placeholder.clone());
            }
        }
        cleanGui.setItem(4, infoItem); // Bilgi itemini placeholder üzerine yaz

        player.openInventory(cleanGui);
    }


    /**
     * Fiyatlandırma için birim miktarın belirleneceği GUI'yi açar.
     * @param player Oyuncu.
     * @param shop Kurulumu devam eden Shop nesnesi (itemType ayarlanmış olmalı).
     */
    public void openQuantityInputMenu(Player player, Shop shop) {
        if (shop.getItemType() == null) {
            player.sendMessage(ChatColor.RED + "Önce satılacak eşya türü belirlenmeli!");
            player.closeInventory();
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 27, QUANTITY_INPUT_TITLE);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Miktar Belirleme", NamedTextColor.YELLOW));
            infoMeta.lore(Arrays.asList(
                    Component.text("Belirleyeceğiniz fiyata karşılık gelecek", NamedTextColor.GRAY),
                    Component.text("eşya miktarını aşağıdaki boş slota koyun.", NamedTextColor.GRAY),
                    Component.text("Sadece ", NamedTextColor.GRAY)
                            .append(Component.text(shop.getItemType().toString(), NamedTextColor.AQUA))
                            .append(Component.text(" türünde eşya koyabilirsiniz.", NamedTextColor.GRAY))
            ));
            infoItem.setItemMeta(infoMeta);
        }

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text("Miktarı Onayla", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)));
            confirmItem.setItemMeta(confirmMeta);
        }

        // Placeholder ile doldurma
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE); // Farklı bir renk
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        if (placeholderMeta != null) {
            placeholderMeta.displayName(Component.text(" "));
            placeholder.setItemMeta(placeholderMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (i != 13 && i != 4 && i != 22) { // Aktif slotlar hariç
                gui.setItem(i, placeholder.clone());
            }
        }

        gui.setItem(4, infoItem);     // Bilgi itemi (placeholder üzerine yazar)
        // Slot 13 (orta) miktar için boş bırakılacak
        gui.setItem(22, confirmItem); // Onay butonu (placeholder üzerine yazar)

        player.openInventory(gui);
    }

    /**
     * Fiyat girişi için oyuncuyu chat'e yönlendirir.
     * @param player Oyuncu.
     * @param shop Kurulumu devam eden Shop nesnesi (itemType ve quantity ayarlanmış olmalı).
     */
    public void promptForPrice(Player player, Shop shop) {
        if (shop.getItemType() == null || shop.getItemQuantityForPrice() <= 0) {
            player.sendMessage(ChatColor.RED + "Önce eşya türü ve miktarı belirlenmeli!");
            player.closeInventory(); // GUI'yi kapat
            plugin.getPlayerShopSetupState().remove(player.getUniqueId()); // Hatalı durumda state'i temizle
            return;
        }
        player.closeInventory(); // Miktar GUI'sini kapat
        player.sendMessage(ChatColor.YELLOW + "Şimdi bu " + ChatColor.AQUA + shop.getItemQuantityForPrice() + " adet " +
                ChatColor.LIGHT_PURPLE + shop.getItemType().toString().replace("_", " ").toLowerCase() +
                ChatColor.YELLOW + " için belirlemek istediğiniz toplam fiyatı chat'e girin.");
        player.sendMessage(ChatColor.GRAY + "(Örn: 10.5 veya 100 yazın. İptal için 'iptal' yazabilirsiniz.)");
        // Oyuncunun chat'e fiyat girmesi bekleniyor, ShopSetupListener bunu yakalayacak.
        // playerShopSetupState zaten ShopListener veya ShopSetupGUIManager.openShopTypeSelectionMenu içinde ayarlanmış olmalı.
    }
}