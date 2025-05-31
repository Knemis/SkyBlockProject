// com/knemis/skyblock/skyblockcoreproject/shop/ShopManager.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block; // Added import
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
    private final ShopTransactionManager shopTransactionManager;

    public ShopManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopStorage = new ShopStorage(plugin);
        this.activeShops = this.shopStorage.loadShops();
        this.pendingShops = new HashMap<>();
        this.shopSignManager = new ShopSignManager(plugin);
        this.shopTransactionManager = new ShopTransactionManager(plugin, this);
        if (this.activeShops != null) {
            plugin.getLogger().info(String.format("[ShopManager] Initialized with %d active shops from storage.", this.activeShops.size()));
        } else {
            plugin.getLogger().severe("[ShopManager] Initialization failed: activeShops is null after attempting to load from ShopStorage.");
        }
    }

    public ShopSignManager getShopSignManager() {
        return shopSignManager;
    }

    public Shop initiateShopCreation(Location location, Player player, ShopMode initialShopMode) {
        String locStr = Shop.locationToString(location);
        plugin.getLogger().info(String.format("[ShopManager] Attempting to initiate shop creation at %s for player %s (UUID: %s) with mode %s.",
                locStr, player.getName(), player.getUniqueId(), initialShopMode));

        if (location == null || player == null) {
            plugin.getLogger().warning(String.format("[ShopManager] initiateShopCreation failed: Location (%s) or player (%s) is null.", locStr, player));
            return null;
        }
        if (isShop(location)) {
            Shop existing = getActiveShop(location);
            if (existing == null) existing = getPendingShop(location);
            if (existing != null) {
                if (existing.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(Component.text("Bu sandıkta zaten bir kurulumunuz veya aktif bir dükkanınız var.", NamedTextColor.YELLOW));
                    plugin.getLogger().info(String.format("[ShopManager] Player %s attempting to re-initiate shop at %s, which they already own (State: %s). Returning existing.",
                            player.getName(), locStr, (existing.isSetupComplete() ? "ACTIVE" : "PENDING")));
                    return existing;
                } else {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(existing.getOwnerUUID());
                    String ownerName = owner.getName() != null ? owner.getName() : existing.getOwnerUUID().toString();
                    player.sendMessage(Component.text("Bu sandık zaten başkası ("+ownerName+") tarafından dükkan olarak kullanılıyor.", NamedTextColor.RED));
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
        if (plugin.getShopSetupGUIManager() != null) {
            ShopSetupSession session = plugin.getShopSetupGUIManager().getPlayerSession(playerId);
            if (session != null) {
                return session.getPendingShop();
            }
        }
        plugin.getLogger().fine("getPendingShop(UUID) called but no session found for player " + playerId + ". This might be okay if not in active setup.");
        return null;
    }

    public void finalizeShopSetup(UUID playerId) {
        ShopSetupGUIManager shopSetupGUIManager = plugin.getShopSetupGUIManager();
        if (shopSetupGUIManager == null) {
            plugin.getLogger().severe("[ShopManager] finalizeShopSetup called but ShopSetupGUIManager is null!");
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage(Component.text("Kritik bir hata oluştu (GUI Manager eksik). Kurulum tamamlanamadı.", NamedTextColor.RED));
            return;
        }

        ShopSetupSession session = shopSetupGUIManager.getPlayerSession(playerId);
        if (session == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed: No setup session found for player %s.", playerId));
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage(Component.text("Dükkan kurulum oturumu bulunamadı. Lütfen baştan başlayın.", NamedTextColor.RED));
            return;
        }

        Player actor = Bukkit.getPlayer(playerId);
        if (actor == null || !actor.isOnline()) {
            plugin.getLogger().severe(String.format("[ShopManager] finalizeShopSetup failed: Player %s is not online!", playerId));
            shopSetupGUIManager.removeSession(playerId);
            return;
        }

        Shop pendingShop = session.getPendingShop();
        Location location = session.getChestLocation();
        ItemStack initialStockItem = session.getInitialStockItem();
        String locStr = Shop.locationToString(location);

        plugin.getLogger().info(String.format("[ShopManager] Attempting to finalize shop setup at %s for player %s (UUID: %s) via session. Initial stock: %s",
                locStr, actor.getName(), playerId, initialStockItem != null ? initialStockItem.toString() : "null"));

        if (pendingShop == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed: No pending shop in session for player %s at %s.", actor.getName(), locStr));
            actor.sendMessage(Component.text("Dükkan kurulum bilgisi oturumda bulunamadı.", NamedTextColor.RED));
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }
        
        pendingShops.remove(location);

        if (pendingShop.getShopMode() == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Shop mode not set.", locStr, actor.getName()));
            actor.sendMessage(Component.text("Dükkan modu seçilmedi! Kurulum iptal edildi.", NamedTextColor.RED));
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        ItemStack templateItem = pendingShop.getTemplateItemStack();
        if (templateItem == null || templateItem.getType() == Material.AIR) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Template item not set.", locStr, actor.getName()));
            actor.sendMessage(Component.text("Satılacak/alınacak eşya ayarlanmadı! Kurulum iptal edildi.", NamedTextColor.RED));
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        if (pendingShop.getBundleAmount() <= 0) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Invalid bundle amount %d.",
                    locStr, actor.getName(), pendingShop.getBundleAmount()));
            actor.sendMessage(Component.text("İşlem için geçersiz paket miktarı! Kurulum iptal edildi.", NamedTextColor.RED));
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        boolean hasValidBuyPrice = pendingShop.getBuyPrice() >= 0;
        boolean hasValidSellPrice = pendingShop.getSellPrice() >= 0;

        if (!hasValidBuyPrice && !hasValidSellPrice) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Neither buy nor sell price is valid. Buy: %.2f, Sell: %.2f",
                    locStr, actor.getName(), pendingShop.getBuyPrice(), pendingShop.getSellPrice()));
            actor.sendMessage(Component.text("Alış veya satış için geçerli bir fiyat belirlenmedi! Kurulum iptal edildi.", NamedTextColor.RED));
            shopSetupGUIManager.removeSession(playerId);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        pendingShop.setSetupComplete(true);
        saveShop(pendingShop); 

        boolean needsStocking = initialStockItem != null && initialStockItem.getType() != Material.AIR;
        boolean canBeStockedByPlayerBuying = pendingShop.getBuyPrice() != -1;

        if (needsStocking && canBeStockedByPlayerBuying) {
            Block shopBlock = location.getBlock();
            if (shopBlock.getState() instanceof Chest) {
                Chest chest = (Chest) shopBlock.getState();
                Inventory chestInventory = chest.getInventory();
                chestInventory.clear();
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
        
        shopSetupGUIManager.removeSession(playerId);
        actor.sendMessage(Component.text("Dükkanınız başarıyla kuruldu!", NamedTextColor.GREEN));
    }

    public void saveShop(Shop shop) {
        if (shop == null || shop.getLocation() == null) {
            plugin.getLogger().warning("[ShopManager] saveShop called but shop or its location is null. Shop object: " + shop);
            return;
        }
        activeShops.put(shop.getLocation(), shop);
        shopStorage.saveShop(shop);
        this.shopSignManager.updateAttachedSign(shop, this.getCurrencySymbol());
        plugin.getLogger().info(String.format("[ShopManager] Shop at %s (Owner: %s) saved/updated in activeShops and persistent storage requested.",
                 Shop.locationToString(shop.getLocation()), shop.getOwnerUUID()));
    }

    public void cancelShopSetup(UUID playerId) {
        if (playerId == null) {
            plugin.getLogger().warning("[ShopManager] cancelShopSetup called with null playerId.");
            return;
        }

        ShopSetupGUIManager shopSetupGUIManager = plugin.getShopSetupGUIManager();
        if (shopSetupGUIManager == null) {
            plugin.getLogger().severe("[ShopManager] cancelShopSetup called but ShopSetupGUIManager is null! Cannot remove session for " + playerId);
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
            shopSetupGUIManager.removeSession(playerId);
        } else {
             plugin.getLogger().warning("[ShopManager] cancelShopSetup called for player " + playerId + " but no active session found.");
        }
        
        String locStr = Shop.locationToString(chestLocation);

        if (chestLocation != null) {
            Shop pending = pendingShops.remove(chestLocation);
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
                player.sendMessage(Component.text("Başlangıç için ayrılan eşya (", NamedTextColor.YELLOW)
                        .append(Component.text(this.shopSignManager.getItemNameForMessages(initialStock, 15), NamedTextColor.AQUA))
                        .append(Component.text(") envanterinize iade edildi.", NamedTextColor.YELLOW)));
                plugin.getLogger().info(String.format("[ShopManager] Initial stock %s returned to player %s after setup cancellation for location %s.",
                        initialStock.toString(), player.getName(), locStr));
            } else {
                plugin.getLogger().warning(String.format("[ShopManager] Could not return initial stock %s to player %s (offline or null) after setup cancellation for location %s.",
                        initialStock.toString(), playerId, locStr));
            }
        }
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
        if (location == null || player == null) return;
        Shop shopToRemove = getActiveShop(location);
        boolean wasPending = false;
        if (shopToRemove == null) {
            shopToRemove = getPendingShop(location);
            if (shopToRemove != null) wasPending = true;
        }
        if (shopToRemove == null) {
            player.sendMessage(Component.text("Bu konumda kaldırılacak bir dükkan yok.", NamedTextColor.RED));
            return;
        }
        if (!shopToRemove.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("skyblock.admin.removeshop")) {
            player.sendMessage(Component.text("Bu dükkanı kaldırma yetkiniz yok.", NamedTextColor.RED));
            return;
        }

        if (!wasPending) {
            activeShops.remove(location);
            shopStorage.removeShop(location);
            this.shopSignManager.clearAttachedSign(location);
            player.sendMessage(Component.text("Dükkan başarıyla kaldırıldı.", NamedTextColor.GREEN));
            plugin.getLogger().info("Aktif dükkan kaldırıldı (" + Shop.locationToString(location) + ") tarafından " + player.getName());
        } else {
            pendingShops.remove(location);
            player.sendMessage(Component.text("Dükkan kurulumu başarıyla iptal edildi.", NamedTextColor.GREEN));
            plugin.getLogger().info("Bekleyen dükkan kurulumu iptal edildi (" + Shop.locationToString(location) + ") tarafından " + player.getName());
            if (plugin.getShopSetupGUIManager() != null) {
                ShopSetupSession session = plugin.getShopSetupGUIManager().getPlayerSession(shopToRemove.getOwnerUUID());
                if (session != null && session.getChestLocation() != null && session.getChestLocation().equals(location)) {
                    ItemStack initialStockFromSession = session.getInitialStockItem();
                    plugin.getShopSetupGUIManager().removeSession(shopToRemove.getOwnerUUID());
                    plugin.getLogger().info("Also removed active setup session for player " + shopToRemove.getOwnerUUID() + " as their pending shop at " + location + " was removed.");
                    if (initialStockFromSession != null && initialStockFromSession.getType() != Material.AIR && player.isOnline()) {
                        player.getInventory().addItem(initialStockFromSession.clone());
                        player.sendMessage(Component.text("Kurulumdaki eşyalarınız iade edildi.", NamedTextColor.YELLOW));
                    }
                }
            }
        }
    }

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
        if (EconomyManager.isEconomyAvailable()) {
            Economy vaultEco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            String name = vaultEco.currencyNameSingular();
            return (name != null && !name.isEmpty()) ? name : plugin.getConfig().getString("economy.currency-symbol", "$");
        }
        return plugin.getConfig().getString("economy.currency-symbol", "$");
    }

    public boolean executeSellToShop(Player seller, Shop shop, int bundlesToSell) {
        return this.shopTransactionManager.executeSellToShop(seller, shop, bundlesToSell);
    }
}