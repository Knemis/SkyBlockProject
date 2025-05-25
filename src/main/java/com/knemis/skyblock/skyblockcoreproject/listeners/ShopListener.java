// com/knemis/skyblock/skyblockcoreproject/listeners/ShopListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager; // Kullanıcının sağladığı
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
// EconomyManager importu burada doğrudan gerekmeyebilir, satın alma işlemi ShopVisitListener'da.

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShopListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;
    private final IslandDataHandler islandDataHandler;
    private final ShopVisitGUIManager shopVisitGUIManager; // Kullanıcının sağladığı GUI Manager

    public ShopListener(SkyBlockProject plugin,
                        ShopManager shopManager,
                        ShopSetupGUIManager shopSetupGUIManager,
                        IslandDataHandler islandDataHandler,
                        ShopVisitGUIManager shopVisitGUIManager) { // Constructor'a eklendi
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
        this.islandDataHandler = islandDataHandler;
        this.shopVisitGUIManager = shopVisitGUIManager; // Atama yapıldı
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { // Sadece bloğa sağ tıklama
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || (clickedBlock.getType() != Material.CHEST && clickedBlock.getType() != Material.TRAPPED_CHEST)) {
            return; // Sadece normal veya tuzaklı sandıklar
        }

        Player player = event.getPlayer();
        Location chestLocation = clickedBlock.getLocation();

        // Shift + Sağ Tık: Mağaza Kurulum/Yönetim İşlemleri
        if (player.isSneaking()) {
            event.setCancelled(true); // Varsayılan sandık açılmasını her zaman engelle (Shift+SağTık için)

            Island islandAtLocation = islandDataHandler.getIslandAt(chestLocation);
            boolean canManageHere = false;

            if (islandAtLocation != null) {
                if (islandAtLocation.getOwnerUUID().equals(player.getUniqueId()) || islandAtLocation.isMember(player.getUniqueId())) {
                    canManageHere = true;
                }
            }
            // Adminler her yerde mağaza kurabilir/yönetebilir (opsiyonel bir izin)
            if (player.hasPermission("skyblock.admin.createshopanywhere")) {
                canManageHere = true;
            }

            if (!canManageHere) {
                player.sendMessage(ChatColor.RED + "Burada mağaza kurma veya yönetme yetkiniz yok.");
                return;
            }

            // Bu konumda zaten bir mağaza var mı?
            Shop existingActiveShop = shopManager.getActiveShop(chestLocation);
            Shop existingPendingShop = shopManager.getPendingShop(chestLocation);

            if (existingActiveShop != null) { // Aktif bir mağaza var
                if (existingActiveShop.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Bu sandık zaten sizin bir mağazanız.");
                    player.sendMessage(ChatColor.GRAY + "Mağaza yönetimi özellikleri yakında eklenecek (örn: fiyat değiştirme, mağazayı kaldırma).");
                    // TODO: Mağaza yönetim GUI'si açılabilir.
                    // Şimdilik, `/is shop remove <x> <y> <z>` gibi bir komutla kaldırma eklenebilir.
                } else {
                    player.sendMessage(ChatColor.RED + "Bu mağaza başkasına ait, yönetemezsiniz.");
                }
            } else if (existingPendingShop != null) { // Kurulumu devam eden bir mağaza var
                if (existingPendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Bu mağazanın kurulumu devam ediyor. Lütfen adımları takip edin.");
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation); // State'i tazele/ayarla
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation); // Kurulumun ilk adımını tekrar aç
                } else {
                    player.sendMessage(ChatColor.RED + "Bu sandık başkası tarafından mağaza olarak kuruluyor.");
                }
            } else { // Yeni mağaza kurulumu
                Shop newShopProvisional = shopManager.initiateShopCreation(chestLocation, player, null); // ShopType GUI'de seçilecek
                if (newShopProvisional != null) {
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation); // Kurulum state'ine ekle
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation); // İlk kurulum GUI'sini aç
                    player.sendMessage(ChatColor.GREEN + "Mağaza kurulumu başlatıldı. Lütfen mağaza türünü seçin.");
                } else {
                    // Bu durum, initiateShopCreation içinde bir sorun olursa veya isShop kontrolü atlanırsa olabilir.
                    player.sendMessage(ChatColor.RED + "Mağaza kurulumu başlatılamadı. Bu konumda zaten bir mağaza olabilir.");
                }
            }
        } else { // Normal Sağ Tık: Mağaza Görüntüleme/Satın Alma veya Normal Sandık Erişimi
            Shop activeShop = shopManager.getActiveShop(chestLocation);

            if (activeShop != null && activeShop.isSetupComplete()) { // Aktif ve kurulumu tamamlanmış bir mağaza ise
                event.setCancelled(true); // Normal sandık açılmasını engelle
                if (!activeShop.getOwnerUUID().equals(player.getUniqueId())) {
                    // Başkasının mağazası, satın alma GUI'sini aç
                    plugin.getPlayerViewingShopLocation().put(player.getUniqueId(), chestLocation); // Oyuncunun hangi mağazaya baktığını kaydet
                    shopVisitGUIManager.openShopVisitMenu(player, activeShop); // Senin GUI Manager'ın
                } else {
                    // Kendi aktif mağazasına normal sağ tıkladı, sandığı açmasına izin ver.
                    event.setCancelled(false);
                    // player.sendMessage(ChatColor.YELLOW + "Bu senin mağazan. İçeriğini düzenleyebilirsin."); // İsteğe bağlı mesaj
                }
            } else {
                Shop pendingShop = shopManager.getPendingShop(chestLocation);
                if (pendingShop != null && pendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    // Kendi kurulumdaki mağazasına normal sağ tıkladı, kurulum GUI'sini devam ettir.
                    event.setCancelled(true);
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
                    // Hangi adımda kaldığını kontrol edip o adımı açmak daha iyi olurdu,
                    // ama şimdilik en başa yönlendiriyoruz.
                    shopSetupGUIManager.openShopTypeSelectionMenu(player, chestLocation);
                    player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumuna devam ediyorsunuz...");
                }
                // else: Mağaza değilse veya başkasının kurulumdaki mağazasıysa,
                // Bukkit'in normal sandık açma/kapama davranışına izin ver (event iptal edilmedi).
            }
        }
    }
}