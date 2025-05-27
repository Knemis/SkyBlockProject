// com/knemis/skyblock/skyblockcoreproject/listeners/ShopSetupListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // ShopAdminGUIManager importu eklendi
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    // Updated slot constants for three shop types
    private static final int PLAYER_SELL_SHOP_SLOT = 10;
    private static final int PLAYER_BUY_SELL_SHOP_SLOT = 13;
    private static final int PLAYER_BUY_SHOP_SLOT = 16;

    private static final int ITEM_PLACEMENT_SLOT = 13; // This is for ITEM_SELECT_TITLE and QUANTITY_INPUT_TITLE GUIs
    private static final int CONFIRM_BUTTON_SLOT_QUANTITY = 31;

    public ShopSetupListener(SkyBlockProject plugin, ShopManager shopManager, ShopSetupGUIManager shopSetupGUIManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();

        if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int rawSlot = event.getRawSlot();
            // Check against new slot constants
            if (rawSlot != PLAYER_SELL_SHOP_SLOT && rawSlot != PLAYER_BUY_SELL_SHOP_SLOT && rawSlot != PLAYER_BUY_SHOP_SLOT) return;

            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            if (chestLocation == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum hatası! Lütfen tekrar deneyin.");
                player.closeInventory();
                return;
            }
            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            if (pendingShop == null) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum bilgisi bulunamadı. Lütfen tekrar deneyin.");
                player.closeInventory();
                return;
            }

            ShopType selectedType = null;
            if (clickedItem.getType() == Material.CHEST && rawSlot == PLAYER_SELL_SHOP_SLOT) {
                selectedType = ShopType.PLAYER_SELL_SHOP;
            } else if (clickedItem.getType() == Material.REPEATER && rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) { // Assuming REPEATER for BUY_SELL
                selectedType = ShopType.PLAYER_BUY_SELL_SHOP;
            } else if (clickedItem.getType() == Material.HOPPER && rawSlot == PLAYER_BUY_SHOP_SLOT) {
                selectedType = ShopType.PLAYER_BUY_SHOP;
            }

            if (selectedType != null) {
                pendingShop.setShopType(selectedType);
                shopSetupGUIManager.openItemSelectionMenu(player, pendingShop);
            }
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            handleItemSelectionGuiClickLogic(event, player, topInventory);
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            handleQuantityInputGuiClickLogic(event, player, topInventory);
        }
    }

    private void handleItemSelectionGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    ItemStack currentItemInPlacementSlot = guiInventory.getItem(ITEM_PLACEMENT_SLOT);
                    if (currentItemInPlacementSlot == null || currentItemInPlacementSlot.getType() == Material.AIR) {
                        ItemStack template = itemToMove.clone();
                        guiInventory.setItem(ITEM_PLACEMENT_SLOT, template);
                        event.setCurrentItem(null);
                        player.updateInventory();
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Eşya seçme yuvası zaten dolu. Lütfen önce mevcut eşyayı alın.");
                    }
                }
            } else {
                event.setCancelled(false);
            }
            return;
        }
        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == ITEM_PLACEMENT_SLOT) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    private void handleQuantityInputGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory) {
        Inventory clickedInventory = event.getClickedInventory();
        Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
        Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;

        if (pendingShop == null || pendingShop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "Kurulum hatası: Satılacak eşya şablonu bulunamadı. Lütfen baştan başlayın.");
            player.closeInventory();
            event.setCancelled(true);
            plugin.getPlayerShopSetupState().remove(player.getUniqueId());
            return;
        }
        ItemStack templateItem = pendingShop.getTemplateItemStack();

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.isSimilar(templateItem)) {
                    ItemStack currentItemInSlot = guiInventory.getItem(ITEM_PLACEMENT_SLOT);
                    if (currentItemInSlot == null || currentItemInSlot.getType() == Material.AIR) {
                        guiInventory.setItem(ITEM_PLACEMENT_SLOT, itemToMove.clone());
                        event.setCurrentItem(null);
                    } else if (currentItemInSlot.isSimilar(itemToMove) && currentItemInSlot.getAmount() < currentItemInSlot.getMaxStackSize()) {
                        int canAdd = currentItemInSlot.getMaxStackSize() - currentItemInSlot.getAmount();
                        int willAdd = Math.min(canAdd, itemToMove.getAmount());
                        if (willAdd > 0) {
                            currentItemInSlot.setAmount(currentItemInSlot.getAmount() + willAdd);
                            if (itemToMove.getAmount() - willAdd > 0) itemToMove.setAmount(itemToMove.getAmount() - willAdd);
                            else event.setCurrentItem(null);
                        }
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Miktar yuvası dolu veya daha fazla eklenemiyor.");
                    }
                    player.updateInventory();
                } else if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + templateItem.getType() + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                }
            } else {
                event.setCancelled(false);
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == CONFIRM_BUTTON_SLOT_QUANTITY && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_WOOL) {
                event.setCancelled(true);
                ItemStack quantityItemStack = guiInventory.getItem(ITEM_PLACEMENT_SLOT);
                if (quantityItemStack != null && quantityItemStack.isSimilar(templateItem) && quantityItemStack.getAmount() > 0) {
                    pendingShop.setItemQuantityForPrice(quantityItemStack.getAmount());
                    shopSetupGUIManager.promptForPrice(player, pendingShop);
                } else {
                    player.sendMessage(ChatColor.RED + "Lütfen miktar yuvasına doğru türde (" + templateItem.getType() + ") ve geçerli miktarda eşya koyun.");
                }
            }
            else if (event.getRawSlot() == ITEM_PLACEMENT_SLOT) {
                ItemStack cursorItem = event.getCursor();
                if (event.getAction().name().startsWith("PLACE_")) {
                    if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                        if (cursorItem.isSimilar(templateItem)) {
                            event.setCancelled(false);
                        } else {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + templateItem.getType() + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                    }
                } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR && !cursorItem.isSimilar(templateItem)) {
                        player.sendMessage(ChatColor.RED + "Bu yuvayla sadece şablondaki (" + ChatColor.AQUA + templateItem.getType() + ChatColor.RED + ") türünde eşya değiştirebilirsiniz!");
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false);
                    }
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(false);
                }
                else event.setCancelled(true);
            }
            else event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Component viewTitleComponent = event.getView().title();
        UUID playerId = player.getUniqueId();

        Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
        // Sadece dükkan kurulum state'i varsa devam et, admin state'i ShopListener'da ele alınıyor.
        if (chestLocation == null) {
            return;
        }

        Shop pendingShop = shopManager.getPendingShop(chestLocation);
        if (pendingShop == null) {
            plugin.getPlayerShopSetupState().remove(playerId);
            return;
        }

        boolean isStillInSetupChain = false;

        if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                pendingShop.setTemplateItemStack(itemInSlot.clone());
                // Store initial stock only if it's not a pure BUY shop.
                // For BUY_SELL shops, it's provisionally stored; might be cleared later if only buying.
                if (pendingShop.getShopType() != ShopType.PLAYER_BUY_SHOP) {
                    plugin.getPlayerInitialShopStockItem().put(playerId, itemInSlot.clone());
                    player.sendMessage(ChatColor.GREEN + "Template item set: " + ChatColor.AQUA + getItemNameForMessages(itemInSlot) + ChatColor.GREEN + ". This will be your initial stock if your shop sells this item.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Template item set: " + ChatColor.AQUA + getItemNameForMessages(itemInSlot) + ChatColor.GREEN + ". (Initial stock not applicable for Buy-Only shops).");
                }
                event.getInventory().setItem(ITEM_PLACEMENT_SLOT, null); // Item is now held by the setup process


                // Check if it's a PLAYER_BUY_SHOP, if so, clear any potentially (erroneously) stored initial stock.
                // This specific check here in ITEM_SELECT_TITLE's close might be slightly redundant
                // if type is selected first, but good for safety. The main check would be before finalize.
                if (pendingShop.getShopType() == ShopType.PLAYER_BUY_SHOP) {
                    ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(player.getUniqueId());
                    if (initialStock != null && initialStock.getType() != Material.AIR) {
                        // This item should not have been stored, but if it was, return it.
                        // Normally, the player wouldn't have a chance to set initial stock for a BUY_SHOP.
                        player.getInventory().addItem(initialStock);
                        player.sendMessage(ChatColor.YELLOW + "Internal note: Initial stock item was cleared as this is a 'Buy Shop'.");
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && plugin.getPlayerShopSetupState().containsKey(playerId)) { // Hala kurulumda mı diye kontrol et
                            shopSetupGUIManager.openQuantityInputMenu(player, pendingShop);
                        }
                    }
                }.runTask(plugin);
                isStillInSetupChain = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "Eşya seçilmedi, mağaza kurulumu iptal ediliyor.");
                plugin.getPlayerInitialShopStockItem().remove(playerId); // Ensure cleared if never set or cancelled
            }
        } else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            if (pendingShop.getItemQuantityForPrice() <= 0) { // Quantity not confirmed, setup cancelled at this stage
                ItemStack itemInSlot = event.getInventory().getItem(ITEM_PLACEMENT_SLOT); // Item for quantity
                if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                    player.getInventory().addItem(itemInSlot.clone());
                    player.sendMessage(ChatColor.YELLOW + "Miktar belirlenmedi, miktar yuvasındaki eşyalar iade edildi.");
                }

                ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);
                if (initialStock != null && initialStock.getType() != Material.AIR) {
                    player.getInventory().addItem(initialStock);
                    player.sendMessage(ChatColor.YELLOW + "Başlangıç için ayrılan eşya (" + ChatColor.AQUA + getItemNameForMessages(initialStock) + ChatColor.YELLOW + ") envanterinize iade edildi.");
                }
                player.sendMessage(ChatColor.YELLOW + "Miktar belirlenmedi, mağaza kurulumu iptal ediliyor.");
            } else {
                // Quantity was confirmed, proceeding to price input via chat
                isStillInSetupChain = true;
            }
            event.getInventory().setItem(ITEM_PLACEMENT_SLOT, null); // Clear the slot in quantity GUI
        }

        if (!isStillInSetupChain) {
            plugin.getPlayerShopSetupState().remove(playerId);
            plugin.getPlayerInitialShopStockItem().remove(playerId); // General cleanup if setup chain broken
            // shopManager.removePendingShop(chestLocation); // Consider if needed
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        boolean isItemSelectGui = viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE);
        boolean isQuantityGui = viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE);

        if (isItemSelectGui || isQuantityGui) {
            boolean affectsOnlyPlacementSlot = true;
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topInventory.getSize()) {
                    if (rawSlot != ITEM_PLACEMENT_SLOT) {
                        affectsOnlyPlacementSlot = false;
                        break;
                    }
                }
            }

            if (!affectsOnlyPlacementSlot) {
                event.setCancelled(true);
            } else {
                if (isQuantityGui) {
                    Shop pendingShop = shopManager.getPendingShop(plugin.getPlayerShopSetupState().get(player.getUniqueId()));
                    if (pendingShop != null && pendingShop.getTemplateItemStack() != null) {
                        ItemStack draggedItem = event.getOldCursor();
                        if (draggedItem != null && draggedItem.getType() != Material.AIR && !draggedItem.isSimilar(pendingShop.getTemplateItemStack())) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + pendingShop.getTemplateItemStack().getType() + ChatColor.RED + ") türünde eşya sürükleyebilirsiniz!");
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChatForPrice(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        // --- DÜKKAN YÖNETİMİ İÇİN YENİ EKLENEN BLOK ---
        if (plugin.getPlayerWaitingForAdminInput().containsKey(playerId)) {
            event.setCancelled(true); // Mesajın normal sohbete gitmesini engelle

            ShopAdminGUIManager.AdminInputType inputType = plugin.getPlayerWaitingForAdminInput().get(playerId);
            Location shopLocation = plugin.getPlayerAdministeringShop().get(playerId);

            if (shopLocation == null) {
                // Bu durum, state'ler düzgün yönetilirse idealde oluşmamalı
                player.sendMessage(ChatColor.RED + "Hata: Yönetilmekte olan dükkan bulunamadı. İşlem iptal edildi.");
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId); // Yönetim state'ini de temizle
                return;
            }

            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId); // Yönetim state'ini de temizle
                player.sendMessage(ChatColor.YELLOW + "Dükkan ayarı işlemi iptal edildi.");
                return;
            }

            // Dükkan işlemleri ve Bukkit API çağrıları ana thread'de yapılmalı
            new BukkitRunnable() {
                @Override
                public void run() {
                    Shop shop = shopManager.getActiveShop(shopLocation); // Dükkanı ana thread'de al
                    if (shop == null || !shop.getOwnerUUID().equals(playerId)) {
                        player.sendMessage(ChatColor.RED + "Hata: Dükkan bulunamadı veya bu dükkanı yönetme yetkiniz yok.");
                        plugin.getPlayerWaitingForAdminInput().remove(playerId);
                        plugin.getPlayerAdministeringShop().remove(playerId);
                        return;
                    }

                    boolean actionSuccess = false;
                    if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME) {
                        String newDisplayName = message;
                        // İsim için basit bir geçerlilik kontrolü (plugin.yml'den veya config'den uzunluk alınabilir)
                        int maxNameLength = plugin.getConfig().getInt("shop.max_display_name_length", 30);
                        if (newDisplayName.length() > 0 && newDisplayName.length() <= maxNameLength) {
                            shop.setShopDisplayName(newDisplayName);
                            actionSuccess = true;
                            player.sendMessage(ChatColor.GREEN + "Dükkan adı başarıyla '" + ChatColor.AQUA + newDisplayName + ChatColor.GREEN + "' olarak ayarlandı.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Geçersiz dükkan adı. Ad 1-" + maxNameLength + " karakter uzunluğunda olmalıdır. Tekrar deneyin veya 'iptal' yazın.");
                        }
                    } else if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_PRICE) {
                        try {
                            double newPrice = Double.parseDouble(message);
                            if (newPrice >= 0) { // Fiyat 0 veya daha büyük olabilir (ücretsiz ürünler için)
                                shop.setPrice(newPrice);
                                actionSuccess = true;
                                player.sendMessage(ChatColor.GREEN + "Dükkan paket fiyatı başarıyla " + ChatColor.GOLD + String.format("%.2f", newPrice) + ChatColor.GREEN + " olarak ayarlandı.");
                            } else {
                                player.sendMessage(ChatColor.RED + "Fiyat negatif olamaz. Lütfen geçerli bir fiyat girin (örn: 10.5) veya 'iptal' yazın.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz fiyat formatı. Lütfen bir sayı girin (örn: 10.5 veya 100) veya 'iptal' yazın.");
                        }
                    }

                    if (actionSuccess) {
                        shopManager.getShopStorage().saveShop(shop); // Değişiklikleri kalıcı olarak kaydet
                        shopManager.updateAttachedSign(shop);       // Dükkan tabelasını güncelle
                        plugin.getPlayerWaitingForAdminInput().remove(playerId); // Başarılı işlem sonrası state'leri temizle
                        plugin.getPlayerAdministeringShop().remove(playerId);
                    }
                    // Eğer actionSuccess false ise (geçersiz giriş gibi), state'ler temizlenmez.
                    // Oyuncu tekrar deneyebilir veya 'iptal' yazabilir.
                }
            }.runTask(plugin); // Ana thread üzerinde çalıştır

            return; // Dükkan yönetim girişi bu event tarafından ele alındı, daha fazla işlem yapma.
        }
        // --- DÜKKAN YÖNETİMİ İÇİN YENİ EKLENEN BLOK SONU ---


        // --- MEVCUT DÜKKAN KURULUM FİYATI GİRİŞİ MANTIĞI ---
        if (plugin.getPlayerShopSetupState().containsKey(playerId)) {
            event.setCancelled(true); // Bu mesajın da normal sohbete gitmesini engelle
            Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
            Shop pendingShop = shopManager.getPendingShop(chestLocation);

            if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getItemQuantityForPrice() <= 0) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulumunda bir hata oluştu (eksik bilgi). Lütfen baştan başlayın.");
                plugin.getPlayerShopSetupState().remove(playerId);
                // shopManager.removePendingShop(chestLocation); // Eğer varsa, bekleyen dükkanı da temizle
                return;
            }

            // 'iptal' komutu burada da geçerli olmalı
            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                ItemStack initialStockToReturn = plugin.getPlayerInitialShopStockItem().remove(playerId);
                if (initialStockToReturn != null && initialStockToReturn.getType() != Material.AIR) {
                    player.getInventory().addItem(initialStockToReturn);
                    player.sendMessage(ChatColor.YELLOW + "Initial stock item (" + ChatColor.AQUA + getItemNameForMessages(initialStockToReturn) + ChatColor.YELLOW + ") returned to inventory.");
                }
                plugin.getPlayerShopSetupState().remove(playerId);
                player.sendMessage(ChatColor.YELLOW + "Price input and shop setup cancelled.");
                return;
            }

            // ShopType is no longer used to determine price input style.
            // All shops now use BUY_PRICE:SELL_PRICE format.
            ItemStack actualInitialStock = plugin.getPlayerInitialShopStockItem().get(playerId); // Get, don't remove yet

            try {
                String[] priceParts = message.split(":");
                if (priceParts.length != 2) {
                    player.sendMessage(ChatColor.RED + "Invalid format. Please enter prices as YOUR_BUY_PRICE:YOUR_SELL_PRICE (e.g., 100:80 or 50:-1, or -1:70). Or type 'iptal'.");
                    return;
                }

                double buyPrice = Double.parseDouble(priceParts[0].trim());
                double sellPrice = Double.parseDouble(priceParts[1].trim());

                // Validate prices
                if (buyPrice < 0 && buyPrice != -1) {
                    player.sendMessage(ChatColor.RED + "Invalid Player Buy Price. Must be a positive number or -1 to disable your shop selling this item. Try again or 'iptal'.");
                    return;
                }
                if (sellPrice < 0 && sellPrice != -1) {
                    player.sendMessage(ChatColor.RED + "Invalid Player Sell Price. Must be a positive number or -1 to disable your shop buying this item. Try again or 'iptal'.");
                    return;
                }
                if (buyPrice == -1 && sellPrice == -1) {
                    player.sendMessage(ChatColor.RED + "A shop must either allow players to buy from it, or sell to it (or both). You cannot set both prices to -1. Try again or 'iptal'.");
                    return;
                }

                pendingShop.setBuyPrice(buyPrice);
                pendingShop.setSellPrice(sellPrice);

                // Adjust initial stock based on if the shop will sell items.
                // If buyPrice is -1, the shop does not sell, so no initial stock is needed from the owner.
                if (buyPrice == -1) {
                    actualInitialStock = null;
                    plugin.getPlayerInitialShopStockItem().remove(playerId); // Clear it if it was there
                }
                // If buyPrice is not -1, actualInitialStock will be the item from getPlayerInitialShopStockItem()
                // or null if nothing was there (which shouldn't happen if template item was set).

                player.sendMessage(ChatColor.GREEN + "Prices accepted. Player Buy Price: " + (buyPrice == -1 ? "Disabled" : String.format("%.2f", buyPrice)) +
                        ", Player Sell Price: " + (sellPrice == -1 ? "Disabled" : String.format("%.2f", sellPrice)));

                // Finalize: Remove states and call ShopManager
                plugin.getPlayerShopSetupState().remove(playerId);
                // Remove initial stock from map only if it wasn't cleared above (i.e., if buyPrice != -1)
                // or if it was already null. Effectively, always remove it as its fate is decided.
                plugin.getPlayerInitialShopStockItem().remove(playerId);


                // Make a final copy for the runnable
                final ItemStack finalInitialStock = actualInitialStock;
                final Shop finalPendingShop = pendingShop; // Capture current state for runnable

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        shopManager.finalizeShopSetup(
                                chestLocation,
                                player,
                                finalInitialStock // This will be null if buyPrice is -1 (shop doesn't sell)
                        );
                    }
                }.runTask(plugin);

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number format for prices. Please use numbers (e.g., 100.50 or -1). Example: 10.5:8.0 or 50:-1 or -1:20.25. Or type 'iptal'.");
                // State'i koru, oyuncu tekrar denesin.
            }
        }
    }

    private String getItemNameForMessages(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen Eşya";
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()) {
                try {
                    Component displayNameComponent = meta.displayName();
                    if (displayNameComponent != null) {
                        return LegacyComponentSerializer.legacySection().serialize(displayNameComponent);
                    }
                } catch (Exception e) {
                    // Loglanabilir
                }
            }
        }
        // Basit bir isim döndürme
        String name = itemStack.getType().toString().toLowerCase().replace("_", " ");
        if (name.length() > 0) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }
}