// com/knemis/skyblock/skyblockcoreproject/shop/ShopManager.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
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
import net.milkbowl.vault.economy.Economy; // Vault Economy importu

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
        this.activeShops = this.shopStorage.loadShops();
        this.pendingShops = new HashMap<>();
        if (this.activeShops != null && !this.activeShops.isEmpty()) {
            plugin.getLogger().info(this.activeShops.size() + " aktif mağaza ShopManager'a yüklendi.");
        } else if (this.activeShops != null) {
            plugin.getLogger().info("ShopManager'a yüklenecek aktif mağaza bulunamadı.");
        } else {
            plugin.getLogger().warning("ShopStorage.loadShops() null döndü, aktif mağaza yüklenemedi.");
        }
    }

    public Shop initiateShopCreation(Location location, Player player, ShopType initialShopType) {
        if (location == null || player == null) {
            plugin.getLogger().warning("initiateShopCreation: Konum veya oyuncu null geldi.");
            return null;
        }
        if (isShop(location)) {
            Shop existing = getActiveShop(location);
            if (existing == null) existing = getPendingShop(location);
            if (existing != null && existing.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Bu sandıkta zaten bir mağaza kurulumunuz var veya aktif bir mağazanız bulunuyor.");
                return existing;
            } else if (existing != null) {
                player.sendMessage(ChatColor.RED + "Bu sandık zaten başkasına ait bir mağaza olarak kullanılıyor.");
                return null;
            }
        }
        Shop newShop = new Shop(location, player.getUniqueId(), initialShopType);
        pendingShops.put(location, newShop);
        plugin.getLogger().info("Pending shop initiated at " + Shop.locationToString(location) + " by " + player.getName());
        return newShop;
    }

    public Shop getPendingShop(Location location) {
        if (location == null) return null;
        return pendingShops.get(location);
    }

    // Refactored signature
    public void finalizeShopSetup(Location location, Player actor, ItemStack initialStockItem) {
        if (location == null || actor == null) {
            plugin.getLogger().severe("finalizeShopSetup: Location or actor is null!");
            if (actor != null) actor.sendMessage(ChatColor.RED + "Shop setup failed due to an internal error (null parameters).");
            if (location != null) pendingShops.remove(location); // Clean up pending shop if location is valid
            // Cannot return initialStockItem if actor is null or inventory is inaccessible.
            return;
        }

        Shop pendingShop = pendingShops.get(location); // Retrieve from pendingShops map
        if (pendingShop == null) {
            plugin.getLogger().warning("No pending shop found for finalization at: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "Shop setup information not found. Please start over.");
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        // Validation based on pendingShop's state
        if (pendingShop.getShopType() == null) {
            plugin.getLogger().warning("Shop type not set for shop at: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "Shop type was not selected! Setup cancelled.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        ItemStack templateItem = pendingShop.getTemplateItemStack();
        if (templateItem == null || templateItem.getType() == Material.AIR) {
            plugin.getLogger().warning("Template item not set for shop at: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "The item to be sold/bought was not set! Setup cancelled.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        if (pendingShop.getItemQuantityForPrice() <= 0) {
            plugin.getLogger().warning("Item quantity for price is invalid for shop at: " + Shop.locationToString(location) + " Qty: " + pendingShop.getItemQuantityForPrice());
            actor.sendMessage(ChatColor.RED + "Invalid item quantity per transaction! Setup cancelled.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        boolean hasValidBuyPrice = pendingShop.getBuyPrice() >= 0;
        boolean hasValidSellPrice = pendingShop.getSellPrice() >= 0;

        if (!hasValidBuyPrice && !hasValidSellPrice) {
            plugin.getLogger().warning("Neither buy price nor sell price is valid for shop at: " + Shop.locationToString(location) + " BuyP: " + pendingShop.getBuyPrice() + " SellP: " + pendingShop.getSellPrice());
            actor.sendMessage(ChatColor.RED + "No valid price set for buying or selling! Setup cancelled.");
            pendingShops.remove(location);
            if (initialStockItem != null && initialStockItem.getType() != Material.AIR) actor.getInventory().addItem(initialStockItem.clone());
            return;
        }

        // All validations passed, remove from pending and proceed
        pendingShops.remove(location); // Remove from pending map

        pendingShop.setSetupComplete(true); // Mark as complete
        activeShops.put(location, pendingShop); // Add to active shops
        shopStorage.saveShop(pendingShop);    // Persist
        updateAttachedSign(pendingShop);      // Update sign

        // Add initial stock to the chest if applicable
        // A shop that only buys from players (sellPrice >= 0 and buyPrice == -1) should not have initial stock placed.
        // initialStockItem should be null in that case, which is handled by ShopSetupListener.
        boolean needsStocking = initialStockItem != null && initialStockItem.getType() != Material.AIR;
        boolean canBeStocked = pendingShop.getBuyPrice() != -1; // Shop sells to players (or is two-way)

        if (needsStocking && canBeStocked) {
            Block shopBlock = location.getBlock();
            if (shopBlock.getState() instanceof Chest) {
                Chest chest = (Chest) shopBlock.getState();
                Inventory chestInventory = chest.getInventory();
                chestInventory.clear();
                chestInventory.addItem(initialStockItem.clone());
                plugin.getLogger().info("Initial stock of " + initialStockItem.getAmount() + "x " +
                        initialStockItem.getType() + " added to shop at " + Shop.locationToString(location));
            } else {
                plugin.getLogger().severe("Shop block at " + Shop.locationToString(location) + " is not a Chest. Cannot add initial stock.");
            }
        } else if (needsStocking && !canBeStocked) {
            plugin.getLogger().info("Initial stock item provided for shop at " + Shop.locationToString(location) +
                    ", but shop is configured to only buy from players (buyPrice is -1). Stock not added.");
        } else {
            plugin.getLogger().info("No initial stock item provided or item was AIR for shop at " + Shop.locationToString(location) + ". No stock added by default.");
        }

        // Updated logging
        plugin.getLogger().info("Shop finalized by " + actor.getName() + ": " + Shop.locationToString(location) +
                " | Item: " + pendingShop.getTemplateItemStack().getType() +
                " Qty: " + pendingShop.getItemQuantityForPrice() +
                " BuyPrice: " + pendingShop.getBuyPrice() + // Price players pay
                " SellPrice: " + pendingShop.getSellPrice() + // Price players receive
                (needsStocking && canBeStocked ? " | Initial stock added: " + initialStockItem.getAmount() + "x " + initialStockItem.getType() : " | No initial stock or shop only buys"));
        actor.sendMessage(ChatColor.GREEN + "Your shop has been successfully created!");
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

    public void removeShop(Location location, Player player) {
        if (location == null || player == null) return;
        Shop shopToRemove = getActiveShop(location);
        boolean wasPending = false;
        if (shopToRemove == null) {
            shopToRemove = getPendingShop(location);
            if (shopToRemove != null) wasPending = true;
        }
        if (shopToRemove == null) {
            player.sendMessage(ChatColor.RED + "Bu konumda kaldırılacak bir mağaza bulunmuyor.");
            return;
        }
        if (!shopToRemove.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("skyblock.admin.removeshop")) {
            player.sendMessage(ChatColor.RED + "Bu mağazayı kaldırma yetkiniz yok.");
            return;
        }
        if (!wasPending) activeShops.remove(location);
        else pendingShops.remove(location);
        shopStorage.removeShop(location);
        clearAttachedSign(location);
        player.sendMessage(ChatColor.GREEN + "Mağaza başarıyla " + (wasPending ? "kurulumdan " : "") + "kaldırıldı.");
        plugin.getLogger().info("Shop removed at " + Shop.locationToString(location) + " by " + player.getName());
    }

    public String shortenFormattedString(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength < 3) maxLength = 3;
        String cleanText = ChatColor.stripColor(text);
        if (cleanText.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.min(text.length(), maxLength - 3)) + "...";
    }

    public String shortenItemName(String name) {
        return shortenFormattedString(name, 15);
    }

    public String getItemDisplayNameForSign(ItemStack itemStack, int maxLength) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Boş";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            try {
                Component displayNameComponent = meta.displayName();
                if (displayNameComponent != null) {
                    return shortenFormattedString(LegacyComponentSerializer.legacySection().serialize(displayNameComponent), maxLength);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.FINER, "Item özel adı (Component) alınırken/kısaltılırken hata (legacy denenecek): " + itemStack.getType(), e);
                if (meta.getDisplayName() != null && !meta.getDisplayName().isEmpty()) { // Bukkit'in eski getDisplayName'i
                    return shortenFormattedString(meta.getDisplayName(), maxLength);
                }
            }
        }
        String materialName = itemStack.getType().toString().toLowerCase().replace("_", " ");
        String[] words = materialName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return shortenFormattedString(sb.toString().trim(), maxLength);
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
            plugin.getLogger().warning("Mağaza (" + Shop.locationToString(shop.getLocation()) + ") için uygun bir tabela yeri bulunamadı/oluşturulamadı.");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        String ownerName = owner.getName() != null ? shortenFormattedString(owner.getName(), 14) : "Bilinmeyen";
        String itemName = getItemDisplayNameForSign(shop.getTemplateItemStack(), 15);

        String currencySymbol = "$"; // Varsayılan
        Economy econ = plugin.getEconomy();
        if (econ != null) {
            String singular = econ.currencyNameSingular();
            String plural = econ.currencyNamePlural();
            if (singular != null && !singular.isEmpty() && !Character.isLetterOrDigit(singular.charAt(0)) && singular.length() == 1) { // Tek karakterli sembol mü?
                currencySymbol = singular;
            } else if (plural != null && !plural.isEmpty()) {
                currencySymbol = " " + plural; // Dolar, Euro gibi
            } else if (singular != null && !singular.isEmpty()){
                currencySymbol = " " + singular;
            }
        }

        // Sign update logic needs to consider buy/sell prices
        String priceString;
        String shopAction; // "Buying", "Selling", "B/S"

        if (shop.getBuyPrice() >= 0 && shop.getSellPrice() >= 0) {
            shopAction = "B/S"; // Both Buy and Sell
            priceString = String.format("B:%.0f S:%.0f", shop.getBuyPrice(), shop.getSellPrice());
        } else if (shop.getBuyPrice() >= 0) {
            shopAction = "Selling"; // Owner is selling, players are buying
            priceString = String.format("Price:%.0f", shop.getBuyPrice());
        } else if (shop.getSellPrice() >= 0) {
            shopAction = "Buying"; // Owner is buying, players are selling
            priceString = String.format("Pay:%.0f", shop.getSellPrice());
        } else {
            shopAction = "Error";
            priceString = "Not Priced"; // Should not happen if finalizeShopSetup validation is correct
        }

        // Construct the full price line, e.g., "16 for B:10 S:8 $"
        String fullPriceLine = shop.getItemQuantityForPrice() + " for " + priceString + currencySymbol;
        if (ChatColor.stripColor(fullPriceLine).length() > 15) { // Attempt to shorten
            if (shop.getBuyPrice() >= 0 && shop.getSellPrice() >= 0) {
                fullPriceLine = String.format("B:%.0f S:%.0f", shop.getBuyPrice(), shop.getSellPrice());
            } else if (shop.getBuyPrice() >= 0) {
                fullPriceLine = String.format("Sell:%.0f", shop.getBuyPrice());
            } else if (shop.getSellPrice() >= 0) {
                fullPriceLine = String.format("Buy:%.0f", shop.getSellPrice());
            }
            // Prepend quantity again
            fullPriceLine = shop.getItemQuantityForPrice() + "/" + fullPriceLine;
            if (ChatColor.stripColor(fullPriceLine).length() > 15) { // Final attempt
                fullPriceLine = shop.getItemQuantityForPrice() + "/" + (shop.getBuyPrice() >=0 ? shop.getBuyPrice() : shop.getSellPrice());
            }
        }

        signState.line(0, Component.text("[Shop]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
        signState.line(1, Component.text(itemName, NamedTextColor.BLACK));
        signState.line(2, Component.text(fullPriceLine, NamedTextColor.DARK_GREEN)); // Updated price line
        signState.line(3, Component.text(ownerName, NamedTextColor.DARK_PURPLE));
        signState.update(true);
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
                        if (sign.line(0).equals(Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
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
                    if (signState.line(0).equals(Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                        signBlock.setType(Material.AIR);
                        plugin.getLogger().info("Mağaza tabelası (yana bitişik) temizlendi: " + Shop.locationToString(chestLocation));
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
        if (activeShops == null) return new ArrayList<>(); // Null kontrolü
        return activeShops.values().stream()
                .filter(shop -> shop != null && shop.getOwnerUUID().equals(ownerUUID)) // shop null kontrolü
                .collect(Collectors.toList());
    }

    public int getTotalActiveShops() {
        return activeShops != null ? activeShops.size() : 0; // Null kontrolü
    }

    // --- Player Inventory Helper Methods ---

    /**
     * Counts the number of items matching the template in a player's inventory.
     * @param player The player whose inventory to check.
     * @param templateItem The item to match (ignores amount, checks type and meta).
     * @return The total count of matching items.
     */
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

    /**
     * Removes a specified amount of an item from the player's inventory.
     * Assumes the player has enough items (pre-check with countItemsInInventory).
     * @param player The player to remove items from.
     * @param templateItem The item to remove (type and meta match).
     * @param amountToRemove The total amount to remove.
     * @return True if removal was successful.
     */
    public boolean removeItemsFromInventory(Player player, ItemStack templateItem, int amountToRemove) {
        if (player == null || templateItem == null || templateItem.getType() == Material.AIR || amountToRemove <= 0) return false;

        ItemStack itemToRemoveCloned = templateItem.clone();
        itemToRemoveCloned.setAmount(amountToRemove);

        HashMap<Integer, ItemStack> didNotRemove = player.getInventory().removeItem(itemToRemoveCloned);

        return didNotRemove.isEmpty();
    }


    // --- Chest Inventory Helper Methods ---
    /**
     * Checks if a chest has enough space to add a given quantity of an item.
     * @param chest The chest to check.
     * @param itemToAdd The item to be added.
     * @param quantityToAdd The quantity of the item to be added.
     * @return True if there's enough space, false otherwise.
     */
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


    /**
     * Satın alma işlemini gerçekleştiren ana metod.
     * Bu metod, listener tarafından çağrılır ve gerekli tüm kontrolleri,
     * ekonomi işlemlerini ve envanter güncellemelerini yapar.
     * @return Satın alma başarılıysa true.
     */
    public boolean executePurchase(Player buyer, Shop shop, int bundlesToBuy) {
        if (shop == null || !shop.isSetupComplete() || buyer == null || bundlesToBuy <= 0 || shop.getTemplateItemStack() == null) {
            plugin.getLogger().warning("[ShopManager-Purchase] Geçersiz parametreler veya mağaza durumu.");
            if (buyer != null) buyer.sendMessage(ChatColor.RED + "Satın alma işlemi sırasında bir sorun oluştu.");
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToBuy = bundlesToBuy * itemsPerBundle;

        // executePurchase is for when a player BUYS from the shop.
        if (shop.getBuyPrice() < 0) { // Shop does not sell this item (buyPrice is what player pays)
            buyer.sendMessage(ChatColor.RED + "This shop is not selling items currently.");
            return false;
        }
        double totalCost = bundlesToBuy * shop.getBuyPrice(); // Player pays the shop's buyPrice
        ItemStack templateItem = shop.getTemplateItemStack();

        if (totalItemsToBuy <= 0 || totalCost < 0) { // buyPrice can be 0 for free items
            plugin.getLogger().warning("[ShopManager-Purchase] Geçersiz hesaplanmış miktar veya fiyat. Mağaza: " + Shop.locationToString(shop.getLocation()));
            buyer.sendMessage(ChatColor.RED + "Mağaza ayarlarında bir sorun var.");
            return false;
        }

        String formattedItemName = getItemDisplayNameForSign(templateItem, 30);
        String currencySymbol = getCurrencySymbol();


        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(buyer) < totalCost) {
            buyer.sendMessage(ChatColor.RED + "Yetersiz bakiye! Gereken: " + String.format("%.2f", totalCost) + currencySymbol);
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Mağaza sandığı bulunamadı!");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (countItemsInChest(chest, templateItem) < totalItemsToBuy) {
            buyer.sendMessage(ChatColor.RED + "Mağazada yeterli stok yok (" + totalItemsToBuy + " adet " + formattedItemName + " gerekli).");
            updateAttachedSign(shop);
            return false;
        }

        ItemStack itemsToReceive = templateItem.clone();
        itemsToReceive.setAmount(totalItemsToBuy);
        if (!hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + totalItemsToBuy + " adet " + formattedItemName + " için yeterli yer yok!");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, totalCost)) {
            buyer.sendMessage(ChatColor.RED + "Para çekme işlemi başarısız oldu.");
            return false;
        }

        if (!EconomyManager.deposit(owner, totalCost)) {
            EconomyManager.deposit(buyer, totalCost); // Alıcıya iade et
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Satıcıya para aktarılamadı. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Sahip " + (owner.getName() != null ? owner.getName() : owner.getUniqueId()) + " hesabına para yatırılamadı. Alıcı " + buyer.getName() + " parası iade edildi.");
            return false;
        }

        if (!removeItemsFromChest(chest, templateItem, totalItemsToBuy)) {
            EconomyManager.withdraw(owner, totalCost); // Satıcıdan geri al
            EconomyManager.deposit(buyer, totalCost);  // Alıcıya iade et
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Eşyalar mağazadan alınamadı. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Sandıktan eşya çekilemedi. Para transferleri geri alındı. Mağaza: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(totalItemsToBuy, totalCost);
        shopStorage.saveShop(shop);

        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToBuy + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.GREEN + " satın aldınız (" + ChatColor.GOLD + String.format("%.2f", totalCost) + currencySymbol + ChatColor.GREEN + ").");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " mağazanızdan " +
                    ChatColor.AQUA + totalItemsToBuy + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.YELLOW + " satın aldı.");
        }
        updateAttachedSign(shop);
        return true;
    }

    /**
     * Vault ekonomisinden para birimi sembolünü veya adını alır.
     * @return Para birimi sembolü/adı veya varsayılan "$".
     */
    public String getCurrencySymbol() {
        Economy econ = plugin.getEconomy();
        if (econ != null) {
            String singular = econ.currencyNameSingular();
            // Genellikle semboller tek karakter olur ($) veya para birimi adı (USD)
            if (singular != null && !singular.isEmpty()) {
                if (singular.length() == 1 && !Character.isLetterOrDigit(singular.charAt(0))) {
                    return singular; // Sadece sembol ise ($)
                }
                return " " + singular; // Dolar, Euro gibi ise boşluklu
            }
            String plural = econ.currencyNamePlural();
            if (plural != null && !plural.isEmpty()) {
                return " " + plural;
            }
        }
        return "$"; // Varsayılan
    }

    /**
     * Handles a player selling items to a shop.
     * @param seller The player selling items.
     * @param shop The shop being sold to.
     * @param bundlesToSell Number of bundles the player wants to sell.
     * @return True if the sale was successful.
     */
    public boolean executeSellToShop(Player seller, Shop shop, int bundlesToSell) {
        if (shop == null || !shop.isSetupComplete() || seller == null || bundlesToSell <= 0 || shop.getTemplateItemStack() == null) {
            plugin.getLogger().warning("[ShopManager-SellToShop] Invalid parameters or shop state.");
            if (seller != null) seller.sendMessage(ChatColor.RED + "Sell transaction failed due to an issue.");
            return false;
        }

        if (shop.getSellPrice() < 0) { // Shop does not buy this item (sellPrice is what player receives)
            seller.sendMessage(ChatColor.RED + "This shop is not buying items currently.");
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToSell = bundlesToSell * itemsPerBundle;
        double totalPaymentToPlayer = bundlesToSell * shop.getSellPrice(); // Player receives shop's sellPrice
        ItemStack templateItem = shop.getTemplateItemStack();
        String formattedItemName = getItemDisplayNameForSign(templateItem, 30);
        String currencySymbol = getCurrencySymbol();

        if (totalItemsToSell <= 0 || totalPaymentToPlayer < 0) { // Payment can be 0 if shop buys for free (unlikely but possible)
            plugin.getLogger().warning("[ShopManager-SellToShop] Invalid calculated quantity or payment for shop: " + Shop.locationToString(shop.getLocation()));
            seller.sendMessage(ChatColor.RED + "Shop configuration error for this item.");
            return false;
        }

        // 1. Check if player has enough items
        if (countItemsInInventory(seller, templateItem) < totalItemsToSell) {
            seller.sendMessage(ChatColor.RED + "You don't have enough " + ChatColor.AQUA + formattedItemName + ChatColor.RED + " to sell. You need " + totalItemsToSell + ".");
            return false;
        }

        // 2. Check if shop has enough space in its chest
        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            seller.sendMessage(ChatColor.RED + "Shop chest not found!");
            plugin.getLogger().severe("[ShopManager-SellToShop] Shop block at " + Shop.locationToString(shop.getLocation()) + " is not a Chest.");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (!hasEnoughSpaceInChest(chest, templateItem, totalItemsToSell)) {
            seller.sendMessage(ChatColor.RED + "The shop does not have enough space for " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName + ChatColor.RED + ".");
            return false;
        }

        // 3. Check shop owner's balance (using personal balance for now)
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        if (!EconomyManager.isEconomyAvailable()) {
            seller.sendMessage(ChatColor.RED + "Economy system is not available.");
            return false;
        }
        if (EconomyManager.getBalance(owner) < totalPaymentToPlayer) {
            seller.sendMessage(ChatColor.RED + "The shop owner does not have enough funds to buy your items.");
            if (owner.isOnline() && owner.getPlayer() != null) {
                owner.getPlayer().sendMessage(ChatColor.RED + "Your shop at " + Shop.locationToString(shop.getLocation()) + " couldn't afford to buy " + totalItemsToSell + " " + formattedItemName + " from " + seller.getName() + ".");
            }
            return false;
        }

        // --- Transaction Process ---
        // 4. Withdraw money from owner
        if (!EconomyManager.withdraw(owner, totalPaymentToPlayer)) {
            seller.sendMessage(ChatColor.RED + "Failed to process payment from shop owner. Please try again.");
            plugin.getLogger().severe("[ShopManager-SellToShop] Failed to withdraw " + totalPaymentToPlayer + " from owner " + owner.getName() + " for shop " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        // 5. Deposit money to seller
        if (!EconomyManager.deposit(seller, totalPaymentToPlayer)) {
            EconomyManager.deposit(owner, totalPaymentToPlayer); // Refund owner
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "CRITICAL ERROR: " + ChatColor.RESET + ChatColor.RED + "Could not deposit funds to your account. Owner has been refunded.");
            plugin.getLogger().severe("[ShopManager-SellToShop] CRITICAL: Failed to deposit " + totalPaymentToPlayer + " to seller " + seller.getName() + ". Owner " + owner.getName() + " refunded for shop " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        // 6. Remove items from seller's inventory
        if (!removeItemsFromInventory(seller, templateItem, totalItemsToSell)) {
            EconomyManager.withdraw(seller, totalPaymentToPlayer); // Take back money from seller
            EconomyManager.deposit(owner, totalPaymentToPlayer);   // Refund owner
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "CRITICAL ERROR: " + ChatColor.RESET + ChatColor.RED + "Failed to remove items from your inventory. Transaction reversed.");
            plugin.getLogger().severe("[ShopManager-SellToShop] CRITICAL: Failed to remove " + totalItemsToSell + " of " + templateItem.getType() + " from " + seller.getName() + ". Transaction reversed for shop " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        // 7. Add items to shop's chest
        ItemStack itemsToAdd = templateItem.clone();
        itemsToAdd.setAmount(totalItemsToSell);
        chest.getInventory().addItem(itemsToAdd.clone()); // Add items to chest

        // 8. Record transaction for the shop
        shop.recordPlayerSaleToShop(totalItemsToSell, totalPaymentToPlayer); // New method in Shop.java
        shopStorage.saveShop(shop);

        seller.sendMessage(ChatColor.GREEN + "Successfully sold " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                ChatColor.GREEN + " to the shop for " + ChatColor.GOLD + String.format("%.2f", totalPaymentToPlayer) + currencySymbol + ChatColor.GREEN + ".");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.YELLOW + "Your shop at " + Shop.locationToString(shop.getLocation()) +
                    " bought " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                    ChatColor.YELLOW + " from " + ChatColor.GOLD + seller.getName() +
                    ChatColor.YELLOW + " for " + ChatColor.GOLD + String.format("%.2f", totalPaymentToPlayer) + currencySymbol + ChatColor.YELLOW + ".");
        }
        updateAttachedSign(shop); // Update sign if stock/price display changes based on this
        return true;
    }
}