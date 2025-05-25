// com/knemis/skyblock/skyblockcoreproject/listeners/ShopSetupListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopType;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component; // Adventure API Component importu
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    public ShopSetupListener(SkyBlockProject plugin, ShopManager shopManager, ShopSetupGUIManager shopSetupGUIManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory(); // Her zaman GUI envanteri
        // Inventory clickedInventory = event.getClickedInventory(); // handleGenericItemGuiInteraction içinde alınacak

        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title(); // GUI başlığını Component olarak al

        // Mağaza Türü Seçimi GUI'si
        if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) { // Component ile karşılaştır
            event.setCancelled(true);
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            if (chestLocation == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulumu için sandık konumu bulunamadı!");
                player.closeInventory();
                return;
            }
            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            if (pendingShop == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum bilgisi bulunamadı, lütfen tekrar deneyin.");
                player.closeInventory();
                return;
            }

            ShopType selectedType = null;
            if (currentItem.getType() == Material.CHEST) {
                selectedType = ShopType.TRADE_CHEST;
            } else if (currentItem.getType() == Material.ENDER_CHEST) {
                selectedType = ShopType.BANK_CHEST;
            }

            if (selectedType != null) {
                pendingShop.setShopType(selectedType);
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
            }
        }
        // Eşya Seçimi GUI'si
        else if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            handleGenericItemGuiInteraction(event, player, topInventory, ShopSetupGUIManager.ITEM_SELECT_TITLE, null);
        }
        // Miktar Belirleme GUI'si
        else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;

            if (pendingShop == null || pendingShop.getItemType() == null) {
                player.sendMessage(ChatColor.RED + "Kurulum hatası: Önceki adım (eşya seçimi) tamamlanmamış.");
                if (player.getOpenInventory().getTopInventory().equals(topInventory)) { // Sadece bizim GUI'miz açıksa kapat
                    player.closeInventory();
                }
                event.setCancelled(true);
                return;
            }
            handleGenericItemGuiInteraction(event, player, topInventory, ShopSetupGUIManager.QUANTITY_INPUT_TITLE, pendingShop.getItemType());
        }
    }

    private void handleGenericItemGuiInteraction(InventoryClickEvent event, Player player, Inventory guiInventory, Component guiTitle, Material expectedMaterialForQuantity) {
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack cursorItem = event.getCursor(); // İmleçteki eşya
        ItemStack currentClickedItemInSlot = event.getCurrentItem(); // Tıklanan slottaki eşya

        boolean isItemSelectGui = guiTitle.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE);
        boolean isQuantityGui = guiTitle.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE);

        // 1. Oyuncunun Kendi Envanterindeki Etkileşimler (Alt Envanter)
        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift-tıklama (oyuncu envanterinden GUI'ye)
                event.setCancelled(true); // Varsayılan davranışı her zaman iptal et, manuel yöneteceğiz
                if (currentClickedItemInSlot != null && currentClickedItemInSlot.getType() != Material.AIR) {
                    ItemStack targetSlotItemInGui = guiInventory.getItem(13); // GUI'deki hedef slot (13)

                    if (isItemSelectGui) {
                        if (targetSlotItemInGui == null || targetSlotItemInGui.getType() == Material.AIR) { // GUI'deki slot 13 boşsa
                            ItemStack oneItem = currentClickedItemInSlot.clone();
                            oneItem.setAmount(1);
                            guiInventory.setItem(13, oneItem); // GUI'ye 1 adet koy
                            if (currentClickedItemInSlot.getAmount() > 1) {
                                currentClickedItemInSlot.setAmount(currentClickedItemInSlot.getAmount() - 1);
                            } else {
                                event.setCurrentItem(null); // Oyuncunun slotundaki itemi bitir
                            }
                            player.updateInventory(); // Görsel güncelleme için
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "Eşya seçme yuvası zaten dolu. Önce boşaltın.");
                        }
                    } else if (isQuantityGui) {
                        if (currentClickedItemInSlot.getType() == expectedMaterialForQuantity) {
                            if (targetSlotItemInGui == null || targetSlotItemInGui.getType() == Material.AIR) {
                                guiInventory.setItem(13, currentClickedItemInSlot.clone()); // Tüm stack'i taşı
                                event.setCurrentItem(null); // Oyuncunun slotunu boşalt
                            } else if (targetSlotItemInGui.isSimilar(currentClickedItemInSlot) && targetSlotItemInGui.getAmount() < targetSlotItemInGui.getMaxStackSize()) {
                                int canAdd = targetSlotItemInGui.getMaxStackSize() - targetSlotItemInGui.getAmount();
                                int willAdd = Math.min(canAdd, currentClickedItemInSlot.getAmount());
                                if (willAdd > 0) {
                                    targetSlotItemInGui.setAmount(targetSlotItemInGui.getAmount() + willAdd);
                                    if (currentClickedItemInSlot.getAmount() - willAdd > 0) {
                                        currentClickedItemInSlot.setAmount(currentClickedItemInSlot.getAmount() - willAdd);
                                    } else {
                                        event.setCurrentItem(null);
                                    }
                                }
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Miktar yuvası dolu veya daha fazla eklenemiyor.");
                            }
                            player.updateInventory(); // Görsel güncelleme için
                        } else {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece " + ChatColor.AQUA + expectedMaterialForQuantity + ChatColor.RED + " türünde eşya koyabilirsiniz!");
                        }
                    }
                }
            } else {
                // Oyuncunun kendi envanterindeki diğer normal tıklamalar serbest bırakılmalı.
                event.setCancelled(false);
            }
            return; // Oyuncu envanteri etkileşimi işlendi.
        }

        // 2. GUI İçindeki Etkileşimler (Üst Envanter)
        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getSlot() == 13) { // Ana işlem slotu (13)
                if (isItemSelectGui) { // Eşya Seçim GUI'si, Slot 13
                    // PLACE_ONE: İmleçte item varsa ve slot 13 boşsa izin ver.
                    if (event.getAction() == InventoryAction.PLACE_ONE) {
                        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                            if (guiInventory.getItem(13) == null || guiInventory.getItem(13).getType() == Material.AIR) {
                                event.setCancelled(false);
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Eşya seçme yuvası zaten dolu. Önce boşaltın.");
                                event.setCancelled(true);
                            }
                        } else {
                            event.setCancelled(true); // Boş imleçle bir şey koymaya çalışma
                        }
                    }
                    // PICKUP ve SWAP: Slot 13'ten item alma veya imleçle değiştirme serbest.
                    else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                        event.setCancelled(false);
                    }
                    // MOVE_TO_OTHER_INVENTORY: Slot 13'teki itemi oyuncu envanterine shift-tıklama ile gönderme.
                    else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        event.setCancelled(false);
                    }
                    // Diğer tüm PLACE actionları (PLACE_ALL, PLACE_SOME) slot 13 için engelliyoruz, sadece tek item olmalı.
                    else if (event.getAction().name().startsWith("PLACE_")) {
                        player.sendMessage(ChatColor.YELLOW + "Bu yuvaya sadece tek bir eşya koyabilirsiniz (sürükleyerek veya sağ tıklayarak).");
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(true); // Diğer bilinmeyen actionlar
                    }
                } else if (isQuantityGui) { // Miktar Belirleme GUI'si, Slot 13
                    // PLACE actionları: İmleçteki item beklenen türdeyse izin ver.
                    if (event.getAction().name().startsWith("PLACE_")) {
                        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                            if (cursorItem.getType() == expectedMaterialForQuantity) {
                                event.setCancelled(false);
                            } else {
                                player.sendMessage(ChatColor.RED + "Bu yuvaya sadece " + ChatColor.AQUA + expectedMaterialForQuantity + ChatColor.RED + " türünde eşya koyabilirsiniz!");
                                event.setCancelled(true);
                            }
                        } else { // Boş imleçle bir şey koymaya çalışma
                            event.setCancelled(true);
                        }
                    }
                    // PICKUP ve SWAP actionları: Alma serbest. Swap yapılıyorsa imleçteki item türü kontrol edilir.
                    else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                        if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR && cursorItem.getType() != expectedMaterialForQuantity) {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece " + ChatColor.AQUA + expectedMaterialForQuantity + ChatColor.RED + " türünde eşya ile değiştirebilirsiniz!");
                            event.setCancelled(true);
                        } else {
                            event.setCancelled(false);
                        }
                    }
                    // MOVE_TO_OTHER_INVENTORY: Slot 13'teki itemi oyuncu envanterine shift-tıklama.
                    else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        event.setCancelled(false);
                    } else {
                        event.setCancelled(true); // Diğer bilinmeyen actionlar
                    }
                }
            } else if (isQuantityGui && event.getSlot() == 22 && currentClickedItemInSlot != null && currentClickedItemInSlot.getType() == Material.GREEN_WOOL) { // Miktar GUI'sinde Onayla butonu (Slot 22)
                event.setCancelled(true);
                Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
                Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;
                if (pendingShop != null) {
                    ItemStack quantityItemStack = guiInventory.getItem(13); // Slot 13'teki miktar itemi
                    if (quantityItemStack != null && quantityItemStack.getType() == pendingShop.getItemType() && quantityItemStack.getAmount() > 0) {
                        pendingShop.setItemQuantityForPrice(quantityItemStack.getAmount());
                        shopSetupGUIManager.promptForPrice(player, pendingShop);
                    } else {
                        player.sendMessage(ChatColor.RED + "Lütfen ortadaki slota doğru türde (" + pendingShop.getItemType() + ") ve geçerli miktarda eşya koyun.");
                    }
                }
            } else {
                // GUI'deki diğer tüm slotlara (bilgi itemleri, boşluklar vb.) tıklamayı engelle
                event.setCancelled(true);
            }
            return; // GUI etkileşimi işlendi.
        }
        // Eğer tıklanan envanter null ise (GUI dışına tıklama gibi) veya yukarıdaki hiçbir koşul karşılanmadıysa,
        // Bukkit'in normal işlemesine izin vermek genellikle en iyisidir, bu yüzden burada bir şey yapmıyoruz
        // (varsayılan olarak event.setCancelled(false) olur).
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Component viewTitleComponent = event.getView().title();

        // Eşya Seçimi GUI'si kapandığında
        if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            if (chestLocation == null) return;

            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            if (pendingShop == null || pendingShop.getShopType() == null) {
                plugin.getPlayerShopSetupState().remove(player.getUniqueId());
                return;
            }

            ItemStack selectedItem = event.getInventory().getItem(13); // GUI'nin orta slotu
            if (selectedItem != null && selectedItem.getType() != Material.AIR) {
                // Miktar GUI'sine geçerken, seçilen itemin sadece türünü alıyoruz.
                // Slot 13'teki itemin miktarını 1'e ayarlamaya gerek yok, çünkü miktar GUI'sinde
                // oyuncu zaten bu türden istediği miktarı koyacak.
                Material itemType = selectedItem.getType(); // Türü al
                event.getInventory().setItem(13, null);
                pendingShop.setItemType(itemType);
                player.sendMessage(ChatColor.GREEN + "Eşya türü belirlendi: " + ChatColor.AQUA + selectedItem.getType());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && plugin.getPlayerShopSetupState().containsKey(player.getUniqueId())) { // State hala geçerli mi kontrol et
                            shopSetupGUIManager.openQuantityInputMenu(player, pendingShop);
                        } else { // Oyuncu offline olduysa veya state temizlendi_ise
                            plugin.getPlayerShopSetupState().remove(player.getUniqueId());
                            // Eğer item oyuncuya geri verilecekse, burada yapılabilir ama selectedItem artık GUI'de yok.
                            // Bu yüzden itemin kaybolmaması için en iyi çözüm, oyuncunun envanterinden
                            // itemin eksilmemesini sağlamak (sadece tipini okumak).
                            // Ya da, slot 13'e konan item'i InventoryClickEvent'te oyuncuya geri vermek
                            // eğer GUI kapatılırsa.
                        }
                    }
                }.runTask(plugin); // Hemen sonraki tick'te çalıştır

            } else {
                // Eşya seçilmeden kapatıldıysa (örn: ESC ile), kurulum state'ini temizle
                plugin.getPlayerShopSetupState().remove(player.getUniqueId());
                // Slot 13'te item varsa (ki bu durumda selectedItem null olmazdı),
                // bu itemin oyuncuya geri verilmesi gerekebilir.
                // Ancak yukarıdaki selectedItem null kontrolü bu durumu kapsıyor.
                // Eğer oyuncu item koyup sonra geri aldıysa selectedItem null olabilir.
                player.sendMessage(ChatColor.YELLOW + "Eşya seçilmedi, mağaza kurulumu iptal edildi.");
            }
        }
        // Miktar Belirleme GUI'si veya Fiyat Girişi Adımı Sonrası Temizlik
        // Not: Fiyat girişi chat üzerinden olduğu için, başarılı veya "iptal" durumunda
        // AsyncPlayerChatEvent içinde playerShopSetupState temizleniyor.
        // Bu blok, oyuncu Miktar GUI'sini ESC ile kapatırsa ve henüz Onayla'ya basmamışsa diye
        // bir güvenlik önlemi olarak düşünülebilir, ama AsyncPlayerChatEvent daha kritik.
        else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            // Eğer oyuncu bu GUI'yi kapattıysa ve hala setup state'inde ise (yani chat'e yönlendirilmediyse)
            // bu, kurulumu iptal ettiği anlamına gelebilir.
            // Ancak bu, Onayla butonuna basıldığında da tetiklenir (çünkü promptForPrice GUI'yi kapatır).
            // Bu yüzden burada state temizliği yapmak yerine AsyncPlayerChatEvent'e güvenmek daha iyi.
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title(); // <--- CORRECTED LINE


        boolean isItemSelectGui = viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE);
        boolean isQuantityGui = viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE);

        if (isItemSelectGui || isQuantityGui) {
            // Sürükleme işleminin GUI'yi etkileyip etkilemediğini kontrol et
            boolean affectsTopInventory = false;
            for (int rawSlot : event.getRawSlots()) { // event.getInventorySlots() sadece etkilenen envanterdeki slotları verir.
                // event.getRawSlots() ise ham slot numaralarını verir (üst+alt)
                if (rawSlot < topInventory.getSize()) { // Eğer slot GUI'nin içindeyse (üst envanter)
                    affectsTopInventory = true;
                    break;
                }
            }

            if (affectsTopInventory) {
                // DÜZELTME: affectsOnlySlot13 burada tanımlanmalı ve kullanılmalı.
                boolean affectsOnlySlot13 = true; // Değişken burada tanımlanıyor.
                for (int guiSlot : event.getInventorySlots()) { // getInventorySlots() sürüklemenin etkilediği GUI slotlarını verir.
                    if (guiSlot != 13) { // Eğer slot 13 dışındaki bir GUI slotu da etkileniyorsa
                        affectsOnlySlot13 = false;
                        break;
                    }
                }

                // Eğer slot 13 dışındaki GUI slotlarına sürükleme yapılıyorsa iptal et
                if (!affectsOnlySlot13) {
                    event.setCancelled(true);
                    return; // Başka işlem yapma
                }

                // Buraya gelindiyse, sürükleme sadece slot 13'ü etkiliyor veya slot 13'ten başlıyor/bitiyor demektir.
                // Slot 13'e sürükleme yapılıyorsa veya slot 13'ten alınıyorsa...
                if (isItemSelectGui) {
                    // Eşya Seçim GUI'sinde slot 13'e sürüklenen itemin miktarını 1 ile sınırlamak
                    // sürükleme olayında biraz daha karmaşıktır. Genellikle tek tıklama ile yerleştirme
                    // bu tür senaryolar için daha kullanıcı dostudur.
                    // Şimdilik, eğer sürükleme slot 13'e yapılıyorsa ve slot boşsa izin verelim,
                    // doluysa ve farklı item ise engelleyelim. InventoryCloseEvent miktarı 1'e ayarlayacak.
                    ItemStack draggedItem = event.getOldCursor(); // Sürüklenen (imleçteki) eşya
                    ItemStack itemInSlot13 = topInventory.getItem(13);

                    if (itemInSlot13 != null && itemInSlot13.getType() != Material.AIR) {
                        // Slot 13 doluysa ve sürüklenen item farklıysa veya imleç boş değilse (yani üzerine bırakma değilse)
                        // bu durumu daha detaylı ele almak gerekebilir. Şimdilik basit bir engelleme:
                        if (draggedItem != null && draggedItem.getType() != Material.AIR && !draggedItem.isSimilar(itemInSlot13)) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.YELLOW + "Bu slota farklı bir eşya sürükleyemezsiniz. Önce boşaltın.");
                        }
                        // Eğer aynı item ise veya imleç boşsa (yani slottan item alınıyorsa) Bukkit'in varsayılanına bırakılabilir
                        // (ancak yukarıdaki !affectsOnlySlot13 kontrolü diğer slotları engellediği için burası sadece slot 13 ile ilgili).
                    }
                    // Eğer slot 13 boşsa ve item sürükleniyorsa, InventoryClickEvent gibi davranmasına izin verilebilir.
                    // Ancak, sürükleme birden fazla itemi tek seferde bırakabilir.
                    // Bu GUI'de sürüklemeyi iptal etmek ve tek tıklamaya zorlamak daha basit olabilir:
                    // player.sendMessage(ChatColor.YELLOW + "Lütfen eşyayı tıklayarak yerleştirin.");
                    // event.setCancelled(true);

                } else if (isQuantityGui) {
                    Shop pendingShop = shopManager.getPendingShop(plugin.getPlayerShopSetupState().get(player));
                    Material expectedMaterial = (pendingShop != null) ? pendingShop.getItemType() : null;
                    ItemStack draggedItem = event.getOldCursor(); // Sürüklenen (imleçteki) eşya

                    if (draggedItem != null && draggedItem.getType() != Material.AIR && draggedItem.getType() != expectedMaterial) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Bu yuvaya sadece " + ChatColor.AQUA + expectedMaterial + ChatColor.RED + " türünde eşya sürükleyebilirsiniz!");
                    }
                    // Eğer doğru türdeyse, Bukkit'in normal sürükleme/stackleme mantığına izin verilebilir.
                    // Slot 13'e bırakıldığında, mevcut item ile birleştirme veya üzerine yazma Bukkit tarafından yapılır.
                }
            }
            // Eğer sürükleme sadece oyuncu envanteri içindeyse veya oyuncu envanterinden GUI dışına ise,
            // affectsTopInventory false olacağı için bu bloğa girmez ve event iptal edilmez.
        }
    }

    @EventHandler
    public void onPlayerChatForPrice(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (plugin.getPlayerShopSetupState().containsKey(playerId)) {
            event.setCancelled(true); // Chat mesajının normalde görünmesini engelle
            Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
            Shop pendingShop = shopManager.getPendingShop(chestLocation);

            if (pendingShop == null || pendingShop.getItemType() == null || pendingShop.getItemQuantityForPrice() <= 0) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulumunda bir hata oluştu (eksik bilgi). Lütfen tekrar deneyin.");
                plugin.getPlayerShopSetupState().remove(playerId); // Hatalı durumda state'i temizle
                return;
            }

            String message = event.getMessage();
            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerShopSetupState().remove(playerId); // State'i temizle
                // İsteğe bağlı: shopManager.cancelPendingShopSetup(chestLocation); gibi bir metod eklenebilir.
                player.sendMessage(ChatColor.YELLOW + "Fiyat girişi ve mağaza kurulumu iptal edildi.");
                return;
            }

            try {
                double price = Double.parseDouble(message);
                if (price <= 0) {
                    player.sendMessage(ChatColor.RED + "Fiyat 0'dan büyük olmalıdır. Lütfen geçerli bir fiyat girin veya 'iptal' yazın.");
                    // State'i koru ki oyuncu tekrar deneyebilsin. promptForPrice tekrar çağrılmıyor, oyuncu yeni mesaj yazacak.
                    return;
                }

                double finalPrice = price;
                // BukkitScheduler ile ana thread'e geçiş
                Bukkit.getScheduler().runTask(plugin, () -> {
                    shopManager.finalizeShopSetup(chestLocation, pendingShop.getItemType(), pendingShop.getItemQuantityForPrice(), finalPrice);
                    String currencyName = "Para"; // Varsayılan
                    if (plugin.getEconomy() != null && plugin.getEconomy().currencyNamePlural() != null && !plugin.getEconomy().currencyNamePlural().isEmpty()) {
                        currencyName = plugin.getEconomy().currencyNamePlural();
                    }
                    player.sendMessage(ChatColor.GREEN + "Mağaza başarıyla kuruldu! " +
                            ChatColor.AQUA + pendingShop.getItemQuantityForPrice() + " adet " +
                            ChatColor.LIGHT_PURPLE + pendingShop.getItemType().toString().toLowerCase().replace("_", " ") +
                            ChatColor.GREEN + " için fiyat: " + ChatColor.GOLD + String.format("%.2f", finalPrice) + " " + currencyName);
                    plugin.getPlayerShopSetupState().remove(playerId); // Başarılı kurulumdan sonra state'i temizle
                });

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Geçersiz fiyat formatı. Lütfen sadece sayı girin (örn: 10.5 veya 100) veya 'iptal' yazın.");
                // State'i koru ki oyuncu tekrar deneyebilsin.
            }
        }
    }
}