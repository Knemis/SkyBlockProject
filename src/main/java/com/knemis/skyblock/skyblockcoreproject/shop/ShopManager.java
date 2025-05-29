// com/knemis/skyblock/skyblockcoreproject/shop/ShopManager.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
// import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager; // SkyBlockProject'ten enum almak için gerekebilir
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

    public ShopManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopStorage = new ShopStorage(plugin);
        this.activeShops = this.shopStorage.loadShops(); // ShopStorage.loadShops() should handle its own logging for load count/failures
        this.pendingShops = new HashMap<>();
        if (this.activeShops != null) { // activeShops can be an empty map from loadShops, not null
            plugin.getLogger().info(String.format("[ShopManager] Initialized with %d active shops from storage.", this.activeShops.size()));
        } else {
            // This case should ideally not happen if loadShops returns an empty map on failure instead of null.
            // However, if it can be null due to a deeper issue in ShopStorage:
            plugin.getLogger().severe("[ShopManager] Initialization failed: activeShops is null after attempting to load from ShopStorage.");
            // Consider initializing activeShops to a new HashMap to prevent NullPointerExceptions later.
            // this.activeShops = new HashMap<>();
        }
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
                newShop.getShopId(), locStr, player.getName(), player.getUniqueId(), initialShopMode));
        return newShop;
    }

    public Shop getPendingShop(Location location) {
        if (location == null) return null;
        return pendingShops.get(location);
    }

    public Shop getPendingShop(UUID playerId) {
        Location shopLocation = plugin.getPlayerShopSetupState().get(playerId);
        if (shopLocation != null) {
            return pendingShops.get(shopLocation);
        }
        return null;
    }


    public void finalizeShopSetup(Location location, Player actor, ItemStack initialStockItem) {
        String locStr = Shop.locationToString(location);
        plugin.getLogger().info(String.format("[ShopManager] Attempting to finalize shop setup at %s for player %s (UUID: %s). Initial stock: %s",
                locStr, actor.getName(), actor.getUniqueId(), initialStockItem != null ? initialStockItem.toString() : "null"));

        if (location == null || actor == null) {
            plugin.getLogger().severe(String.format("[ShopManager] finalizeShopSetup failed: Location (%s) or actor (%s) is null!", locStr, actor));
            if (actor != null) actor.sendMessage(ChatColor.RED + "Dükkan kurulumu bir iç hata nedeniyle başarısız oldu (null parametreler).");
            if (location != null) pendingShops.remove(location); // Clean up pending shop if location is known
            return;
        }

        Shop pendingShop = pendingShops.get(location);
        if (pendingShop == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed: No pending shop found at %s for player %s.", locStr, actor.getName()));
            actor.sendMessage(ChatColor.RED + "Dükkan kurulum bilgisi bulunamadı. Lütfen baştan başlayın.");
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        if (pendingShop.getShopMode() == null) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Shop mode not set.", locStr, actor.getName()));
            actor.sendMessage(ChatColor.RED + "Dükkan modu seçilmedi! Kurulum iptal edildi.");
            pendingShops.remove(location); // Clean up
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        ItemStack templateItem = pendingShop.getTemplateItemStack();
        if (templateItem == null || templateItem.getType() == Material.AIR) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Template item not set.", locStr, actor.getName()));
            actor.sendMessage(ChatColor.RED + "Satılacak/alınacak eşya ayarlanmadı! Kurulum iptal edildi.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        if (pendingShop.getBundleAmount() <= 0) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Invalid bundle amount %d.",
                    locStr, actor.getName(), pendingShop.getBundleAmount()));
            actor.sendMessage(ChatColor.RED + "İşlem için geçersiz paket miktarı! Kurulum iptal edildi.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        boolean hasValidBuyPrice = pendingShop.getBuyPrice() >= 0;
        boolean hasValidSellPrice = pendingShop.getSellPrice() >= 0;

        if (!hasValidBuyPrice && !hasValidSellPrice) {
            plugin.getLogger().warning(String.format("[ShopManager] Finalization failed for shop %s (Player: %s): Neither buy nor sell price is valid. Buy: %.2f, Sell: %.2f",
                    locStr, actor.getName(), pendingShop.getBuyPrice(), pendingShop.getSellPrice()));
            actor.sendMessage(ChatColor.RED + "Alış veya satış için geçerli bir fiyat belirlenmedi! Kurulum iptal edildi.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        String shopId = pendingShop.getShopId(); // Get ID before removing from pending
        pendingShops.remove(location);
        pendingShop.setSetupComplete(true);
        saveShop(pendingShop); // This will log the save operation

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
                        initialStockItem.toString(), Shop.locationToString(shop.getLocation()), locStr)); // Changed shopId to location
            } else {
                plugin.getLogger().severe(String.format("[ShopManager] Shop block at %s is not a Chest for shop at %s. Initial stock not added.", locStr, Shop.locationToString(shop.getLocation()))); // Changed shopId to location
            }
        } else if (needsStocking && !canBeStockedByPlayerBuying) {
            plugin.getLogger().info(String.format("[ShopManager] Initial stock for shop at %s (%s) not added as shop is sell-only (buyPrice is -1).", locStr, Shop.locationToString(shop.getLocation()))); // Changed shopId to location
        }

        plugin.getLogger().info(String.format("[ShopManager] Shop setup finalized: Location %s by %s. Item: %s, QtyPerBundle: %d, BuyPrice: %.2f, SellPrice: %.2f, Mode: %s. Stocked: %b",
                locStr, actor.getName(), templateItem.getType(), pendingShop.getBundleAmount(),
                pendingShop.getBuyPrice(), pendingShop.getSellPrice(), pendingShop.getShopMode(), (needsStocking && canBeStockedByPlayerBuying)));
        actor.sendMessage(ChatColor.GREEN + "Dükkanınız başarıyla kuruldu!");
    }

    public void saveShop(Shop shop) {
        if (shop == null || shop.getLocation() == null) {
            plugin.getLogger().warning("[ShopManager] saveShop called but shop or its location is null. Shop object: " + shop);
            return;
        }
        activeShops.put(shop.getLocation(), shop);
        shopStorage.saveShop(shop); 
        updateAttachedSign(shop); 
        plugin.getLogger().info(String.format("[ShopManager] Shop at %s (Owner: %s) saved/updated in activeShops and persistent storage requested.",
                 Shop.locationToString(shop.getLocation()), shop.getOwnerUUID()));
    }

    public void cancelShopSetup(UUID playerId) {
        if (playerId == null) {
            plugin.getLogger().warning("[ShopManager] cancelShopSetup called with null playerId.");
            return;
        }

        Location chestLocation = plugin.getPlayerShopSetupState().remove(playerId);
        plugin.getPlayerWaitingForSetupInput().remove(playerId); 
        ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(playerId);
        String locStr = Shop.locationToString(chestLocation); // May be "UNKNOWN_LOCATION" if chestLocation is null

        if (chestLocation != null) {
            Shop pending = pendingShops.remove(chestLocation);
            plugin.getLogger().info(String.format("[ShopManager] Shop setup cancelled by player %s for location %s. Pending shop (Owner: %s) removed.",
                    playerId, locStr, (pending != null ? pending.getOwnerUUID() : "N/A")));
        } else {
            plugin.getLogger().info(String.format("[ShopManager] Shop setup cancellation for player %s (no specific location found in state, possibly already cleaned or state error).", playerId));
        }

        if (initialStock != null && initialStock.getType() != Material.AIR) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(initialStock.clone());
                player.sendMessage(ChatColor.YELLOW + "Başlangıç için ayrılan eşya (" + ChatColor.AQUA + getItemNameForMessages(initialStock, 15) + ChatColor.YELLOW + ") envanterinize iade edildi.");
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
            clearAttachedSign(location);
            player.sendMessage(ChatColor.GREEN + "Dükkan başarıyla kaldırıldı.");
            plugin.getLogger().info("Aktif dükkan kaldırıldı (" + Shop.locationToString(location) + ") tarafından " + player.getName());
        } else {
            pendingShops.remove(location);
            player.sendMessage(ChatColor.GREEN + "Dükkan kurulumu başarıyla iptal edildi.");
            plugin.getLogger().info("Bekleyen dükkan kurulumu iptal edildi (" + Shop.locationToString(location) + ") tarafından " + player.getName());
            plugin.getPlayerShopSetupState().remove(shopToRemove.getOwnerUUID());
            // **** DÜZELTME: getPlayerWaitingForSetupInput kullanıldı ****
            plugin.getPlayerWaitingForSetupInput().remove(shopToRemove.getOwnerUUID());
            ItemStack initialStock = plugin.getPlayerInitialShopStockItem().remove(shopToRemove.getOwnerUUID());
            if (initialStock != null && initialStock.getType() != Material.AIR && player.isOnline()) {
                player.getInventory().addItem(initialStock.clone());
                player.sendMessage(ChatColor.YELLOW + "Kurulumdaki eşyalarınız iade edildi.");
            }
        }
    }

    public String shortenFormattedString(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength < 3) maxLength = 3;
        String cleanText = ChatColor.stripColor(text);
        if (cleanText.length() <= maxLength) {
            return text;
        }
        return cleanText.substring(0, maxLength - 3) + "...";
    }

    public String shortenItemName(String name) {
        return shortenFormattedString(name, 15);
    }

    public String getItemNameForMessages(ItemStack itemStack, int maxLength) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            try {
                Component displayNameComponent = meta.displayName();
                if (displayNameComponent != null) {
                    return shortenFormattedString(LegacyComponentSerializer.legacySection().serialize(displayNameComponent), maxLength);
                }
            } catch (NoSuchMethodError e) { // API < 1.16.5 (or no Adventure support)
                return shortenFormattedString(meta.getDisplayName(), maxLength); // Bukkit's String display name
            }
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
            return shortenFormattedString(capitalizedName.toString().trim(), maxLength);
        }
        return "Eşya";
    }

    public void updateAttachedSign(Shop shop) {
        if (shop == null || !shop.isSetupComplete() || shop.getLocation() == null || shop.getTemplateItemStack() == null) {
            return;
        }
        Block chestBlock = shop.getLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST) {
            return;
        }
        Sign signState = findOrCreateAttachedSign(chestBlock);
        if (signState == null) {
            plugin.getLogger().fine("Dükkan için uygun tabela bulunamadı/oluşturulamadı (" + Shop.locationToString(shop.getLocation()) + ").");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        String ownerName = owner.getName() != null ? shortenFormattedString(owner.getName(), 14) : "Bilinmeyen";
        String itemName = getItemNameForMessages(shop.getTemplateItemStack(), 15);
        String currencySymbol = getCurrencySymbol();
        String priceString;
        boolean canPlayerBuy = shop.getBuyPrice() >= 0;
        boolean canPlayerSell = shop.getSellPrice() >= 0;

        if (canPlayerBuy && canPlayerSell) {
            priceString = String.format("Al:%.0f Sat:%.0f", shop.getBuyPrice(), shop.getSellPrice());
        } else if (canPlayerBuy) {
            priceString = String.format("Fiyat: %.0f", shop.getBuyPrice());
        } else if (canPlayerSell) {
            priceString = String.format("Ödeme: %.0f", shop.getSellPrice());
        } else {
            priceString = "Fiyat Yok";
        }

        String bundleInfo = shop.getBundleAmount() + " adet";
        String fullPriceLine = bundleInfo + " / " + priceString;

        if (ChatColor.stripColor(fullPriceLine).length() > 15) {
            fullPriceLine = shop.getBundleAmount() + "/" + priceString;
            if (ChatColor.stripColor(fullPriceLine).length() > 15) {
                if (canPlayerBuy && canPlayerSell) priceString = String.format("A:%.0f S:%.0f", shop.getBuyPrice(), shop.getSellPrice());
                else if (canPlayerBuy) priceString = String.format("F:%.0f", shop.getBuyPrice());
                else if (canPlayerSell) priceString = String.format("Ö:%.0f", shop.getSellPrice());
                fullPriceLine = shop.getBundleAmount() + "/" + priceString;
            }
        }

        signState.line(0, Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
        signState.line(1, Component.text(itemName, NamedTextColor.BLACK));
        signState.line(2, Component.text(fullPriceLine, NamedTextColor.DARK_GREEN));
        signState.line(3, Component.text(ownerName, NamedTextColor.DARK_PURPLE));
        try {
            signState.update(true);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Tabela güncellenirken hata oluştu: " + Shop.locationToString(shop.getLocation()), e);
        }
    }

    private Sign findOrCreateAttachedSign(Block chestBlock) {
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        Material signMaterial = Material.OAK_WALL_SIGN;

        for (BlockFace face : facesToTry) {
            Block relative = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(relative.getType()) && relative.getState() instanceof Sign) {
                if (relative.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) relative.getBlockData();
                    if (wallSignData.getFacing().getOppositeFace() == face) {
                        Sign sign = (Sign) relative.getState();
                        if (sign.line(0).equals(Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                            return sign;
                        }
                    }
                }
            }
        }
        for (BlockFace face : facesToTry) {
            Block potentialSignBlock = chestBlock.getRelative(face);
            if (potentialSignBlock.getType() == Material.AIR) {
                potentialSignBlock.setType(signMaterial, false);
                if (potentialSignBlock.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) potentialSignBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace());
                    potentialSignBlock.setBlockData(wallSignData, true);
                    return (Sign) potentialSignBlock.getState();
                } else {
                    potentialSignBlock.setType(Material.AIR);
                }
            }
        }
        return null;
    }

    private void clearAttachedSign(Location chestLocation) {
        if (chestLocation == null) return;
        Block chestBlock = chestLocation.getBlock();
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : facesToTry) {
            Block signBlock = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(signBlock.getType()) && signBlock.getBlockData() instanceof WallSign) {
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                if (wallSignData.getFacing().getOppositeFace() == face) {
                    Sign signState = (Sign) signBlock.getState();
                    if (signState.line(0).equals(Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                        signBlock.setType(Material.AIR);
                        plugin.getLogger().info("Dükkan tabelası temizlendi: " + Shop.locationToString(chestLocation));
                        return;
                    }
                }
            }
        }
    }

    public boolean hasEnoughSpace(Player player, ItemStack itemToReceive) {
        Inventory inv = player.getInventory();
        if (itemToReceive == null || itemToReceive.getType() == Material.AIR || itemToReceive.getAmount() <= 0) return true;
        int amountNeeded = itemToReceive.getAmount();
        for (ItemStack slotItem : inv.getStorageContents()) {
            if (amountNeeded <= 0) break;
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                amountNeeded -= itemToReceive.getMaxStackSize();
            } else if (slotItem.isSimilar(itemToReceive)) {
                amountNeeded -= (Math.max(0, itemToReceive.getMaxStackSize() - slotItem.getAmount()));
            }
        }
        return amountNeeded <= 0;
    }

    public boolean removeItemsFromChest(Chest chest, ItemStack templateItemToRemove, int amountToRemove) {
        if (chest == null || templateItemToRemove == null || templateItemToRemove.getType() == Material.AIR || amountToRemove <= 0) return false;
        Inventory chestInventory = chest.getInventory();
        if (countItemsInChest(chest, templateItemToRemove) < amountToRemove) {
            return false;
        }
        int removedCount = 0;
        ItemStack[] contents = chestInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemInSlot = contents[i];
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToRemove)) {
                int amountInSlot = itemInSlot.getAmount();
                int canRemoveFromSlot = Math.min(amountToRemove - removedCount, amountInSlot);
                itemInSlot.setAmount(amountInSlot - canRemoveFromSlot);
                removedCount += canRemoveFromSlot;
                if (itemInSlot.getAmount() <= 0) {
                    contents[i] = null;
                }
                if (removedCount >= amountToRemove) break;
            }
        }
        chestInventory.setContents(contents);
        return removedCount >= amountToRemove;
    }

    public int countItemsInChest(Chest chest, ItemStack templateItemToMatch) {
        int count = 0;
        if (chest == null || templateItemToMatch == null || templateItemToMatch.getType() == Material.AIR) return 0;
        Inventory chestInventory = chest.getInventory();
        for (ItemStack itemInSlot : chestInventory.getContents()) {
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToMatch)) {
                count += itemInSlot.getAmount();
            }
        }
        return count;
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
                .filter(shop -> shop != null && shop.getOwnerUUID().equals(ownerUUID))
                .collect(Collectors.toList());
    }

    public int getTotalActiveShops() {
        return activeShops != null ? activeShops.size() : 0;
    }

    public int countItemsInInventory(Player player, ItemStack templateItem) {
        if (player == null || templateItem == null || templateItem.getType() == Material.AIR) return 0;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(templateItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public boolean removeItemsFromInventory(Player player, ItemStack templateItem, int amountToRemove) {
        if (player == null || templateItem == null || templateItem.getType() == Material.AIR || amountToRemove <= 0) return false;

        ItemStack itemToRemoveCloned = templateItem.clone();
        itemToRemoveCloned.setAmount(amountToRemove);

        HashMap<Integer, ItemStack> didNotRemove = player.getInventory().removeItem(itemToRemoveCloned);

        return didNotRemove.isEmpty();
    }

    public boolean hasEnoughSpaceInChest(Chest chest, ItemStack itemToAdd, int quantityToAdd) {
        if (chest == null || itemToAdd == null || itemToAdd.getType() == Material.AIR || quantityToAdd <= 0) return false;

        Inventory chestInventory = chest.getInventory();
        int maxStackSize = itemToAdd.getMaxStackSize();
        int remainingToAdd = quantityToAdd;

        for (ItemStack slotItem : chestInventory.getStorageContents()) {
            if (remainingToAdd <= 0) break;

            if (slotItem == null || slotItem.getType() == Material.AIR) {
                remainingToAdd -= maxStackSize;
            } else if (slotItem.isSimilar(itemToAdd)) {
                int spaceInSlot = maxStackSize - slotItem.getAmount();
                remainingToAdd -= spaceInSlot;
            }
        }
        return remainingToAdd <= 0;
    }

    public boolean executePurchase(Player buyer, Shop shop, int bundlesToBuy) {
        if (shop == null || !shop.isSetupComplete() || buyer == null || bundlesToBuy <= 0 || shop.getTemplateItemStack() == null) {
            plugin.getLogger().warning("[ShopManager-Purchase] Invalid parameters or shop state for purchase attempt by " + (buyer != null ? buyer.getName() : "UnknownPlayer"));
            if (buyer != null) buyer.sendMessage(ChatColor.RED + "Satın alma işlemi sırasında bir hata oluştu.");
            return false;
        }

        int itemsPerBundle = shop.getBundleAmount();
        int totalItemsToBuy = bundlesToBuy * itemsPerBundle;

        if (shop.getBuyPrice() < 0) {
            buyer.sendMessage(ChatColor.RED + "Bu dükkan şu anda ürün satmıyor.");
            return false;
        }
        double totalCost = bundlesToBuy * shop.getBuyPrice();
        ItemStack templateItem = shop.getTemplateItemStack();

        if (totalItemsToBuy <= 0 || totalCost < 0) {
            plugin.getLogger().warning("[ShopManager-Purchase] Geçersiz hesaplanmış miktar veya fiyat. Dükkan: " + Shop.locationToString(shop.getLocation()));
            buyer.sendMessage(ChatColor.RED + "Dükkan ayarlarında bir sorun var.");
            return false;
        }

        String formattedItemName = getItemNameForMessages(templateItem, 30);
        String currencySymbol = getCurrencySymbol();

        // **** DÜZELTME: EconomyManager çağrılarından plugin parametresi kaldırıldı ****
        if (!EconomyManager.isEconomyAvailable()) { // plugin parametresi kaldırıldı
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(buyer) < totalCost) { // plugin parametresi kaldırıldı
            buyer.sendMessage(ChatColor.RED + "Yetersiz bakiye! Gereken: " + String.format("%.2f%s", totalCost, currencySymbol));
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Dükkan sandığı bulunamadı!");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (countItemsInChest(chest, templateItem) < totalItemsToBuy) {
            buyer.sendMessage(ChatColor.RED + "Dükkanda yeterli stok yok (" + totalItemsToBuy + " " + formattedItemName + " gerekli).");
            updateAttachedSign(shop);
            return false;
        }

        ItemStack itemsToReceive = templateItem.clone();
        itemsToReceive.setAmount(totalItemsToBuy);
        if (!hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + totalItemsToBuy + " " + formattedItemName + " için yeterli yer yok!");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, totalCost)) { // plugin parametresi kaldırıldı
            buyer.sendMessage(ChatColor.RED + "Ödeme çekme işlemi başarısız oldu.");
            return false;
        }

        if (!EconomyManager.deposit(owner, totalCost)) { // plugin parametresi kaldırıldı
            EconomyManager.deposit(buyer, totalCost); // Alıcıya iade et // plugin parametresi kaldırıldı
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Satıcıya para transfer edilemedi. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Para sahibi " + (owner.getName() != null ? owner.getName() : owner.getUniqueId()) + " hesabına yatırılamadı. Alıcı " + buyer.getName() + " iade edildi.");
            return false;
        }

        if (!removeItemsFromChest(chest, templateItem, totalItemsToBuy)) {
            EconomyManager.withdraw(owner, totalCost); // Satıcıdan geri al // plugin parametresi kaldırıldı
            EconomyManager.deposit(buyer, totalCost);  // Alıcıya iade et // plugin parametresi kaldırıldı
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Dükkandan ürünler alınamadı. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Sandıktan ürünler çekilemedi. Para transferleri geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(totalItemsToBuy, totalCost);
        saveShop(shop);

        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToBuy + " " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.GREEN + " satın aldınız, fiyat: " + ChatColor.GOLD + String.format("%.2f%s", totalCost, currencySymbol) + ChatColor.GREEN + ".");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " dükkanınızdan " +
                    ChatColor.AQUA + totalItemsToBuy + " " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.YELLOW + " satın aldı.");
        }
        updateAttachedSign(shop);
        return true;
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
        if (shop == null || !shop.isSetupComplete() || seller == null || bundlesToSell <= 0 || shop.getTemplateItemStack() == null) {
            plugin.getLogger().warning("[ShopManager-SellToShop] Geçersiz parametreler veya dükkan durumu.");
            if (seller != null) seller.sendMessage(ChatColor.RED + "Satış işlemi sırasında bir sorun oluştu.");
            return false;
        }

        if (shop.getSellPrice() < 0) {
            seller.sendMessage(ChatColor.RED + "Bu dükkan şu anda ürün almıyor.");
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToSell = bundlesToSell * itemsPerBundle;
        double totalPaymentToPlayer = bundlesToSell * shop.getSellPrice();
        ItemStack templateItem = shop.getTemplateItemStack();
        String formattedItemName = getItemNameForMessages(templateItem, 30);
        String currencySymbol = getCurrencySymbol();

        if (totalItemsToSell <= 0 || totalPaymentToPlayer < 0) {
            plugin.getLogger().warning("[ShopManager-SellToShop] Geçersiz hesaplanmış miktar veya ödeme. Dükkan: " + Shop.locationToString(shop.getLocation()));
            seller.sendMessage(ChatColor.RED + "Bu ürün için dükkan yapılandırma hatası.");
            return false;
        }

        if (countItemsInInventory(seller, templateItem) < totalItemsToSell) {
            seller.sendMessage(ChatColor.RED + "Satmak için yeterli " + ChatColor.AQUA + formattedItemName + ChatColor.RED + " ürününüz yok. " + totalItemsToSell + " adet gerekli.");
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            seller.sendMessage(ChatColor.RED + "Dükkan sandığı bulunamadı!");
            plugin.getLogger().severe("[ShopManager-SellToShop] Dükkan bloğu (" + Shop.locationToString(shop.getLocation()) + ") sandık değil.");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (!hasEnoughSpaceInChest(chest, templateItem, totalItemsToSell)) {
            seller.sendMessage(ChatColor.RED + "Dükkanda " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName + ChatColor.RED + " için yeterli yer yok.");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        // **** DÜZELTME: EconomyManager çağrılarından plugin parametresi kaldırıldı ****
        if (!EconomyManager.isEconomyAvailable()) { // plugin parametresi kaldırıldı
            seller.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(owner) < totalPaymentToPlayer) { // plugin parametresi kaldırıldı
            seller.sendMessage(ChatColor.RED + "Dükkan sahibinin ürünlerinizi alacak kadar parası yok.");
            if (owner.isOnline() && owner.getPlayer() != null) {
                owner.getPlayer().sendMessage(ChatColor.RED + Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız, " + seller.getName() + " adlı oyuncudan " + totalItemsToSell + " " + formattedItemName + " alacak kadar paraya sahip değildi.");
            }
            return false;
        }

        if (!EconomyManager.withdraw(owner, totalPaymentToPlayer)) { // plugin parametresi kaldırıldı
            seller.sendMessage(ChatColor.RED + "Dükkan sahibinden ödeme işlenirken hata oluştu. Lütfen tekrar deneyin.");
            plugin.getLogger().severe("[ShopManager-SellToShop] Sahip " + owner.getName() + " hesabından " + totalPaymentToPlayer + " çekilemedi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!EconomyManager.deposit(seller, totalPaymentToPlayer)) { // plugin parametresi kaldırıldı
            EconomyManager.deposit(owner, totalPaymentToPlayer); // Sahibine iade et // plugin parametresi kaldırıldı
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Hesabınıza para yatırılamadı. Sahip iade edildi.");
            plugin.getLogger().severe("[ShopManager-SellToShop] KRİTİK: Satıcı " + seller.getName() + " hesabına " + totalPaymentToPlayer + " yatırılamadı. Sahip " + owner.getName() + " iade edildi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!removeItemsFromInventory(seller, templateItem, totalItemsToSell)) {
            EconomyManager.withdraw(seller, totalPaymentToPlayer); // Satıcıdan parayı geri al // plugin parametresi kaldırıldı
            EconomyManager.deposit(owner, totalPaymentToPlayer);   // Sahibine iade et // plugin parametresi kaldırıldı
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Envanterinizden ürünler kaldırılamadı. İşlem geri alındı.");
            plugin.getLogger().severe("[ShopManager-SellToShop] KRİTİK: " + seller.getName() + " envanterinden " + totalItemsToSell + " adet " + templateItem.getType() + " kaldırılamadı. İşlem geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        ItemStack itemsToAdd = templateItem.clone();
        itemsToAdd.setAmount(totalItemsToSell);
        chest.getInventory().addItem(itemsToAdd.clone());

        shop.recordPlayerSaleToShop(totalItemsToSell, totalPaymentToPlayer);
        saveShop(shop);

        seller.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                ChatColor.GREEN + " dükkana sattınız, kazanç: " + ChatColor.GOLD + String.format("%.2f%s", totalPaymentToPlayer, currencySymbol) + ChatColor.GREEN + ".");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.YELLOW + Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız " +
                    ChatColor.GOLD + seller.getName() + ChatColor.YELLOW + " adlı oyuncudan " +
                    ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                    ChatColor.YELLOW + " satın aldı, ödenen: " + ChatColor.GOLD + String.format("%.2f%s", totalPaymentToPlayer, currencySymbol) + ChatColor.YELLOW + ".");
        }
        updateAttachedSign(shop);
        return true;
    }
}