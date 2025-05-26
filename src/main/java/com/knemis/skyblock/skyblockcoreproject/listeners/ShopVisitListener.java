// com/knemis/skyblock/skyblockcoreproject/listeners/ShopVisitListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Kullanıcının sağladığı
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import net.milkbowl.vault.economy.Economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Level;

public class ShopVisitListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopVisitGUIManager shopVisitGUIManager; // GUI'yi yeniden açmak için

    public ShopVisitListener(SkyBlockProject plugin, ShopManager shopManager, ShopVisitGUIManager shopVisitGUIManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopVisitGUIManager = shopVisitGUIManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player buyer = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();

        if (topInventory == null || !event.getView().title().equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();

        // Oyuncunun kendi envanterine tıklamasına izin ver
        if (clickedInventory != null && clickedInventory.equals(buyer.getOpenInventory().getBottomInventory())) {
            event.setCancelled(false);
            return;
        }

        // GUI içindeki diğer tüm varsayılan item hareketlerini engelle
        event.setCancelled(true);

        ItemStack clickedItemRepresentation = event.getCurrentItem();
        if (clickedItemRepresentation == null || clickedItemRepresentation.getType() == Material.AIR) {
            return;
        }

        // ShopVisitGUIManager slot 13'te item gösteriyor
        if (event.getRawSlot() != 13) {
            buyer.sendMessage(ChatColor.YELLOW + "Satın almak için lütfen mağaza ürününe tıklayın.");
            return;
        }

        Location shopLocation = plugin.getPlayerViewingShopLocation().get(buyer.getUniqueId());
        if (shopLocation == null) {
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "HATA: " + ChatColor.RESET + ChatColor.RED + "Mağaza bilgisi bulunamadı. Lütfen mağazayı kapatıp tekrar açın.");
            buyer.closeInventory();
            return;
        }

        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.isSetupComplete() || shop.getTemplateItemStack() == null) {
            buyer.sendMessage(ChatColor.RED + "Bu mağaza şu anda mevcut değil veya henüz tam olarak ayarlanmamış.");
            buyer.closeInventory();
            plugin.getPlayerViewingShopLocation().remove(buyer.getUniqueId());
            return;
        }

        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(ChatColor.YELLOW + "Kendi mağazanızdan ürün satın alamazsınız.");
            return;
        }

        ItemStack templateItemFromShop = shop.getTemplateItemStack();
        int itemsPerBundle = shop.getItemQuantityForPrice();
        double bundlePrice = shop.getPrice();
        String formattedItemName = getItemNameForMessages(templateItemFromShop);
        String currencyName = getCurrencyName();

        if (itemsPerBundle <= 0 || bundlePrice < 0) { // Ücretsiz itemler için fiyat 0 olabilir
            buyer.sendMessage(ChatColor.RED + "Mağaza ayarlarında bir sorun var (Fiyat/Miktar). Lütfen mağaza sahibine bildirin.");
            plugin.getLogger().warning("Geçersiz mağaza ayarı: " + Shop.locationToString(shopLocation) + " Fiyat: " + bundlePrice + " Miktar: " + itemsPerBundle);
            return;
        }

        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi şu anda aktif değil.");
            return;
        }

        if (EconomyManager.getBalance(buyer) < bundlePrice) {
            buyer.sendMessage(ChatColor.RED + "Yetersiz bakiye! " +
                    ChatColor.YELLOW + "Gereken: " + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName +
                    ChatColor.RED + ", Mevcut: " + ChatColor.GOLD + String.format("%.2f", EconomyManager.getBalance(buyer)) + " " + currencyName);
            return;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Kritik Hata: Mağaza sandığı artık mevcut değil! İşlem iptal edildi.");
            plugin.getLogger().severe("Mağaza sandığı (" + Shop.locationToString(shopLocation) + ") artık Chest değil: " + shopBlock.getType());
            closeShopForPlayer(buyer);
            return;
        }
        Chest chest = (Chest) shopBlock.getState();

        if (shopManager.countItemsInChest(chest, templateItemFromShop) < itemsPerBundle) {
            buyer.sendMessage(ChatColor.GOLD + "Üzgünüz, mağazada bu üründen yeterli stok kalmamış!");
            buyer.sendMessage(ChatColor.GRAY + "(" + itemsPerBundle + " adet " + formattedItemName + " gerekliydi)");
            shopManager.updateAttachedSign(shop); // Stok bitti, tabelayı güncelle
            this.shopVisitGUIManager.openShopVisitMenu(buyer, shop); // GUI'yi güncel stokla yeniden aç
            return;
        }

        ItemStack itemsToReceive = templateItemFromShop.clone();
        itemsToReceive.setAmount(itemsPerBundle);
        if (!shopManager.hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + itemsPerBundle + " adet " + formattedItemName + " için yeterli yer yok!");
            return;
        }

        // ---- SATIN ALMA İŞLEMİ ----
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, bundlePrice)) {
            buyer.sendMessage(ChatColor.RED + "Para çekme işlemi başarısız oldu. Lütfen tekrar deneyin.");
            plugin.getLogger().warning("[ShopPurchase] Para çekme başarısız: Alıcı=" + buyer.getName() + ", Miktar=" + bundlePrice + ", Mağaza=" + Shop.locationToString(shopLocation));
            return;
        }

        if (!EconomyManager.deposit(owner, bundlePrice)) {
            EconomyManager.deposit(buyer, bundlePrice); // Parayı alıcıya iade et!
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Satıcıya para aktarılamadı. Paranız iade edildi. Lütfen durumu yetkililere bildirin!");
            plugin.getLogger().log(Level.SEVERE, "[ShopPurchase] KRİTİK: Sahip " + owner.getName() + " (" + shop.getOwnerUUID() + ") hesabına para yatırılamadı! " +
                    buyer.getName() + " adlı oyuncudan çekilen " + bundlePrice + " " + currencyName + " iade edildi. Mağaza: " + Shop.locationToString(shopLocation));
            closeShopForPlayer(buyer);
            return;
        }

        if (!shopManager.removeItemsFromChest(chest, templateItemFromShop, itemsPerBundle)) {
            EconomyManager.withdraw(owner, bundlePrice); // Satıcıdan parayı geri al
            EconomyManager.deposit(buyer, bundlePrice);  // Alıcıya parayı iade et
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Eşyalar mağazadan alınırken bir sorun oluştu. Paranız iade edildi. Lütfen durumu yetkililere bildirin!");
            plugin.getLogger().log(Level.SEVERE, "[ShopPurchase] KRİTİK: Sandıktan (" + Shop.locationToString(shopLocation) + ") " + itemsPerBundle + " adet " + formattedItemName +
                    " çekilemedi! Para transferleri geri alındı.");
            closeShopForPlayer(buyer);
            return;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(itemsPerBundle, bundlePrice);
        plugin.getShopManager().getShopStorage().saveShop(shop); // Mağaza istatistiklerini ve son aktiviteyi kaydet

        buyer.playSound(buyer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + itemsPerBundle + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName +
                ChatColor.GREEN + " satın aldınız (" + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName + ChatColor.GREEN + ").");

        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " mağazanızdan " +
                    ChatColor.AQUA + itemsPerBundle + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName +
                    ChatColor.YELLOW + " satın aldı (" + ChatColor.GOLD + String.format("%.2f", bundlePrice) + " " + currencyName + ChatColor.YELLOW + ").");
            owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);
        }
        plugin.getLogger().info("[SHOP TRANSACTION] " + buyer.getName() + " bought " + itemsPerBundle + " of [" + formattedItemName + "] for " + bundlePrice +
                " from shop at " + Shop.locationToString(shopLocation) + " (Owner: " + shop.getOwnerUUID() + ")");

        shopManager.updateAttachedSign(shop);
        closeShopForPlayer(buyer); // İşlem tamamlandı, GUI'yi kapat ve state'i temizle
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        // Eğer bu bizim mağaza ziyaret GUI'miz ise ve oyuncu state'i hala varsa, temizle
        if (event.getView().title().equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
            if (plugin.getPlayerViewingShopLocation().containsKey(player.getUniqueId())) {
                plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
                // plugin.getLogger().fine(player.getName() + " için mağaza görüntüleme state'i temizlendi (GUI Kapatıldı).");
            }
        }
    }

    private String getItemNameForMessages(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            try {
                return LegacyComponentSerializer.legacySection().serialize(itemStack.getItemMeta().displayName());
            } catch (Exception e) {
                return ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()); // Fallback
            }
        }
        return itemStack.getType().toString().toLowerCase().replace("_", " ");
    }

    private String getCurrencyName() {
        Economy econ = plugin.getEconomy();
        if (econ != null && econ.currencyNamePlural() != null && !econ.currencyNamePlural().isEmpty()) {
            return econ.currencyNamePlural();
        }
        return "$"; // Varsayılan
    }

    private void closeShopForPlayer(Player player) {
        player.closeInventory();
        plugin.getPlayerViewingShopLocation().remove(player.getUniqueId());
    }
}