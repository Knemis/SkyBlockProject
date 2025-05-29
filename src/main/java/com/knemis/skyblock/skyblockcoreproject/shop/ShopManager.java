// com/knemis/skyblock/skyblockcoreproject/shop/ShopManager.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession; // Import ShopSetupSession
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ShopManager {

    private final SkyBlockProject plugin;
    private final ShopStorage shopStorage;
    private final Map<Location, Shop> activeShops;
    private final Map<Location, Shop> pendingShops;
    private final ShopSignManager shopSignManager; 
    private final ShopTransactionManager shopTransactionManager; // New field

    public ShopManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopStorage = new ShopStorage(plugin);
        this.activeShops = this.shopStorage.loadShops();
        this.pendingShops = new HashMap<>();
        this.shopSignManager = new ShopSignManager(plugin); 
        this.shopTransactionManager = new ShopTransactionManager(plugin, this); // Initialize ShopTransactionManager
        if (this.activeShops != null) {
            plugin.getLogger().info(String.format("[ShopManager] Initialized with %d active shops from storage.", this.activeShops.size()));
        } else {
            plugin.getLogger().severe("[ShopManager] Initialization failed: activeShops is null after attempting to load from ShopStorage.");
        }
    }

    // Getter for ShopSignManager to be used by ShopTransactionManager
    public ShopSignManager getShopSignManager() {
        return shopSignManager;
    }

    public Shop initiateShopCreation(Location location, Player player, ShopMode initialShopMode) {
        System.out.println("[TRACE] In ShopManager.initiateShopCreation for player " + (player != null ? player.getName() : "null") + " at " + Shop.locationToString(location) + " with mode " + initialShopMode);
        String locStr = Shop.locationToString(location);
        plugin.getLogger().info(String.format("[ShopManager] Attempting to initiate shop creation at %s for player %s (UUID: %s) with mode %s.",
                locStr, player.getName(), player.getUniqueId(), initialShopMode));

        if (location == null || player == null) {
            plugin.getLogger().warning(String.format("[ShopManager] initiateShopCreation failed: Location (%s) or player (%s) is null.", locStr, player));
            return null;
        }
        if (isShop(location)) {
            Shop existing = getActiveShop(location);
            if (existing == null) existing = getPendingShop(location); // Check pending too
            if (existing != null) {
                if (existing.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Bu sandıkta zaten bir kurulumunuz veya aktif bir dükkanınız var.");
                    plugin.getLogger().info(String.format("[ShopManager] Player %s attempting to re-initiate shop at %s, which they already own (State: %s). Returning existing.",
                            player.getName(), locStr, (existing.isSetupComplete() ? "ACTIVE" : "PENDING")));
                    return existing; // Allow resuming setup or managing existing shop.
                } else {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(existing.getOwnerUUID());
                    String ownerName = owner.getName() != null ? owner.getName() : existing.getOwnerUUID().toString();
                    player.sendMessage(ChatColor.RED + "Bu sandık zaten başkası ("+ownerName+") tarafından dükkan olarak kullanılıyor.");
                    plugin.getLogger().warning(String.format("[ShopManager] Player %s failed to initiate shop at %s: Already a shop owned by %s.",
                            player.getName(), locStr, ownerName));
                    return null;
                }
            }
        }
        Shop newShop = new Shop(location, player.getUniqueId(), initialShopMode);
        pendingShops.put(location, newShop);
        plugin.getLogger().info(String.format("[ShopManager] Pending shop initiated successfully: Location %s for owner %s (UUID: %s) with mode %s. Shop ID for internal tracking: %s.",
                locStr, player.getName(), player.getUniqueId(), initialShopMode, newShop.getShopId()));
        return newShop;
    }

    public Shop getPendingShop(Location location) {
        if (location == null) return null;
        return pendingShops.get(location);
    }

    public Shop getPendingShop(UUID playerId) {
        // This specific override might become less relevant if all pending shop access goes via session.
        // For now, it can try to get from session if ShopSetupGUIManager is available.
        if (plugin.getShopSetupGUIManager() != null) {
            ShopSetupSession session = plugin.getShopSetupGUIManager().getPlayerSession(playerId);
            if (session != null) {
                return session.getPendingShop();
            }
        }
        // Fallback or alternative logic if no session (e.g. if there's a way to have pending shops outside sessions)
        // This part depends on whether all pending shops are exclusively managed by sessions.
        // For this refactor, we assume sessions are the primary way for setup.
        // If direct access to pendingShops by playerId is needed elsewhere without a session,
        // that implies a different state management path.
        plugin.getLogger().fine("getPendingShop(UUID) called but no session found for player " + playerId + ". This might be okay if not in active setup.");
        return null; // Or look up in pendingShops via a custom map if that's a separate flow.
    }

    public void finalizeShopSetup(UUID playerId) {
        ShopSetupGUIManager shopSetupGUIManager = plugin.getShopSetupGUIManager();
        if (shopSetupGUIManager == null) {
            plugin.getLogger().severe("[ShopManager] finalizeShopSetup called but ShopSetupGUIManager is null!");
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage(ChatColor.RED + "Kritik bir hata oluştu (GUI Manager eksik). Kurulum tamamlanamadı.");
            return;
        }

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);
        if (session == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed: No setup session found for player %s.", playerId));
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage(ChatColor.RED + "Dükkan kurulum oturumu bulunamadı. Lütfen baştan başlayın.");
            return;
        }

        Player actor = Bukkit.getPlayer(playerId);
        if (actor == null || !actor.isOnline()) {
            plugin.getLogger().severe(String.format("[ShopManager] finalizeShopSetup failed: Player %s is not online!", playerId));
            shopSetupGUIManager.removeSession(playerId); // Clean up session
            return;
        }

        Shop pendingShop = session.getPendingShop();
        Location location = session.getChestLocation();
        ItemStack initialStockItem = session.getInitialStockItem(); // Get from session
        String locStr = Shop.locationToString(location);

        plugin.getLogger().info(String.format("[ShopManager] Attempting to finalize shop setup at %s for player %s (UUID: %s) via session. Initial stock: %s",
                locStr, actor.getName(), playerId, initialStockItem != null ? initialStockItem.toString() : "null"));

        if (pendingShop == null) { // Should be caught by session check, but good to have
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed: No pending shop in session for player %s at %s.", actor.getName(), locStr));
            actor.sendMessage(ChatColor.RED + "Dükkan kurulum bilgisi oturumda bulunamadı.");
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }
        
        // Ensure the pendingShop from the session is also removed from the direct pendingShops map if it was added there.
        // This covers cases where initiateShopCreation might have added to pendingShops map before session was fully utilized.
        pendingShops.remove(location);


        if (pendingShop.getShopMode() == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Shop mode not set.", locStr, actor.getName()));
            actor.sendMessage(ChatColor.RED + "Dükkan modu seçilmedi! Kurulum iptal edildi.");
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        ItemStack templateItem = pendingShop.getTemplateItemStack();
        if (templateItem == null || templateItem.getType() == Material.AIR) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Template item not set.", locStr, actor.getName()));
            actor.sendMessage(ChatColor.RED + "Satılacak/alınacak eşya ayarlanmadı! Kurulum iptal edildi.");
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        if (pendingShop.getBundleAmount() <= 0) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Invalid bundle amount %d.",
                    locStr, actor.getName(), pendingShop.getBundleAmount()));
            actor.sendMessage(ChatColor.RED + "İşlem için geçersiz paket miktarı! Kurulum iptal edildi.");
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        boolean hasValidBuyPrice = pendingShop.getBuyPrice() >= 0;
        boolean hasValidSellPrice = pendingShop.getSellPrice() >= 0;

        if (!hasValidBuyPrice && !hasValidSellPrice) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Neither buy nor sell price is valid. Buy: %.2f, Sell: %.2f",
                    locStr, actor.getName(), pendingShop.getBuyPrice(), pendingShop.getSellPrice()));
            actor.sendMessage(ChatColor.RED + "Alış veya satış için geçerli bir fiyat belirlenmedi! Kurulum iptal edildi.");
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        pendingShop.setSetupComplete(true);
        saveShop(pendingShop); 

        boolean needsStocking = initialStockItem != null && initialStockItem.getType() != Material.AIR;
        boolean canBeStockedByPlayerBuying = pendingShop.getBuyPrice() != -1; // Shop sells to player (player buys)

        if (needsStocking && canBeStockedByPlayerBuying) {
            Block shopBlock = location.getBlock();
            if (shopBlock.getState() instanceof Chest) {
                Chest chest = (Chest) shopBlock.getState();
                Inventory chestInventory = chest.getInventory();
                chestInventory.clear(); // Clear any items that might have been left from quantity GUI
                chestInventory.addItem(initialStockItem.clone());
                plugin.getLogger().info(String.format("[ShopManager] Initial stock (%s) added to shop %s at %s.",
                        initialStockItem.toString(), Shop.locationToString(pendingShop.getLocation()), locStr));
            } else {
                plugin.getLogger().severe(String.format("[ShopManager] Shop block at %s is not a Chest for shop at %s. Initial stock not added.", locStr, Shop.locationToString(pendingShop.getLocation())));
            }
        } else if (needsStocking && !canBeStockedByPlayerBuying) {
            plugin.getLogger().info(String.format("[ShopManager] Initial stock for shop at %s (%s) not added as shop is sell-only (buyPrice is -1).", locStr, Shop.locationToString(pendingShop.getLocation())));
        }

        plugin.getLogger().info(String.format("[ShopManager] Shop setup finalized: Location %s by %s. Item: %s, QtyPerBundle: %d, BuyPrice: %.2f, SellPrice: %.2f, Mode: %s. Stocked: %b",
                locStr, actor.getName(), templateItem.getType(), pendingShop.getBundleAmount(),
                pendingShop.getBuyPrice(), pendingShop.getSellPrice(), pendingShop.getShopMode(), (needsStocking && canBeStockedByPlayerBuying)));
        
        shopSetupGUIManager.removeSession(playerId); // Clean up session after successful finalization
        actor.sendMessage(ChatColor.GREEN + "Dükkanınız başarıyla kuruldu!");
    }

    public void saveShop(Shop shop) {
        // System.out.println("[TRACE] In ShopManager.saveShop for shop at " + (shop != null ? Shop.locationToString(shop.getLocation()) : "null"));
        if (shop == null || shop.getLocation() == null) {
            plugin.getLogger().warning("[ShopManager] saveShop called but shop or its location is null. Shop object: " + shop);
            return;
        }
        activeShops.put(shop.getLocation(), shop);
        shopStorage.saveShop(shop);
        this.shopSignManager.updateAttachedSign(shop, this.getCurrencySymbol()); // Use ShopSignManager
        plugin.getLogger().info(String.format("[ShopManager] Shop at %s (Owner: %s) saved/updated in activeShops and persistent storage requested.",
                 Shop.locationToString(shop.getLocation()), shop.getOwnerUUID()));
    }

    public void cancelShopSetup(UUID playerId) {
        // System.out.println("[TRACE] In ShopManager.cancelShopSetup for player UUID " + playerId);
        if (playerId == null) {
            plugin.getLogger().warning("[ShopManager] cancelShopSetup called with null playerId.");
            return;
        }

        ShopSetupGUIManager shopSetupGUIManager = plugin.getShopSetupGUIManager();
        if (shopSetupGUIManager == null) {
            plugin.getLogger().severe("[ShopManager] cancelShopSetup called but ShopSetupGUIManager is null! Cannot remove session for " + playerId);
            // Try to remove from pendingShops directly if possible, though this indicates a larger issue.
            // This path should ideally not be hit if GUIManager is always available.
            return;
        }

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);
        Location chestLocation = null;
        ItemStack initialStock = null;
        UUID ownerIdFromSession = null;

        if (session != null) {
            chestLocation = session.getChestLocation();
            initialStock = session.getInitialStockItem();
            if(session.getPendingShop() != null) {
                 ownerIdFromSession = session.getPendingShop().getOwnerUUID();
            }
             // Remove session first
            shopSetupGUIManager.removeSession(playerId);
        } else {
            // If no session, attempt to retrieve from old maps as a fallback (though these should be removed later)
            // This part is for graceful transition. Once old maps are gone, this else might be an error or no-op.
            // chestLocation = plugin.getPlayerShopSetupState().remove(playerId);
            // plugin.getPlayerWaitingForSetupInput().remove(playerId);
            // initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);
             plugin.getLogger().warning("[ShopManager] cancelShopSetup called for player " + playerId + " but no active session found. Old map removal would occur here if they were still in use.");
        }
        
        String locStr = Shop.locationToString(chestLocation);

        if (chestLocation != null) {
            Shop pending = pendingShops.remove(chestLocation); // Remove from manager's direct pendingShops map
            if (pending != null) {
                 plugin.getLogger().info(String.format("[ShopManager] Shop setup cancelled by player %s for location %s. Pending shop (Owner: %s) removed from map.",
                    playerId, locStr, pending.getOwnerUUID()));
            } else if (ownerIdFromSession != null) {
                 plugin.getLogger().info(String.format("[ShopManager] Shop setup cancelled by player %s for location %s. No pending shop in map, but session existed (Owner from session: %s).",
                    playerId, locStr, ownerIdFromSession));
            } else {
                 plugin.getLogger().info(String.format("[ShopManager] Shop setup cancelled by player %s for location %s (from session). No pending shop in map or session owner info.", playerId, locStr));
            }
        } else {
            plugin.getLogger().info(String.format("[ShopManager] Shop setup cancellation for player %s (no specific location found in session or old state).", playerId));
        }

        if (initialStock != null && initialStock.getType() != Material.AIR) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(initialStock.clone());
                player.sendMessage(ChatColor.YELLOW + "Başlangıç için ayrılan eşya (" + ChatColor.AQUA + this.shopSignManager.getItemNameForMessages(initialStock, 15) + ChatColor.YELLOW + ") envanterinize iade edildi.");
                plugin.getLogger().info(String.format("[ShopManager] Initial stock %s returned to player %s after setup cancellation for location %s.",
                        initialStock.toString(), player.getName(), locStr));
            } else {
                plugin.getLogger().warning(String.format("[ShopManager] Could not return initial stock %s to player %s (offline or null) after setup cancellation for location %s.",
                        initialStock.toString(), playerId, locStr));
            }
        }
         // Ensure old maps are cleared if they were somehow still populated (transitional)
        // plugin.getPlayerShopSetupState().remove(playerId);
        // plugin.getPlayerWaitingForSetupInput().remove(playerId); 
        // plugin.getPlayerInitialShopStockItem().remove(playerId);
    }

    public boolean isActiveShop(Location location) {
        if (location == null) return false;
        Shop shop = activeShops.get(location);
        return shop != null && shop.isSetupComplete();
    }

    public boolean isShop(Location location) {
        if (location == null) return false;
        return activeShops.containsKey(location) || pendingShops.containsKey(location);
    }

    public Shop getActiveShop(Location location) {
        if (location == null) return null;
        return activeShops.get(location);
    }

    public Map<Location, Shop> getActiveShopsMap() {
        return activeShops;
    }

    public void removeShop(Location location, Player player) {
        System.out.println("[TRACE] In ShopManager.removeShop for player " + (player != null ? player.getName() : "null") + " at " + Shop.locationToString(location));
        if (location == null || player == null) return;
        Shop shopToRemove = getActiveShop(location);
        boolean wasPending = false;
        if (shopToRemove == null) {
            shopToRemove = getPendingShop(location);
            if (shopToRemove != null) wasPending = true;
        }
        if (shopToRemove == null) {
            player.sendMessage(ChatColor.RED + "Bu konumda kaldırılacak bir dükkan yok.");
            return;
        }
        if (!shopToRemove.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("skyblock.admin.removeshop")) {
            player.sendMessage(ChatColor.RED + "Bu dükkanı kaldırma yetkiniz yok.");
            return;
        }

        if (!wasPending) {
            activeShops.remove(location);
            shopStorage.removeShop(location);
            this.shopSignManager.clearAttachedSign(location); // Use ShopSignManager
            player.sendMessage(ChatColor.GREEN + "Dükkan başarıyla kaldırıldı.");
            plugin.getLogger().info("Aktif dükkan kaldırıldı (" + Shop.locationToString(location) + ") tarafından " + player.getName());
        } else {
            pendingShops.remove(location);
            player.sendMessage(ChatColor.GREEN + "Dükkan kurulumu başarıyla iptal edildi.");
            plugin.getLogger().info("Bekleyen dükkan kurulumu iptal edildi (" + Shop.locationToString(location) + ") tarafından " + player.getName());
            // If a pending shop is removed, also remove its session if it exists
            if (plugin.getShopSetupGUIManager() != null) {
                ShopSetupSession session = plugin.getShopSetupGUIManager().getPlayerSession(shopToRemove.getOwnerUUID());
                // Check if the session is for the shop being removed.
                if (session != null && session.getChestLocation() != null && session.getChestLocation().equals(location)) {
                    ItemStack initialStockFromSession = session.getInitialStockItem();
                    plugin.getShopSetupGUIManager().removeSession(shopToRemove.getOwnerUUID());
                    plugin.getLogger().info("Also removed active setup session for player " + shopToRemove.getOwnerUUID() + " as their pending shop at " + location + " was removed.");
                    if (initialStockFromSession != null && initialStockFromSession.getType() != Material.AIR && player.isOnline()) {
                        player.getInventory().addItem(initialStockFromSession.clone());
                        player.sendMessage(ChatColor.YELLOW + "Kurulumdaki eşyalarınız iade edildi.");
                    }
                }
            }
        }
    }

    // getItemNameForMessages is now in ShopSignManager
    // shortenFormattedString and shortenItemName are now in ShopSignManager
    // updateAttachedSign is now in ShopSignManager
    // findOrCreateAttachedSign is now in ShopSignManager
    // clearAttachedSign is now in ShopSignManager

    // The following methods were moved to ShopInventoryManager as static methods
    // hasEnoughSpace(Player player, ItemStack itemToReceive)
    // removeItemsFromChest(Chest chest, ItemStack templateItemToRemove, int amountToRemove)
    // countItemsInChest(Chest chest, ItemStack templateItemToMatch)
    // countItemsInInventory(Player player, ItemStack templateItem)
    // removeItemsFromInventory(Player player, ItemStack templateItem, int amountToRemove)
    // hasEnoughSpaceInChest(Chest chest, ItemStack itemToAdd, int quantityToAdd)

    public ShopStorage getShopStorage() {
        return this.shopStorage;
    }

    public List<Shop> getShopsByOwner(UUID ownerUUID) {
        if (ownerUUID == null) return new ArrayList<>();
        if (activeShops == null) {
            plugin.getLogger().warning("getShopsByOwner called but activeShops is null!");
            return new ArrayList<>();
        }
        return activeShops.values().stream()
                .filter(s -> s != null && s.getOwnerUUID().equals(ownerUUID))
                .collect(Collectors.toList());
    }

    public int getTotalActiveShops() {
        return activeShops != null ? activeShops.size() : 0;
    }

    public boolean executePurchase(Player buyer, Shop shop, int bundlesToBuy) {
        return this.shopTransactionManager.executePurchase(buyer, shop, bundlesToBuy);
    }

    public String getCurrencySymbol() {
        // Vault ekonomisi kurulmuş mu?
        if (EconomyManager.isEconomyAvailable()) {
            Economy vaultEco = Bukkit.getServicesManager()
                    .getRegistration(Economy.class)
                    .getProvider();
            // Vault API'de sembol yoksa singular name (ör. "Lira") dönebilir.
            // Gerçek bir sembol istiyorsanız config'ten okuyun.
            String name = vaultEco.currencyNameSingular();
            return (name != null && !name.isEmpty()) ? name :
                    plugin.getConfig().getString("economy.currency-symbol", "₺");
        }
        // Vault yoksa config'den oku, yoksa varsayılan "₺"
        return plugin.getConfig().getString("economy.currency-symbol", "₺");
    }

    public boolean executeSellToShop(Player seller, Shop shop, int bundlesToSell) {
        return this.shopTransactionManager.executeSellToShop(seller, shop, bundlesToSell);
    }
}