package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // Yeni eklendi
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent; // InventoryCloseEvent için import
import org.bukkit.event.inventory.InventoryCloseEvent;  // Yeni eklendi
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack; // ItemStack importu eklendi

public class ShopListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;
    private final IslandDataHandler islandDataHandler;
    private final ShopVisitGUIManager shopVisitGUIManager;
    private final ShopAdminGUIManager shopAdminGUIManager; // Yeni eklendi

    public ShopListener(SkyBlockProject plugin,
                        ShopManager shopManager,
                        ShopSetupGUIManager shopSetupGUIManager,
                        IslandDataHandler islandDataHandler,
                        ShopVisitGUIManager shopVisitGUIManager,
                        ShopAdminGUIManager shopAdminGUIManager) { // Constructor'a eklendi
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
        this.islandDataHandler = islandDataHandler;
        this.shopVisitGUIManager = shopVisitGUIManager;
        this.shopAdminGUIManager = shopAdminGUIManager; // Atama yapıldı
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || (clickedBlock.getType() != Material.CHEST && clickedBlock.getType() != Material.TRAPPED_CHEST)) {
            return;
        }

        Player player = event.getPlayer();
        Location chestLocation = clickedBlock.getLocation();

        if (player.isSneaking()) { // Shift + Sağ Tık
            event.setCancelled(true);

            Island islandAtLocation = islandDataHandler.getIslandAt(chestLocation);
            boolean canManageHere = false;

            if (islandAtLocation != null) {
                if (islandAtLocation.getOwnerUUID().equals(player.getUniqueId()) || islandAtLocation.isMember(player.getUniqueId())) {
                    canManageHere = true;
                }
            }
            if (player.hasPermission("skyblock.admin.createshopanywhere")) {
                canManageHere = true;
            }

            if (!canManageHere) {
                player.sendMessage(ChatColor.RED + "Burada mağaza kurma veya yönetme yetkiniz yok.");
                return;
            }

            Shop existingActiveShop = shopManager.getActiveShop(chestLocation);
            Shop existingPendingShop = shopManager.getPendingShop(chestLocation);

            if (existingActiveShop != null) {
                if (existingActiveShop.getOwnerUUID().equals(player.getUniqueId())) {
                    // Aktif ve kendi dükkanı: Yönetim menüsünü aç
                    this.shopAdminGUIManager.openAdminMenu(player, existingActiveShop);
                } else {
                    player.sendMessage(ChatColor.RED + "Bu mağaza başkasına ait, yönetemezsiniz.");
                }
            } else if (existingPendingShop != null) {
                if (existingPendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Bu mağazanın kurulumu devam ediyor. Lütfen adımları takip edin.");
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation);
                } else {
                    player.sendMessage(ChatColor.RED + "Bu sandık başkası tarafından mağaza olarak kuruluyor.");
                }
            } else { // Yeni mağaza kurulumu
                Shop newShopProvisional = shopManager.initiateShopCreation(chestLocation, player, null);
                if (newShopProvisional != null) {
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation);
                    player.sendMessage(ChatColor.GREEN + "Mağaza kurulumu başlatıldı. Lütfen mağaza türünü seçin.");
                } else {
                    player.sendMessage(ChatColor.RED + "Mağaza kurulumu başlatılamadı. Bu konumda zaten bir mağaza olabilir.");
                }
            }
        } else { // Normal Sağ Tık
            Shop activeShop = shopManager.getActiveShop(chestLocation);

            if (activeShop != null && activeShop.isSetupComplete()) {
                event.setCancelled(true);
                if (!activeShop.getOwnerUUID().equals(player.getUniqueId())) {
                    plugin.getPlayerViewingShopLocation().put(player.getUniqueId(), chestLocation);
                    shopVisitGUIManager.openShopVisitMenu(player, activeShop);
                } else {
                    event.setCancelled(false); // Kendi dükkanı, sandığı açsın
                }
            } else {
                Shop pendingShop = shopManager.getPendingShop(chestLocation);
                if (pendingShop != null && pendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation);
                    player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumuna devam ediyorsunuz...");
                }
                // else: Mağaza değilse veya başkasının kurulumdaki mağazasıysa, normal sandık davranışına izin ver.
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        // Önceki GUI başlık kontrolleriniz burada kalabilir (ShopSetupGUIManager vs.)
        // ...

        // Yeni Shop Admin GUI tıklama yönetimi
        if (event.getView().title().equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Location shopLocation = plugin.getPlayerAdministeringShop().get(player.getUniqueId());
            if (shopLocation == null) {
                player.sendMessage(ChatColor.RED + "Hata: Yönetilecek mağaza bilgisi bulunamadı.");
                player.closeInventory();
                return;
            }
            Shop shop = shopManager.getActiveShop(shopLocation);
            if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Hata: Mağaza bulunamadı veya bu mağazayı yönetme yetkiniz yok.");
                player.closeInventory();
                plugin.getPlayerAdministeringShop().remove(player.getUniqueId()); // State'i temizle
                return;
            }

            // ShopAdminGUIManager'daki slot numaralarına göre kontrol
            // Bu slot numaralarını ShopAdminGUIManager sınıfından statik olarak almak daha iyi olurdu.
            // Şimdilik varsayılan değerleri (11 ve 13) kullanalım.
            // Gerçek slotları ShopAdminGUIManager.DISPLAY_NAME_SLOT ve ShopAdminGUIManager.PRICE_SLOT ile eşleştirin.
            int displayNameSlot = 11; // ShopAdminGUIManager'daki DISPLAY_NAME_SLOT ile eşleşmeli
            int priceSlot = 13;       // ShopAdminGUIManager'daki PRICE_SLOT ile eşleşmeli

            if (event.getRawSlot() == displayNameSlot) {
                shopAdminGUIManager.initiateDisplayNameChange(player, shop);
            } else if (event.getRawSlot() == priceSlot) {
                shopAdminGUIManager.initiatePriceChange(player, shop);
            }
            // Gelecekte eklenecek diğer yönetim butonları için buraya else if blokları eklenebilir.
        }
        // Diğer GUI'ler için tıklama yönetimi (ShopSetupListener vb. kendi event handler'larında yönetiyor olmalı)
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        // ShopVisitGUI kapatıldığında state'i temizle
        if (event.getView().title().equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
            if (plugin.getPlayerViewingShopLocation().containsKey(playerId)) {
                plugin.getPlayerViewingShopLocation().remove(playerId);
            }
        }
        // ShopAdminGUI kapatıldığında ilgili state'leri temizle
        else if (event.getView().title().equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
            plugin.getPlayerAdministeringShop().remove(playerId);
            // Eğer oyuncu chat'e bir şey yazmak üzereyken GUI'yi kapattıysa,
            // beklenen input durumunu da temizle ve bir mesaj gönder.
            ShopAdminGUIManager.AdminInputType expectedInput = plugin.getPlayerWaitingForAdminInput().remove(playerId);
            if (expectedInput != null) {
                player.sendMessage(ChatColor.YELLOW + "Mağaza ayarı girişi iptal edildi.");
            }
        }
        // ShopSetupGUI'ler için state temizliği ShopSetupListener içinde yapılmalı.
    }
}