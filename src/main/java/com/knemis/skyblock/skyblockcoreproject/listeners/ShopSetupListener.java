// com/knemis/skyblock/skyblockcoreproject/listeners/ShopSetupListener.java
package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

public class ShopSetupListener implements Listener {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;

    private static final int PLAYER_BUY_SHOP_SLOT = 2;
    private static final int PLAYER_SELL_SHOP_SLOT = 4;
    private static final int PLAYER_BUY_SELL_SHOP_SLOT = 6;

    // ITEM_SELECT_TITLE için ortadaki slot (13), QUANTITY_INPUT_TITLE için miktar girme slotu (22) olmalı
    // ShopSetupGUIManager.openQuantityInputMenu'deki tasarıma göre güncellenmeli.
    // Şimdilik, her iki GUI için de farklı olabileceklerini varsayarak,
    // handleQuantityInputGuiClickLogic'e slot parametresi eklemek yerine
    // ITEM_PLACEMENT_SLOT'u genel bir yerleştirme slotu olarak düşünelim
    // ve GUI tasarımına göre bu listener içindeki click logic'te doğru slotu kullanalım.
    private static final int ITEM_SELECT_PLACEMENT_SLOT = 13; // Eşya seçimi GUI'sindeki yerleştirme slotu
    private static final int QUANTITY_PLACEMENT_SLOT = 22;    // Miktar GUI'sindeki yerleştirme slotu
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
            if (rawSlot == PLAYER_BUY_SHOP_SLOT) {
                selectedType = ShopType.PLAYER_BUY_SHOP;
            } else if (rawSlot == PLAYER_SELL_SHOP_SLOT) {
                selectedType = ShopType.PLAYER_SELL_SHOP;
            } else if (rawSlot == PLAYER_BUY_SELL_SHOP_SLOT) {
                selectedType = ShopType.PLAYER_BUY_SELL_SHOP;
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
        else if (viewTitleComponent.equals(ShopSetupGUIManager.CONFIRMATION_TITLE)) {
            event.setCancelled(true);
            Location chestLocation = plugin.getPlayerShopSetupState().get(player.getUniqueId());
            Shop pendingShop = (chestLocation != null) ? shopManager.getPendingShop(chestLocation) : null;

            // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
            if (pendingShop == null || !plugin.getPlayerWaitingForSetupInput().containsKey(player.getUniqueId()) ||
                    plugin.getPlayerWaitingForSetupInput().get(player.getUniqueId()) != ShopSetupGUIManager.InputType.PRICE) {
                player.sendMessage(ChatColor.RED + "Onaylama hatası. Kurulum bilgileri eksik.");
                player.closeInventory();
                plugin.getShopManager().cancelShopSetup(player.getUniqueId());
                return;
            }

            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getType() == Material.GREEN_WOOL) {
                    shopManager.finalizeShopSetup(
                            chestLocation,
                            player,
                            plugin.getPlayerInitialShopStockItem().get(player.getUniqueId())
                    );
                    // finalizeShopSetup zaten başarı mesajı veriyor.
                } else if (event.getCurrentItem().getType() == Material.RED_WOOL) {
                    player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumu iptal edildi.");
                    shopManager.cancelShopSetup(player.getUniqueId());
                }
                player.closeInventory();
            }
        }
    }

    private void handleItemSelectionGuiClickLogic(InventoryClickEvent event, Player player, Inventory guiInventory) {
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.getType() != Material.AIR) {
                    if (guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT) == null || guiInventory.getItem(ITEM_SELECT_PLACEMENT_SLOT).getType() == Material.AIR) {
                        guiInventory.setItem(ITEM_SELECT_PLACEMENT_SLOT, itemToMove.clone());
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
            if (event.getRawSlot() == ITEM_SELECT_PLACEMENT_SLOT) { // Sadece yerleştirme slotuyla etkileşime izin ver
                event.setCancelled(false);
            } else {
                event.setCancelled(true); // Diğer GUI slotlarına tıklamayı engelle
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
            if (player != null) plugin.getShopManager().cancelShopSetup(player.getUniqueId());
            return;
        }
        ItemStack templateItem = pendingShop.getTemplateItemStack();

        if (event.getRawSlot() == CONFIRM_BUTTON_SLOT_QUANTITY && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_WOOL) {
            event.setCancelled(true);
            ItemStack quantityItemStack = guiInventory.getItem(QUANTITY_PLACEMENT_SLOT);
            if (quantityItemStack != null && quantityItemStack.isSimilar(templateItem) && quantityItemStack.getAmount() > 0) {
                pendingShop.setItemQuantityForPrice(quantityItemStack.getAmount());
                shopSetupGUIManager.openPriceInputPrompt(player, pendingShop);
            } else {
                player.sendMessage(ChatColor.RED + "Lütfen miktar yuvasına doğru türde (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") ve geçerli miktarda eşya koyun.");
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(player.getOpenInventory().getBottomInventory())) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove != null && itemToMove.isSimilar(templateItem)) {
                    ItemStack currentItemInSlot = guiInventory.getItem(QUANTITY_PLACEMENT_SLOT);
                    if (currentItemInSlot == null || currentItemInSlot.getType() == Material.AIR) {
                        guiInventory.setItem(QUANTITY_PLACEMENT_SLOT, itemToMove.clone());
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
                    player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                }
            } else {
                event.setCancelled(false);
            }
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(guiInventory)) {
            if (event.getRawSlot() == QUANTITY_PLACEMENT_SLOT) {
                ItemStack cursorItem = event.getCursor();
                if (event.getAction().name().startsWith("PLACE_")) {
                    if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                        if (cursorItem.isSimilar(templateItem)) {
                            event.setCancelled(false);
                        } else {
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya koyabilirsiniz!");
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(false);
                    }
                } else if (event.getAction().name().startsWith("PICKUP_") || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && cursorItem != null && cursorItem.getType() != Material.AIR && !cursorItem.isSimilar(templateItem)) {
                        player.sendMessage(ChatColor.RED + "Bu yuvayla sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(templateItem) + ChatColor.RED + ") türünde eşya değiştirebilirsiniz!");
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false);
                    }
                } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(false);
                }
                else event.setCancelled(true);
            }
            else if (event.getRawSlot() != CONFIRM_BUTTON_SLOT_QUANTITY) { // Onay butonu hariç diğer slotlar
                event.setCancelled(true);
            }
        } else {
            if(clickedInventory != null) event.setCancelled(true); // GUI dışı ama null olmayan tıklamaları engelle
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Component viewTitleComponent = event.getView().title();
        UUID playerId = player.getUniqueId();

        Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
        if (chestLocation == null) {
            return;
        }

        Shop pendingShop = shopManager.getPendingShop(chestLocation);
        if (pendingShop == null) {
            plugin.getPlayerShopSetupState().remove(playerId);
            // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
            plugin.getPlayerWaitingForSetupInput().remove(playerId);
            plugin.getPlayerInitialShopStockItem().remove(playerId);
            return;
        }

        // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
        boolean isStillInSetupChain = plugin.getPlayerWaitingForSetupInput().containsKey(playerId);

        if (viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE)) {
            ItemStack itemInSlot = event.getInventory().getItem(ITEM_SELECT_PLACEMENT_SLOT);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                pendingShop.setTemplateItemStack(itemInSlot.clone());
                if (pendingShop.getShopType() != null && pendingShop.getShopType() != ShopType.PLAYER_BUY_SHOP) {
                    plugin.getPlayerInitialShopStockItem().put(playerId, itemInSlot.clone());
                    player.sendMessage(ChatColor.GREEN + "Şablon eşya ayarlandı: " + ChatColor.AQUA + getItemNameForMessages(itemInSlot) +
                            ChatColor.GREEN + ". Dükkanınız bu eşyayı satacaksa bu başlangıç stoğunuz olacak.");
                } else {
                    plugin.getPlayerInitialShopStockItem().remove(playerId);
                    player.sendMessage(ChatColor.GREEN + "Şablon eşya ayarlandı: " + ChatColor.AQUA + getItemNameForMessages(itemInSlot) +
                            ChatColor.GREEN + ". (Sadece Satın Alma Dükkanları için başlangıç stoğu uygulanmaz.)");
                }
                // event.getInventory().setItem(ITEM_SELECT_PLACEMENT_SLOT, null); // Slotu temizle
                final Shop finalPendingShop = pendingShop;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && plugin.getPlayerShopSetupState().containsKey(playerId)) {
                            shopSetupGUIManager.openQuantityInputMenu(player, finalPendingShop);
                        }
                    }
                }.runTask(plugin);
                isStillInSetupChain = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "Eşya seçilmedi, mağaza kurulumu iptal ediliyor.");
                shopManager.cancelShopSetup(playerId);
            }
        } else if (viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE)) {
            if (pendingShop.getBundleAmount() <= 0) {
                ItemStack itemInQuantitySlot = event.getInventory().getItem(QUANTITY_PLACEMENT_SLOT);
                if (itemInQuantitySlot != null && itemInQuantitySlot.getType() != Material.AIR) {
                    player.getInventory().addItem(itemInQuantitySlot.clone());
                    player.sendMessage(ChatColor.YELLOW + "Miktar belirlenmedi, miktar yuvasındaki eşyalar iade edildi.");
                }
                ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);
                if (initialStock != null && initialStock.getType() != Material.AIR) {
                    player.getInventory().addItem(initialStock.clone());
                    player.sendMessage(ChatColor.YELLOW + "Şablon eşyanız (" + ChatColor.AQUA + getItemNameForMessages(initialStock) + ChatColor.YELLOW + ") envanterinize iade edildi.");
                }
                player.sendMessage(ChatColor.YELLOW + "Miktar belirlenmedi, mağaza kurulumu iptal ediliyor.");
                shopManager.cancelShopSetup(playerId);
            } else {
                // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
                isStillInSetupChain = plugin.getPlayerWaitingForSetupInput().get(playerId) == ShopSetupGUIManager.InputType.PRICE;
            }
        }
        else if (viewTitleComponent.equals(ShopSetupGUIManager.SHOP_TYPE_TITLE)) {
            if (pendingShop.getShopType() == null) {
                player.sendMessage(ChatColor.YELLOW + "Mağaza türü seçilmedi, kurulum iptal ediliyor.");
                shopManager.cancelShopSetup(playerId);
            } else {
                isStillInSetupChain = true;
            }
        }

        if (!isStillInSetupChain && plugin.getPlayerShopSetupState().containsKey(playerId)) {
            shopManager.cancelShopSetup(playerId);
            player.sendMessage(ChatColor.YELLOW + "Mağaza kurulumu tamamlanmadığı için iptal edildi.");
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        Component viewTitleComponent = event.getView().title();
        boolean isItemSelectGui = viewTitleComponent.equals(ShopSetupGUIManager.ITEM_SELECT_TITLE);
        boolean isQuantityGui = viewTitleComponent.equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE);

        if (isItemSelectGui || isQuantityGui) {
            int targetPlacementSlot = isItemSelectGui ? ITEM_SELECT_PLACEMENT_SLOT : (isQuantityGui ? QUANTITY_PLACEMENT_SLOT : -1);

            boolean affectsOnlyPlacementSlot = true;
            if (targetPlacementSlot != -1) {
                for (int rawSlot : event.getRawSlots()) {
                    if (rawSlot < topInventory.getSize()) {
                        if (rawSlot != targetPlacementSlot) {
                            affectsOnlyPlacementSlot = false;
                            break;
                        }
                    }
                }
            } else {
                affectsOnlyPlacementSlot = false;
            }

            if (!affectsOnlyPlacementSlot) {
                event.setCancelled(true);
            } else {
                if (isQuantityGui) {
                    Player player = (Player) event.getWhoClicked();
                    Shop pendingShop = shopManager.getPendingShop(plugin.getPlayerShopSetupState().get(player.getUniqueId()));
                    if (pendingShop != null && pendingShop.getTemplateItemStack() != null) {
                        ItemStack draggedItem = event.getOldCursor();
                        if (draggedItem != null && draggedItem.getType() != Material.AIR && !draggedItem.isSimilar(pendingShop.getTemplateItemStack())) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "Bu yuvaya sadece şablondaki (" + ChatColor.AQUA + getItemNameForMessages(pendingShop.getTemplateItemStack()) + ChatColor.RED + ") türünde eşya sürükleyebilirsiniz!");
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

        if (plugin.getPlayerWaitingForAdminInput().containsKey(playerId)) {
            event.setCancelled(true);
            ShopAdminGUIManager.AdminInputType inputType = plugin.getPlayerWaitingForAdminInput().get(playerId);
            Location shopLocation = plugin.getPlayerAdministeringShop().get(playerId);
            String message = event.getMessage();

            if (shopLocation == null) {
                player.sendMessage(ChatColor.RED + "Hata: Yönetilen dükkan bulunamadı.");
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId);
                return;
            }

            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                plugin.getPlayerWaitingForAdminInput().remove(playerId);
                plugin.getPlayerAdministeringShop().remove(playerId);
                player.sendMessage(ChatColor.YELLOW + "Dükkan ayarı iptal edildi.");
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    Shop shop = shopManager.getActiveShop(shopLocation);
                    if (shop == null || !shop.getOwnerUUID().equals(playerId)) {
                        player.sendMessage(ChatColor.RED + "Hata: Dükkan bulunamadı veya yönetme yetkiniz yok.");
                        plugin.getPlayerWaitingForAdminInput().remove(playerId);
                        plugin.getPlayerAdministeringShop().remove(playerId);
                        return;
                    }

                    boolean actionSuccess = false;
                    if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME) {
                        int maxNameLength = plugin.getConfig().getInt("shop.max_display_name_length", 30);
                        if (message.length() > 0 && message.length() <= maxNameLength) {
                            shop.setShopDisplayName(ChatColor.translateAlternateColorCodes('&', message));
                            actionSuccess = true;
                            player.sendMessage(ChatColor.GREEN + "Dükkan adı: " + ChatColor.RESET + shop.getShopDisplayName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Ad 1-" + maxNameLength + " karakter olmalı. Tekrar dene veya 'iptal' yaz.");
                        }
                    } else if (inputType == ShopAdminGUIManager.AdminInputType.SHOP_PRICE) {
                        try {
                            double newPrice = Double.parseDouble(message);
                            if (newPrice >= 0) {
                                shop.setBuyPrice(newPrice); // Veya shop.setPrice(newPrice) eğer tek fiyat varsa
                                actionSuccess = true;
                                player.sendMessage(ChatColor.GREEN + "Dükkan paket fiyatı: " + ChatColor.GOLD + String.format("%.2f", newPrice));
                            } else {
                                player.sendMessage(ChatColor.RED + "Fiyat negatif olamaz. Tekrar dene veya 'iptal' yaz.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz fiyat. Sayı girin (örn: 10.5) veya 'iptal' yaz.");
                        }
                    }

                    if (actionSuccess) {
                        shopManager.saveShop(shop);
                        plugin.getPlayerWaitingForAdminInput().remove(playerId);
                        plugin.getPlayerAdministeringShop().remove(playerId);
                    }
                }
            }.runTask(plugin);
            return;
        }

        // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
        if (plugin.getPlayerShopSetupState().containsKey(playerId) &&
                plugin.getPlayerWaitingForSetupInput().get(playerId) == ShopSetupGUIManager.InputType.PRICE) {
            event.setCancelled(true);
            Location chestLocation = plugin.getPlayerShopSetupState().get(playerId);
            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            String message = event.getMessage();

            if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getBundleAmount() <= 0) {
                player.sendMessage(ChatColor.RED + "Mağaza kurulum hatası (eksik bilgi). Baştan başla.");
                shopManager.cancelShopSetup(playerId);
                return;
            }

            if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
                shopManager.cancelShopSetup(playerId);
                player.sendMessage(ChatColor.YELLOW + "Fiyat girişi ve dükkan kurulumu iptal edildi.");
                return;
            }

            try {
                String[] priceParts = message.split(":");
                if (priceParts.length != 2) {
                    player.sendMessage(ChatColor.RED + "Geçersiz format. Fiyatları ALIS_FIYATI:SATIS_FIYATI şeklinde girin (örn: 100:80 veya 50:-1). 'iptal' yazarak iptal edin.");
                    return;
                }

                double buyPrice = Double.parseDouble(priceParts[0].trim());
                double sellPrice = Double.parseDouble(priceParts[1].trim());

                if ((buyPrice < 0 && buyPrice != -1) || (sellPrice < 0 && sellPrice != -1)) {
                    player.sendMessage(ChatColor.RED + "Geçersiz fiyatlar. Fiyatlar pozitif olmalı veya -1 (devre dışı) olmalı. Tekrar deneyin veya 'iptal' yazın.");
                    return;
                }
                if (buyPrice == -1 && sellPrice == -1 && pendingShop.getShopMode() != null ) {
                    player.sendMessage(ChatColor.RED + "Bir dükkan ya satış yapmalı ya da alış yapmalı (veya ikisi de). İki fiyatı da -1 yapamazsınız. Tekrar deneyin veya 'iptal' yazın.");
                    return;
                }

                pendingShop.setBuyPrice(buyPrice);
                pendingShop.setSellPrice(sellPrice);

                player.sendMessage(ChatColor.GREEN + "Fiyatlar kabul edildi. Oyuncu Alış Fiyatı: " +
                        (buyPrice == -1 ? ChatColor.GRAY + "Devre Dışı" : ChatColor.GOLD + String.format("%.2f", buyPrice)) +
                        ChatColor.GREEN + ", Oyuncu Satış Fiyatı: " +
                        (sellPrice == -1 ? ChatColor.GRAY + "Devre Dışı" : ChatColor.GOLD + String.format("%.2f", sellPrice)));

                plugin.getPlayerShopSetupState().remove(playerId);
                // **** DÜZELTME: getPlayerWaitingForInput yerine getPlayerWaitingForSetupInput kullanıldı ****
                plugin.getPlayerWaitingForSetupInput().remove(playerId);
                ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);

                if (buyPrice == -1 && initialStock != null) {
                    player.getInventory().addItem(initialStock.clone());
                    player.sendMessage(ChatColor.YELLOW + "Dükkanınız satış yapmayacağı için başlangıç stoğunuz iade edildi.");
                    initialStock = null;
                }

                final ItemStack finalInitialStock = initialStock;
                final Shop finalPendingShop = pendingShop;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        shopManager.finalizeShopSetup(
                                chestLocation,
                                player,
                                finalInitialStock
                        );
                    }
                }.runTask(plugin);

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Fiyatlar için geçersiz sayı formatı. Örn: 10.5 veya -1. Tekrar deneyin veya 'iptal' yazın.");
            }
        }
    }

    private String getItemNameForMessages(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen Eşya";
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        String name = itemStack.getType().toString().toLowerCase().replace("_", " ");
        if (!name.isEmpty()) {
            String[] parts = name.split(" ");
            StringBuilder capitalizedName = new StringBuilder();
            for (String part : parts) {
                if (part.length() > 0) {
                    capitalizedName.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase()).append(" ");
                }
            }
            return capitalizedName.toString().trim();
        }
        return "Bilinmeyen Eşya";
    }
}