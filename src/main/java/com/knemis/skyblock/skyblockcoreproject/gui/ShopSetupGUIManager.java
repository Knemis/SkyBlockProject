// com/knemis/skyblock/skyblockcoreproject/gui/ShopSetupGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // Direkt kullanılmıyor ama constructor'da var

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopSetupGUIManager {

    private final SkyBlockProject plugin;
    // private final ShopManager shopManager; // Direkt olarak bu sınıfta kullanılmıyor gibi, kaldırılabilir

    // GUI Başlıkları
    public static final Component SHOP_TYPE_TITLE = Component.text("Mağaza Türünü Belirle", Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
    public static final Component ITEM_SELECT_TITLE = Component.text("Satılacak Eşyayı Seç", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD));
    public static final Component QUANTITY_INPUT_TITLE = Component.text("Paket Miktarını Belirle", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD));

    private final ItemStack PLACEHOLDER_ITEM_GRAY;
    private final ItemStack PLACEHOLDER_ITEM_BLACK;

    public ShopSetupGUIManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        // this.shopManager = shopManager; // Eğer metodlar içinde shopManager'a erişim gerekmiyorsa kaldırılabilir

        PLACEHOLDER_ITEM_GRAY = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = PLACEHOLDER_ITEM_GRAY.getItemMeta();
        if (grayMeta != null) {
            grayMeta.displayName(Component.text(" "));
            PLACEHOLDER_ITEM_GRAY.setItemMeta(grayMeta);
        }

        PLACEHOLDER_ITEM_BLACK = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = PLACEHOLDER_ITEM_BLACK.getItemMeta();
        if (blackMeta != null) {
            blackMeta.displayName(Component.text(" "));
            PLACEHOLDER_ITEM_BLACK.setItemMeta(blackMeta);
        }
    }

    /**
     * GUI'nin arka planını (belirtilen slotlar hariç) yer tutucu item ile doldurur.
     * @param gui Doldurulacak envanter.
     * @param placeholder Kullanılacak yer tutucu item.
     * @param excludedSlots Doldurulmayacak slotların indeksleri.
     */
    private void fillGuiBackground(Inventory gui, ItemStack placeholder, Integer... excludedSlots) {
        List<Integer> excluded = Arrays.asList(excludedSlots);
        for (int i = 0; i < gui.getSize(); i++) {
            if (!excluded.contains(i) && gui.getItem(i) == null) { // Sadece boş ve hariç tutulmayan slotları doldur
                gui.setItem(i, placeholder.clone());
            }
        }
    }

    public void openShopTypeSelectionMenu(Player player, Location chestLocation) {
        Inventory gui = Bukkit.createInventory(null, 27, SHOP_TYPE_TITLE);

        // Alışveriş Sandığı Seçeneği
        ItemStack tradeChestItem = new ItemStack(Material.CHEST);
        ItemMeta tradeMeta = tradeChestItem.getItemMeta();
        if (tradeMeta != null) {
            tradeMeta.displayName(Component.text("Alışveriş Mağazası", NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> tradeLore = new ArrayList<>();
            tradeLore.add(Component.text("Oyuncular belirlediğiniz paketler halinde", NamedTextColor.YELLOW));
            tradeLore.add(Component.text("ürün satın alabilirler.", NamedTextColor.YELLOW));
            tradeLore.add(Component.text("Örn: 16 Taş / 10 Para", NamedTextColor.GRAY));
            tradeMeta.lore(tradeLore);
            tradeChestItem.setItemMeta(tradeMeta);
        }

        // Banka Sandığı Seçeneği
        ItemStack bankChestItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta bankMeta = bankChestItem.getItemMeta();
        if (bankMeta != null) {
            bankMeta.displayName(Component.text("Banka Mağazası", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
            List<Component> bankLore = new ArrayList<>();
            bankLore.add(Component.text("Oyuncular sandıktaki tüm ürünleri", NamedTextColor.YELLOW));
            bankLore.add(Component.text("(veya paraları yettiği kadarını)", NamedTextColor.YELLOW));
            bankLore.add(Component.text("tek seferde satın alabilir.", NamedTextColor.YELLOW));
            bankMeta.lore(bankLore);
            bankChestItem.setItemMeta(bankMeta);
        }

        fillGuiBackground(gui, PLACEHOLDER_ITEM_GRAY, 11, 15); // Belirli slotlar hariç doldur
        gui.setItem(11, tradeChestItem);
        gui.setItem(15, bankChestItem);

        plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
        player.openInventory(gui);
    }

    public void openItemSelectionMenu(Player player, Shop shop) {
        if (shop == null || shop.getShopType() == null) {
            player.sendMessage(ChatColor.RED + "Önce mağaza türü seçilmelidir!");
            player.closeInventory();
            plugin.getPlayerShopSetupState().remove(player.getUniqueId());
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 27, ITEM_SELECT_TITLE);

        // Bilgilendirme itemi
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Eşya Belirleme Adımı", NamedTextColor.YELLOW, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Mağazanızda satmak istediğiniz eşyanın", NamedTextColor.GRAY));
            lore.add(Component.text("bir örneğini (tüm özellikleriyle birlikte)", NamedTextColor.AQUA));
            lore.add(Component.text("aşağıdaki ortaya çıkan boş yuvaya yerleştirin.", NamedTextColor.GRAY));
            lore.add(Component.text("Bu eşya, mağazanızın şablonu olacaktır.", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Özel isim, büyü, lore gibi tüm detaylar", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("bu şablondan alınacaktır.", NamedTextColor.DARK_GRAY));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        // Slot 13 (orta slot) item koymak için boş bırakılacak.

        // Diğer slotları yer tutucu ile doldur
        fillGuiBackground(gui, PLACEHOLDER_ITEM_BLACK, 13, 4); // Slot 13 ve 4 hariç
        gui.setItem(4, infoItem); // Bilgi itemini slot 4'e koy (placeholder üzerine yazar)

        player.openInventory(gui);
    }

    public void openQuantityInputMenu(Player player, Shop shop) {
        if (shop == null || shop.getTemplateItemStack() == null || shop.getTemplateItemStack().getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Hata: Önce satılacak eşya şablonu belirlenmeli!");
            player.closeInventory();
            plugin.getPlayerShopSetupState().remove(player.getUniqueId());
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 36, QUANTITY_INPUT_TITLE); // 4 sıra

        // Seçilen template item'ı göster
        ItemStack displayTemplateItem = shop.getTemplateItemStack().clone();
        ItemMeta displayMeta = displayTemplateItem.getItemMeta();
        if (displayMeta != null) {
            List<Component> lore = displayMeta.hasLore() ? new ArrayList<>(displayMeta.lore()) : new ArrayList<>();
            if(lore == null) lore = new ArrayList<>(); // Null safety
            lore.add(0,Component.text(" "));
            lore.add(0,Component.text("Satılacak Eşya Türü:", NamedTextColor.BLUE));
            displayMeta.lore(lore);
            displayTemplateItem.setItemMeta(displayMeta);
        }
        gui.setItem(4, displayTemplateItem); // Sol üstte göster

        // Miktar için bilgilendirme itemi
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Paket Miktarı Belirleme", NamedTextColor.YELLOW, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Belirleyeceğiniz fiyata karşılık gelecek", NamedTextColor.GRAY));
            lore.add(Component.text("eşya miktarını aşağıdaki boş yuvaya koyun.", NamedTextColor.GRAY));
            lore.add(Component.text("Sadece ", NamedTextColor.GRAY)
                    .append(Component.text(shop.getTemplateItemStack().getType().toString(), NamedTextColor.AQUA))
                    .append(Component.text(" türünde eşya koyabilirsiniz.", NamedTextColor.GRAY)));
            lore.add(Component.text("Örn: 16 adet için bir fiyat belirleyecekseniz,", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("yuvaya 16 adet " + shop.getTemplateItemStack().getType().toString().toLowerCase().replace("_"," ") + " yerleştirin.", NamedTextColor.DARK_GRAY));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(13, infoItem); // Item koyma slotunun yanına bilgi

        // Onay Butonu
        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text("Miktarı Onayla ve Fiyat Belirle", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Bu miktarı onaylayarak bir sonraki", NamedTextColor.GRAY));
            lore.add(Component.text("adım olan fiyat belirlemeye geçin.", NamedTextColor.GRAY));
            confirmMeta.lore(lore);
            confirmItem.setItemMeta(confirmMeta);
        }
        gui.setItem(31, confirmItem); // Sağ alt köşe (36 slotluk envanterde)

        // Yer tutucular
        fillGuiBackground(gui, PLACEHOLDER_ITEM_BLACK, 4, 13, 22, 31); // Önemli slotlar hariç
        // Slot 22'yi miktar koyma yeri yapalım (ortadaki 3x3'ün merkezi)
        // Eski slot 13 bilgi oldu, slot 22 (eski slot 13) miktar slotu oldu.

        // Düzeltilmiş Slotlar:
        // Üst sıra: 0-8
        // İkinci sıra: 9-17 (Slot 13 bilgi itemi oldu)
        // Üçüncü sıra: 18-26 (Slot 22 miktar girme slotu olacak)
        // Dördüncü sıra: 27-35 (Slot 31 onay butonu)

        // Yeni düzen:
        gui.setItem(4, displayTemplateItem); // Satılacak itemi göster
        // Slot 13 (eski bilgi itemi yeri) artık miktar girme slotu olacak.
        fillGuiBackground(gui, PLACEHOLDER_ITEM_BLACK, 4, 13, 31);
        gui.setItem(31, confirmItem);


        player.openInventory(gui);
    }

    public void promptForPrice(Player player, Shop shop) {
        if (shop.getTemplateItemStack() == null || shop.getTemplateItemStack().getType() == Material.AIR || shop.getItemQuantityForPrice() <= 0) {
            player.sendMessage(ChatColor.RED + "Hata: Önce satılacak eşya ve paket miktarı doğru şekilde belirlenmeli!");
            player.closeInventory();
            plugin.getPlayerShopSetupState().remove(player.getUniqueId());
            return;
        }
        player.closeInventory(); // Miktar GUI'sini kapat

        String itemName = shop.getTemplateItemStack().hasItemMeta() && shop.getTemplateItemStack().getItemMeta().hasDisplayName() ?
                LegacyComponentSerializer.legacySection().serialize(shop.getTemplateItemStack().getItemMeta().displayName()) :
                shop.getTemplateItemStack().getType().toString().toLowerCase().replace("_", " ");

        player.sendMessage(ChatColor.GOLD + "===== Fiyat Belirleme =====");
        player.sendMessage(ChatColor.YELLOW + "Satılacak Eşya: " + ChatColor.AQUA + itemName);
        player.sendMessage(ChatColor.YELLOW + "Paket Miktarı: " + ChatColor.AQUA + shop.getItemQuantityForPrice() + " adet");
        player.sendMessage(ChatColor.GREEN + "Lütfen bu paket için belirlemek istediğiniz toplam fiyatı chat'e girin.");
        player.sendMessage(ChatColor.GRAY + "Örnek: " + ChatColor.WHITE + "100.50" + ChatColor.GRAY + " veya " + ChatColor.WHITE + "50");
        player.sendMessage(ChatColor.GRAY + "İptal etmek için '" + ChatColor.RED + "iptal" + ChatColor.GRAY + "' yazın.");
        // playerShopSetupState zaten ShopListener veya önceki GUI açılışında ayarlanmıştı,
        // fiyat girildiğinde bu state'e göre işlem yapılacak.
    }
}