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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID; // Added import

import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopType; // Keep for old finalizeShopSetup if needed by other parts, though ideally it's removed

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
        // event.setCancelled(true); // Already done by previous logic if it's an existing shop of theirs

        // Check if player is already in a shop creation process
        if (plugin.getPlayerShopSetupState().containsKey(player.getUniqueId()) || plugin.getPlayerChoosingShopMode().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are already in a shop creation process. Type 'cancel' to abort.");
            event.setCancelled(true);
            return;
        }

        // Permission Check
        if (!player.hasPermission("skyblock.shop.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create shops.");
            event.setCancelled(true);
            return;
        }

        // Location Check (Island Ownership/Membership)
        Island island = plugin.getIslandDataHandler().getIslandAt(chestLocation);
        boolean canCreateHere = false;
        if (island != null && (island.isOwner(player.getUniqueId()) || island.isMember(player.getUniqueId()))) {
            canCreateHere = true;
        }
        if (player.hasPermission("skyblock.admin.createshopanywhere")) {
            canCreateHere = true;
        }

        if (!canCreateHere) {
            player.sendMessage(ChatColor.RED + "You can only create shops on your island or an island you are a member of.");
            event.setCancelled(true);
            return;
        }

        // Shop Existence Check (Handles both pending and active)
        Shop existingShop = shopManager.getActiveShop(chestLocation);
        if (existingShop == null) {
            existingShop = shopManager.getPendingShop(chestLocation);
        }

        if (existingShop != null) {
            if (existingShop.getOwnerUUID().equals(player.getUniqueId())) {
                if (existingShop.isSetupComplete()) {
                    // It's their own active shop, open admin menu
                    shopAdminGUIManager.openAdminMenu(player, existingShop);
                } else {
                    // It's their own pending shop, let them continue setup (e.g., by opening item selection)
                    player.sendMessage(ChatColor.YELLOW + "Resuming setup for this pending shop.");
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation); // Ensure state for GUI listener
                    shopSetupGUIManager.openItemSelectionMenu(player, existingShop);
                }
            } else {
                player.sendMessage(ChatColor.RED + "A shop owned by " + Bukkit.getOfflinePlayer(existingShop.getOwnerUUID()).getName() + " already exists here.");
            }
            event.setCancelled(true);
            return;
        }

        // Initiate Mode Choice if no shop exists
        plugin.getPlayerChoosingShopMode().put(player.getUniqueId(), chestLocation);
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Choose a shop mode for this chest location:");
        player.sendMessage(ChatColor.GREEN + "Type 'market'" + ChatColor.GRAY + " - Players open chest, select custom amount.");
        player.sendMessage(ChatColor.GREEN + "Type 'bank'" + ChatColor.GRAY + "   - Players click sign/chest to buy fixed bundles.");
        player.sendMessage(ChatColor.RED + "Type 'cancel'" + ChatColor.GRAY + " - Abort shop creation.");
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");
        event.setCancelled(true);

    } else { // Normal Right Click (not sneaking)
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
                event.setCancelled(true); // Prevent normal chest opening
                // If a shop is pending, direct them to the next step of setup
                // This depends on how `ShopSetupGUIManager` handles resuming pending shops.
                // For now, let's assume it might try to restart or guide them.
                // The current setup flow via chat mode selection might make this part less relevant
                // if `getPlayerShopSetupState` is primarily for GUI driven setup.
                // However, if a player logs out mid-setup, this could be a resume point.
                // The new chat-based setup doesn't use `getPlayerShopSetupState` in the same way.
                // It uses `getPlayerChoosingShopMode` first.
                // We might need to re-evaluate how to resume a pending shop setup
                // if it was initiated via chat but not completed.
                // For now, trying to open the item selection menu might be a good default.
                if (plugin.getShopSetupGUIManager() != null) {
                    plugin.getShopSetupGUIManager().openItemSelectionMenu(player, pendingShop);
                    player.sendMessage(ChatColor.YELLOW + "Resuming shop setup: Please select an item.");
                } else {
                    player.sendMessage(ChatColor.RED + "Error: Shop setup GUI is unavailable.");
                }
            }
            // else: Not a shop, or not owner's pending shop: normal chest interaction.
        }
    }
}

@EventHandler
public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();

    if (plugin.getPlayerChoosingShopMode().containsKey(playerId)) {
        event.setCancelled(true);
        Location chestLocation = plugin.getPlayerChoosingShopMode().get(playerId);
        String message = event.getMessage().toLowerCase().trim();
        ShopMode selectedMode = null;

        if (message.equals("market")) {
            selectedMode = ShopMode.MARKET_CHEST;
        } else if (message.equals("bank")) {
            selectedMode = ShopMode.BANK_CHEST;
        } else if (message.equals("cancel")) {
            plugin.getPlayerChoosingShopMode().remove(playerId);
            player.sendMessage(ChatColor.YELLOW + "Shop creation cancelled.");
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid choice. Type 'market', 'bank', or 'cancel'.");
            return;
        }

        final ShopMode finalSelectedMode = selectedMode;
        plugin.getPlayerChoosingShopMode().remove(playerId); // Remove before task to prevent re-entry

        new BukkitRunnable() {
            @Override
            public void run() {
                // Re-check if shop exists, as it might have been created by another process/admin
                // between chat input and this runnable execution.
                if (plugin.getShopManager().isShop(chestLocation)) {
                    player.sendMessage(ChatColor.RED + "A shop was created at this location while you were choosing. Please try again.");
                    return;
                }

                Shop newShop = plugin.getShopManager().initiateShopCreation(chestLocation, player, finalSelectedMode);
                if (newShop != null) {
                    if (plugin.getShopSetupGUIManager() != null) {
                        // The player is now in the item selection phase.
                        // The ShopSetupListener handles the rest of the GUI flow from here.
                        // We also need to put them into the general shop setup state.
                        plugin.getPlayerShopSetupState().put(playerId, chestLocation);
                        plugin.getShopSetupGUIManager().openItemSelectionMenu(player, newShop);
                        player.sendMessage(ChatColor.GREEN + "Shop mode '" + finalSelectedMode.name() + "' selected. Now, please select the item for your shop.");
                    } else {
                        plugin.getLogger().severe("ShopSetupGUIManager is null! Cannot open item selection menu.");
                        player.sendMessage(ChatColor.RED + "Error: Shop setup GUI could not be opened. Please contact an admin.");
                    }
                } else {
                    // initiateShopCreation should send a message if it fails,
                    // but we can add a generic one here if it doesn't.
                    player.sendMessage(ChatColor.RED + "Failed to initiate shop creation. Please ensure the location is valid and try again.");
                }
            }
        }.runTask(plugin);
    }
    // Other chat handlers for other states (like price input) are in ShopSetupListener
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