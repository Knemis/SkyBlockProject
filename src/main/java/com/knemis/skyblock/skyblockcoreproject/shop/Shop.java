// com/knemis/skyblock/skyblockcoreproject/shop/Shop.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.serialization.ConfigurationSerializable; // ItemStack kaydetmek için

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Shop implements ConfigurationSerializable { // ConfigurationSerializable eklendi

    private final Location location; // Mağaza sandığının konumu (final olmalı)
    private final UUID ownerUUID;    // Mağaza sahibinin UUID'si (final olmalı)
    private ShopMode shopMode;       // Defines interaction model (MARKET_CHEST, BANK_CHEST)

    // Geliştirilmiş Eşya Tanımlaması
    // templateItemStack.getAmount() now represents the bundleAmount for the buyPrice
    private ItemStack templateItemStack; // Satılacak eşyanın tam bir kopyası (NBT dahil), amount is bundle size.
    private double buyPrice;           // Total price for the bundle defined by templateItemStack.getAmount()
    private double sellPrice;          // Oyuncunun eşyayı satmak için alacağı fiyat (dükkan ALIYOR), -1 ise alım yok

    private boolean setupComplete;     // Kurulum tamamlandı mı?
    private boolean isAdminShop;       // Bu bir admin mağazası mı?

    // İstatistikler
    private long totalItemsSold;    // Items sold by the shop to players
    private double totalEarnings;   // Money earned by the shop from players
    private long totalItemsBought;  // Items bought by the shop from players
    private double totalMoneySpent; // Money spent by the shop to buy from players

    // Opsiyonel Geliştirmeler
    private String shopDisplayName;    // Oyuncunun mağazaya verdiği özel isim
    private long lastActivityTimestamp; // Son işlem veya güncelleme zamanı

    /**
     * Yeni bir mağaza kurulumu için (geçici) constructor.
     * @param location Shop location.
     * @param ownerUUID Shop owner's UUID.
     * @param initialShopMode The initial mode of the shop.
     */
    public Shop(Location location, UUID ownerUUID, ShopMode initialShopMode) {
        if (location == null) throw new IllegalArgumentException("Shop location cannot be null.");
        if (ownerUUID == null) throw new IllegalArgumentException("Shop owner UUID cannot be null.");
        if (initialShopMode == null) throw new IllegalArgumentException("Shop mode cannot be null.");

        this.location = location.clone();
        this.ownerUUID = ownerUUID;
        this.shopMode = initialShopMode; // Set the new shopMode
        this.setupComplete = false;
        this.isAdminShop = false;
        this.buyPrice = 0.0;      // Represents totalPriceForBundle, set during setup
        this.sellPrice = -1.0;    // Defaulted, as this class now focuses on owner-selling shops
        this.totalItemsSold = 0;
        this.totalEarnings = 0.0;
        this.totalItemsBought = 0;
        this.totalMoneySpent = 0.0;
        this.lastActivityTimestamp = System.currentTimeMillis();
        // templateItemStack (with its amount as bundle size) is set during setup
    }

    /**
     * Fully parameterized constructor for loading shops or direct instantiation.
     * `templateItemStackWithBundleAmount.getAmount()` is the bundle size.
     * `totalPriceForBundle` is the price for that bundle.
     */
    public Shop(Location location, UUID ownerUUID, ShopMode shopMode,
                ItemStack templateItemStackWithBundleAmount, double totalPriceForBundle, double sellPrice, // sellPrice kept for buy-shops
                boolean setupComplete, boolean isAdminShop,
                long totalItemsSold, double totalEarnings, long totalItemsBought, double totalMoneySpent,
                String shopDisplayName, long lastActivityTimestamp) {

        this(location, ownerUUID, shopMode); // Calls the simpler constructor

        if (templateItemStackWithBundleAmount == null && setupComplete) throw new IllegalArgumentException("Template ItemStack cannot be null for a completed shop.");
        if (templateItemStackWithBundleAmount != null && templateItemStackWithBundleAmount.getAmount() <= 0 && setupComplete) {
            throw new IllegalArgumentException("Template ItemStack amount (bundle size) must be positive for a completed shop.");
        }
        if (totalPriceForBundle < 0 && setupComplete && shopMode != null /* && shop allows selling by owner */) {
            // If it's a shop where owner sells (buyPrice is relevant), buyPrice can't be negative.
            // This might need more nuanced checking depending on how buy-only shops are handled with ShopMode
            if (sellPrice < 0) { // If it's not also a buy-from-player shop
                throw new IllegalArgumentException("Buy price (totalPriceForBundle) cannot be negative if sell price is not set.");
            }
        }
        // sellPrice validation can remain if it's still used for player-selling-to-shop functionality
        // For now, the focus is on owner-selling shops, so sellPrice is less critical here but kept for data structure.
        // if (sellPrice < -1 && setupComplete) throw new IllegalArgumentException("Sell price cannot be less than -1.");


        this.templateItemStack = (templateItemStackWithBundleAmount != null) ? templateItemStackWithBundleAmount.clone() : null;
        this.buyPrice = totalPriceForBundle;
        this.sellPrice = sellPrice; // Retained for potential buy-back functionality
        this.setupComplete = setupComplete;
        this.isAdminShop = isAdminShop;
        this.totalItemsSold = totalItemsSold;
        this.totalEarnings = totalEarnings;
        this.totalItemsBought = totalItemsBought; // Assign new param
        this.totalMoneySpent = totalMoneySpent;   // Assign new param
        this.shopDisplayName = shopDisplayName;
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    // --- Getter Metodları ---
    public Location getLocation() { return location.clone(); }
    public UUID getOwnerUUID() { return ownerUUID; }
    public ShopMode getShopMode() { return shopMode; }
    public ItemStack getTemplateItemStack() { return (templateItemStack != null) ? templateItemStack.clone() : null; }
    public double getBuyPrice() { return buyPrice; } // This is totalPriceForBundle
    public double getSellPrice() { return sellPrice; }
    public boolean isSetupComplete() { return setupComplete; }
    public boolean isAdminShop() { return isAdminShop; }
    public long getTotalItemsSold() { return totalItemsSold; }
    public double getTotalEarnings() { return totalEarnings; }
    public long getTotalItemsBought() { return totalItemsBought; }
    public double getTotalMoneySpent() { return totalMoneySpent; }
    public String getShopDisplayName() { return shopDisplayName; }
    public long getLastActivityTimestamp() { return lastActivityTimestamp; }

    // Convenience Getters
    public int getBundleAmount() {
        return this.templateItemStack != null ? this.templateItemStack.getAmount() : 0;
    }

    public double getUnitPrice() {
        int bundleAmount = getBundleAmount();
        if (bundleAmount > 0 && this.buyPrice >= 0) {
            return this.buyPrice / bundleAmount;
        }
        return -1.0; // Or throw exception, or handle as error state
    }

    // --- Setter Metodları (Kurulum ve İşlem Sırasında Kullanılır) ---
    public void setShopMode(ShopMode shopMode) {
        if (shopMode == null) throw new IllegalArgumentException("Shop mode cannot be null.");
        this.shopMode = shopMode;
        updateLastActivity();
    }

    /**
     * Sets the template item. The amount of this ItemStack is considered the bundle size.
     * @param templateItemStack The item with its amount representing the bundle size.
     */
    public void setTemplateItemStack(ItemStack templateItemStack) {
        if (templateItemStack == null || templateItemStack.getType() == Material.AIR) {
            throw new IllegalArgumentException("Template ItemStack cannot be null or AIR.");
        }
        if (templateItemStack.getAmount() <= 0) {
            throw new IllegalArgumentException("Template ItemStack amount (bundle size) must be positive.");
        }
        this.templateItemStack = templateItemStack.clone();
        updateLastActivity();
    }

    /**
     * Sets the total price for the bundle defined by templateItemStack.getAmount().
     * @param totalPriceForBundle The total price for the bundle.
     */
    public void setBuyPrice(double totalPriceForBundle) {
        if (totalPriceForBundle < 0) {
            throw new IllegalArgumentException("Buy price (totalPriceForBundle) cannot be negative.");
        }
        this.buyPrice = totalPriceForBundle;
        updateLastActivity();
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice < -1.0) { // -1.0 indicates not buying from players
            throw new IllegalArgumentException("Sell price cannot be less than -1.");
        }
        this.sellPrice = sellPrice;
        updateLastActivity();
    }

    public void setSetupComplete(boolean setupComplete) {
        this.setupComplete = setupComplete;
        updateLastActivity();
    }

    public void setAdminShop(boolean adminShop) {
        isAdminShop = adminShop;
        updateLastActivity();
    }

    public void setShopDisplayName(String shopDisplayName) {
        this.shopDisplayName = shopDisplayName; // Null veya boş olabilir
        updateLastActivity();
    }

    private void updateLastActivity() {
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    // --- İşlem Metodları ---
    /**
     * Bir satış işlemi gerçekleştiğinde çağrılır. İstatistikleri günceller.
     * @param quantitySold Satılan eşya (bundle değil, tekil eşya) sayısı.
     * @param transactionPrice Bu işlemden elde edilen toplam fiyat.
     */
    public void recordTransaction(int quantitySold, double transactionPrice) {
        if (quantitySold <= 0 || transactionPrice < 0) return; // Geçersiz işlem
        this.totalItemsSold += quantitySold;
        this.totalEarnings += transactionPrice;
        updateLastActivity();
    }

    /**
     * Called when a player sells items to this shop. Updates shop statistics.
     * @param quantityBoughtByShop Number of items (individual, not bundles) the shop bought.
     * @param moneySpentByShop Total money the shop paid to the player.
     */
    public void recordPlayerSaleToShop(int quantityBoughtByShop, double moneySpentByShop) {
        if (quantityBoughtByShop <= 0 || moneySpentByShop < 0) return; // Invalid transaction
        this.totalItemsBought += quantityBoughtByShop;
        this.totalMoneySpent += moneySpentByShop;
        updateLastActivity();
    }

    // --- ConfigurationSerializable Metodları (ItemStack'i YAML'a kaydetmek için) ---
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerUUID", ownerUUID.toString());
        map.put("shopMode", shopMode.name()); // Store ShopMode
        map.put("location", locationToString(location));
        if (templateItemStack != null) {
            // Amount is now part of templateItemStack
            map.put("templateItemStack", templateItemStack.serialize());
        }
        map.put("buyPrice", buyPrice); // This is totalPriceForBundle
        map.put("sellPrice", sellPrice);
        map.put("setupComplete", setupComplete);
        map.put("isAdminShop", isAdminShop);
        map.put("totalItemsSold", totalItemsSold);
        map.put("totalEarnings", totalEarnings);
        map.put("totalItemsBought", totalItemsBought);   // Serialize new field
        map.put("totalMoneySpent", totalMoneySpent); // Serialize new field
        if (shopDisplayName != null) {
            map.put("shopDisplayName", shopDisplayName);
        }
        map.put("lastActivityTimestamp", lastActivityTimestamp);
        return map;
    }

    @SuppressWarnings("unchecked") // ItemStack deserileştirme için
    public static Shop deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("ownerUUID"));
        Location loc = stringToLocation((String) map.get("location"));

        if (loc == null) {
            Bukkit.getLogger().severe("[Shop Deserialization] Failed to deserialize shop due to invalid location for owner: " + owner);
            return null;
        }

        ShopMode shopMode = ShopMode.MARKET_CHEST; // Default for migration or if missing
        if (map.containsKey("shopMode")) {
            try {
                shopMode = ShopMode.valueOf((String) map.get("shopMode"));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid shopMode value '" + map.get("shopMode") + "' found for shop at " + loc + ". Defaulting to MARKET_CHEST.");
            }
        } else if (map.containsKey("shopType")) { // Legacy ShopType handling
            String legacyShopType = (String) map.get("shopType");
            // Assuming PLAYER_SELL_SHOP and PLAYER_BUY_SELL_SHOP (where owner sells) map to a default ShopMode like MARKET_CHEST.
            // BANK_CHEST from old ShopType might map to ShopMode.BANK_CHEST if that was its intended interaction.
            if ("PLAYER_SELL_SHOP".equals(legacyShopType) || "PLAYER_BUY_SELL_SHOP".equals(legacyShopType)) {
                shopMode = ShopMode.MARKET_CHEST;
            } else if ("BANK_CHEST".equals(legacyShopType)) { // Example if old BANK_CHEST maps to new BANK_CHEST
                shopMode = ShopMode.BANK_CHEST;
            }
            // Note: PLAYER_BUY_SHOP from old ShopType doesn't have a direct equivalent in the new owner-selling ShopMode enum.
            // This logic assumes we're primarily migrating shops where the owner was selling.
            // If PLAYER_BUY_SHOP needs to be handled, it might require a different approach or be considered an invalid state for the new model.
            Bukkit.getLogger().info("Migrating legacy shopType '" + legacyShopType + "' to shopMode '" + shopMode + "' for shop at " + loc);
        }

        Shop shop = new Shop(loc, owner, shopMode);

        if (map.containsKey("templateItemStack")) {
            Object itemStackData = map.get("templateItemStack");
            if (itemStackData instanceof ItemStack) {
                shop.templateItemStack = (ItemStack) itemStackData;
            } else if (itemStackData instanceof Map) {
                try {
                    shop.templateItemStack = ItemStack.deserialize((Map<String, Object>) itemStackData);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[Shop Deserialization] Failed to deserialize templateItemStack for shop at " + loc + ": " + e.getMessage());
                    shop.templateItemStack = new ItemStack(Material.STONE, 1); // Fallback with amount 1
                }
            }
        }

        // Legacy handling for itemQuantityForPrice if templateItemStack amount is not set (e.g. old data)
        if (shop.templateItemStack != null && (shop.templateItemStack.getAmount() == 0 || shop.templateItemStack.getAmount() == 1) && map.containsKey("itemQuantityForPrice")) {
            int legacyQuantity = ((Number) map.get("itemQuantityForPrice")).intValue();
            if (legacyQuantity > 0) {
                shop.templateItemStack.setAmount(legacyQuantity);
                Bukkit.getLogger().info("Applied legacy itemQuantityForPrice (" + legacyQuantity + ") to templateItemStack amount for shop at " + loc);
            }
        }
        if (shop.templateItemStack != null && shop.templateItemStack.getAmount() == 0) {
            shop.templateItemStack.setAmount(1); // Ensure bundle amount is at least 1 if somehow still 0
        }


        // Backward compatibility for price -> buyPrice (totalPriceForBundle)
        if (map.containsKey("buyPrice")) {
            shop.buyPrice = ((Number) map.get("buyPrice")).doubleValue();
        } else if (map.containsKey("price")) { // Legacy support for "price" field
            shop.buyPrice = ((Number) map.get("price")).doubleValue();
            Bukkit.getLogger().info("Migrated legacy 'price' field to 'buyPrice' for shop at " + loc);
        } else {
            shop.buyPrice = 0.0;
        }
        shop.sellPrice = ((Number) map.getOrDefault("sellPrice", -1.0)).doubleValue();

        shop.setupComplete = (boolean) map.getOrDefault("setupComplete", false);
        shop.isAdminShop = (boolean) map.getOrDefault("isAdminShop", false);
        shop.totalItemsSold = ((Number) map.getOrDefault("totalItemsSold", 0L)).longValue();
        shop.totalEarnings = ((Number) map.getOrDefault("totalEarnings", 0.0)).doubleValue();
        shop.totalItemsBought = ((Number) map.getOrDefault("totalItemsBought", 0L)).longValue();
        shop.totalMoneySpent = ((Number) map.getOrDefault("totalMoneySpent", 0.0)).doubleValue();
        shop.shopDisplayName = (String) map.get("shopDisplayName");
        shop.lastActivityTimestamp = ((Number) map.getOrDefault("lastActivityTimestamp", System.currentTimeMillis())).longValue();

        if (shop.setupComplete && (shop.templateItemStack == null || shop.templateItemStack.getType() == Material.AIR || shop.templateItemStack.getAmount() <= 0)) {
            Bukkit.getLogger().warning("[Shop Deserialization] Completed shop at " + loc + " has invalid templateItemStack (null, AIR, or 0 amount). Marking as incomplete.");
            shop.setupComplete = false;
        }
        if (shop.setupComplete && shop.buyPrice < 0 && shop.sellPrice < 0) { // Ensure at least one price is valid for a completed shop
            Bukkit.getLogger().warning("[Shop Deserialization] Completed shop at " + loc + " has no valid prices (buyPrice and sellPrice are < 0). Marking as incomplete.");
            shop.setupComplete = false;
        }


        return shop;
    }

    // --- Yardımcı Konum Serileştirme Metodları ---
    public static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        // Dünya ismini UUID yerine isim olarak kaydetmek daha taşınabilir.
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public static Location stringToLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(";");
        if (parts.length != 4) return null;
        org.bukkit.World world = Bukkit.getWorld(parts[0]); // Dünya ismiyle yükle
        if (world == null) return null;
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- Eşitlik ve HashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop) o;
        return location.equals(shop.location); // Mağazalar konumlarına göre benzersizdir
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}