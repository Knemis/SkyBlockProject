// com/knemis/skyblock/skyblockcoreproject/listeners/ShopVisitListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Level;

public class ShopVisitListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    // ShopVisitGUIManager'a doğrudan bağımlılık GUI başlığını kontrol etmek için gerekli.

    public ShopVisitListener(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player buyer = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        // Tıklanan envanterin bizim GUI'miz olup olmadığını ve başlığın eşleşip eşleşmediğini kontrol et
        if (topInventory == null || !event.getView().title().equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
            return;
        }

        // Oyuncunun kendi envanterine tıklamasına izin ver, GUI etkileşimlerini iptal et
        if (clickedInventory == null || !clickedInventory.equals(topInventory)) {
            // Eğer kendi envanterine tıklıyorsa (örn: itemlerini düzenlemek için), iptal etme
            if (clickedInventory != null && clickedInventory.equals(buyer.getOpenInventory().getBottomInventory())) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true); // GUI dışı veya bilinmeyen tıklamalar
            }
            return;
        }

        event.setCancelled(true); // GUI içindeki varsayılan item hareketlerini engelle

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }

        // Mağazanın GUI'sinde genellikle ortadaki slot (13) satın alma itemini içerir.
        // ShopVisitGUIManager'daki yapıya göre bu slot numarasını doğrula.
        if (event.getRawSlot() != 13) { // Sadece slot 13'teki iteme tıklanırsa devam et
            return;
        }

        // Oyuncunun hangi mağazaya baktığını SkyBlockProject'teki map'ten al
        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyer.getUniqueId());
        if (shopLocation == null) {
            buyer.sendMessage(ChatColor.RED + "Hangi mağazadan alışveriş yaptığınız anlaşılamadı. Lütfen mağazayı tekrar açın.");
            buyer.closeInventory();
            return;
        }

        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.isSetupComplete()) {
            buyer.sendMessage(ChatColor.RED + "Bu mağaza artık mevcut değil veya kurulumu tamamlanmamış.");
            buyer.closeInventory();
            plugin.getPlayerViewingShopLocation().remove(buyer.getUniqueId()); // Hatalı durumu temizle
            return;
        }

        // Mağaza sahibi kendi mağazasından alım yapamaz
        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(ChatColor.YELLOW + "Kendi mağazanızdan ürün satın alamazsınız.");
            // GUI açık kalabilir veya kapatılabilir.
            return;
        }

        // Satın alınacak ürün ve fiyat bilgileri
        int itemsPerBundle = shop.getItemQuantityForPrice();
        double bundlePrice = shop.getPrice();
        Material itemType = shop.getItemType();
        String formattedItemName = itemType.toString().toLowerCase().replace("_", " ");
        String currencyName = plugin.getEconomy() != null && plugin.getEconomy().currencyNamePlural() != null && !plugin.getEconomy().currencyNamePlural().isEmpty() ? plugin.getEconomy().currencyNamePlural() : "Para";


        if (itemsPerBundle <= 0 || bundlePrice < 0) { // Fiyat 0 olabilir (ücretsiz item)
            buyer.sendMessage(ChatColor.RED + "Mağaza fiyat veya miktar ayarlarında bir sorun var. Lütfen mağaza sahibine bildirin.");
            plugin.getLogger().warning("Geçersiz mağaza ayarı (fiyat/miktar <=0): " + Shop.locationToString(shopLocation) + " Fiyat: " + bundlePrice + " Miktar: " + itemsPerBundle);
            return;
        }

        // Ekonomi Sistemi Kontrolü
        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi şu anda aktif değil. Satın alma işlemi yapılamıyor.");
            plugin.getLogger().severe("ShopVisitListener: Ekonomi sistemi (Vault/EconomyManager) bulunamadı!");
            return;
        }

        // Bakiye Kontrolü
        if (EconomyManager.getBalance(buyer) < bundlePrice) {
            buyer.sendMessage(ChatColor.RED + "Yeterli paranız yok. " +
                    ChatColor.YELLOW + "Gereken: " + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName +
                    ChatColor.RED + ", Sizin: " + ChatColor.GOLD + String.format("%.2f", EconomyManager.getBalance(buyer)) + " " + currencyName);
            return;
        }

        // Mağaza Sandığı ve Stok Kontrolü
        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Mağaza sandığı bulunamadı veya türü değişmiş! Satın alma iptal edildi.");
            plugin.getLogger().warning("Mağaza sandığı ("+Shop.locationToString(shopLocation)+") artık Chest değil: " + shopBlock.getType());
            return;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (shopManager.countItemsInChest(chest, itemType) < itemsPerBundle) {
            buyer.sendMessage(ChatColor.RED + "Mağazada yeterli stok kalmamış! (" + itemsPerBundle + " adet " + formattedItemName + " gerekli)");
            shopManager.updateShopSign(shop); // Stok bitti, tabelayı güncelle
            // GUI'yi yeniden açarak güncel stokla göstermek iyi olabilir
            // Veya ShopVisitGUIManager'daki GUI'yi dinamik olarak güncellemek
            this.plugin.getShopVisitGUIManager().openShopVisitMenu(buyer, shop); // GUI'yi yenile
            return;
        }

        // Oyuncunun Envanterinde Yer Kontrolü
        ItemStack itemsToReceive = new ItemStack(itemType, itemsPerBundle);
        if (!shopManager.hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + itemsPerBundle + " adet " + formattedItemName + " için yeterli yer yok!");
            return;
        }

        // --- Tüm Kontrollerden Geçti, İşlemleri Başlat ---

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        // 1. Alıcıdan Para Çekme
        if (!EconomyManager.withdraw(buyer, bundlePrice)) {
            buyer.sendMessage(ChatColor.RED + "Para çekme işlemi sırasında bir hata oluştu. Lütfen tekrar deneyin.");
            plugin.getLogger().warning("Para çekme başarısız: " + buyer.getName() + ", Miktar: " + bundlePrice + " Mağaza: " + Shop.locationToString(shopLocation));
            return;
        }

        // 2. Satıcıya Para Yatırma
        if (!EconomyManager.deposit(owner, bundlePrice)) {
            EconomyManager.deposit(buyer, bundlePrice); // Parayı alıcıya iade et
            buyer.sendMessage(ChatColor.RED + "Satıcıya para aktarılırken bir sorun oluştu. Paranız iade edildi. Lütfen durumu bir yetkiliye bildirin.");
            plugin.getLogger().log(Level.SEVERE, "KRİTİK HATA: Mağaza sahibi " + owner.getName() + " (" + shop.getOwnerUUID() + ") hesabına para yatırılamadı! " +
                    buyer.getName() + " adlı oyuncudan çekilen " + bundlePrice + " " + currencyName + " iade edildi. Mağaza: " + Shop.locationToString(shopLocation));
            return;
        }

        // 3. Sandıktan Eşyaları Çekme
        if (!shopManager.removeItemsFromChest(chest, itemType, itemsPerBundle)) {
            EconomyManager.withdraw(owner, bundlePrice); // Satıcıdan parayı geri al
            EconomyManager.deposit(buyer, bundlePrice);  // Alıcıya parayı iade et
            buyer.sendMessage(ChatColor.RED + "Mağaza sandığından eşyalar alınırken bir sorun oluştu. Paranız iade edildi. Lütfen durumu bir yetkiliye bildirin.");
            plugin.getLogger().log(Level.SEVERE, "KRİTİK HATA: Mağaza sandığından (" + Shop.locationToString(shopLocation) + ") " + itemsPerBundle + " adet " + itemType +
                    " çekilemedi! Para transferleri geri alındı.");
            return;
        }

        // 4. Alıcıya Eşyaları Verme
        buyer.getInventory().addItem(itemsToReceive.clone());

        // 5. Başarı Mesajları ve Loglama
        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + itemsPerBundle + " adet " + formattedItemName +
                " satın aldınız (" + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName + ChatColor.GREEN + ").");

        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " mağazanızdan " +
                    ChatColor.AQUA + itemsPerBundle + " adet " + formattedItemName +
                    ChatColor.YELLOW + " satın aldı (" + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName + ChatColor.YELLOW + ").");
        }
        plugin.getLogger().info(buyer.getName() + " bought " + itemsPerBundle + " of " + itemType + " for " + bundlePrice +
                " from shop " + Shop.locationToString(shopLocation) + " (Owner: " + shop.getOwnerUUID() + ")");

        // 6. Tabela Güncelleme
        shopManager.updateShopSign(shop);

        // 7. GUI Kapatma ve State Temizleme
        buyer.closeInventory(); // Başarılı işlem sonrası GUI'yi kapat
        plugin.getPlayerViewingShopLocation().remove(buyer.getUniqueId());

        // İsteğe bağlı: GUI'yi güncel stokla yeniden açmak yerine kapatıyoruz.
        // Eğer açık kalması ve güncellenmesi isteniyorsa, closeInventory yerine
        // ShopVisitGUIManager üzerinden GUI'yi yeniden açma metodu çağrılabilir.
    }
}