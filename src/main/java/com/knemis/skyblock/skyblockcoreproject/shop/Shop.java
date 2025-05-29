// com/knemis/skyblock/skyblockcoreproject/shop/Shop.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Shop implements ConfigurationSerializable {

    private final Location location;
    private final UUID ownerUUID;
    private ShopMode shopMode;
    private ItemStack templateItemStack;
    private double buyPrice;  // Oyuncunun bu dükkandan bir paket eşya almak için ödeyeceği fiyat
    private double sellPrice; // Oyuncunun bu dükkana bir paket eşya satarak alacağı fiyat

    private boolean setupComplete;
    private boolean isAdminShop;
    private long totalItemsSold;
    private double totalEarnings;
    private long totalItemsBought;
    private double totalMoneySpent;
    private String shopDisplayName;
    private long lastActivityTimestamp;

    // private com.knemis.skyblock.skyblockcoreproject.shop.ShopType shopType; // Removed


    public Shop(Location location, UUID ownerUUID, ShopMode initialShopMode) {
        if (location == null) throw new IllegalArgumentException("Shop location cannot be null.");
        if (ownerUUID == null) throw new IllegalArgumentException("Shop owner UUID cannot be null.");
        if (initialShopMode == null) throw new IllegalArgumentException("Shop mode cannot be null.");

        this.location = location.clone();
        this.ownerUUID = ownerUUID;
        this.shopMode = initialShopMode;
        this.setupComplete = false;
        this.isAdminShop = false;
        this.buyPrice = 0.0;
        this.sellPrice = -1.0;
        this.totalItemsSold = 0;
        this.totalEarnings = 0.0;
        this.totalItemsBought = 0;
        this.totalMoneySpent = 0.0;
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    public Shop(Location location, UUID ownerUUID, ShopMode shopMode,
                ItemStack templateItemStackWithBundleAmount, double totalPriceForBundle, double sellPrice,
                boolean setupComplete, boolean isAdminShop,
                long totalItemsSold, double totalEarnings, long totalItemsBought, double totalMoneySpent,
                String shopDisplayName, long lastActivityTimestamp) {

        this(location, ownerUUID, shopMode);

        if (templateItemStackWithBundleAmount == null && setupComplete) {
            // Bukkit.getLogger().warning("Template ItemStack cannot be null for a completed shop at " + locationToString(location));
            // Geçici olarak null bırakılabilir veya hata fırlatılabilir.
            // throw new IllegalArgumentException("Template ItemStack cannot be null for a completed shop.");
        }
        if (templateItemStackWithBundleAmount != null && templateItemStackWithBundleAmount.getAmount() <= 0 && setupComplete) {
            // Bukkit.getLogger().warning("Template ItemStack amount (bundle size) must be positive for a completed shop at " + locationToString(location));
            // throw new IllegalArgumentException("Template ItemStack amount (bundle size) must be positive for a completed shop.");
        }
        if (totalPriceForBundle < 0 && setupComplete && shopMode != null && sellPrice < 0) {
            // Bukkit.getLogger().warning("Buy price (totalPriceForBundle) cannot be negative if sell price is not set for shop at " + locationToString(location));
            // throw new IllegalArgumentException("Buy price (totalPriceForBundle) cannot be negative if sell price is not set.");
        }

        this.templateItemStack = (templateItemStackWithBundleAmount != null) ? templateItemStackWithBundleAmount.clone() : null;
        this.buyPrice = totalPriceForBundle;
        this.sellPrice = sellPrice;
        this.setupComplete = setupComplete;
        this.isAdminShop = isAdminShop;
        this.totalItemsSold = totalItemsSold;
        this.totalEarnings = totalEarnings;
        this.totalItemsBought = totalItemsBought;
        this.totalMoneySpent = totalMoneySpent;
        this.shopDisplayName = shopDisplayName;
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    // --- Getter Methods ---
    public Location getLocation() { return location.clone(); }
    public UUID getOwnerUUID() { return ownerUUID; }
    public ShopMode getShopMode() { return shopMode; }
    public ItemStack getTemplateItemStack() { return (templateItemStack != null) ? templateItemStack.clone() : null; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public boolean isSetupComplete() { return setupComplete; }
    public boolean isAdminShop() { return isAdminShop; }
    public long getTotalItemsSold() { return totalItemsSold; }
    public double getTotalEarnings() { return totalEarnings; }
    public long getTotalItemsBought() { return totalItemsBought; }
    public double getTotalMoneySpent() { return totalMoneySpent; }
    public String getShopDisplayName() { return shopDisplayName; }
    public long getLastActivityTimestamp() { return lastActivityTimestamp; }

    public String getShopId() {
        System.out.println("[TRACE] Executing getShopId for shop at " + this.location);
        return Shop.locationToString(this.location);
    }

    // Convenience Getters
    public int getBundleAmount() {
        return this.templateItemStack != null ? this.templateItemStack.getAmount() : 0;
    }

    public double getUnitPrice() {
        int bundleAmount = getBundleAmount();
        if (bundleAmount > 0 && this.buyPrice >= 0) {
            return this.buyPrice / bundleAmount;
        }
        return -1.0;
    }

    // Compatibility Getters/Setters (Sizin ekledikleriniz)
    public int getItemQuantityForPrice() {
        return getBundleAmount();
    }

    public double getPrice() {
        return getBuyPrice();
    }

    // YENİ EKLENEN METOTLAR (ShopSetupListener uyumluluğu için) - REMOVED
    // public com.knemis.skyblock.skyblockcoreproject.shop.ShopType getShopType() {
    //     return shopType;
    // }

    // public void setShopType(com.knemis.skyblock.skyblockcoreproject.shop.ShopType shopType) {
    //     this.shopType = shopType;
    //     updateLastActivity(); 
    // }

    /**
     * Fiyat başına ürün miktarını (paket boyutunu) ayarlar.
     * Bu, templateItemStack'in miktarını doğrudan değiştirir.
     * @param amount Yeni paket boyutu. Pozitif olmalıdır.
     */
    public void setItemQuantityForPrice(int amount) {
        if (this.templateItemStack == null) {
            // Bu durum, kurulum akışı doğruysa idealde ulaşılmamalıdır.
            // Hata loglanabilir veya IllegalStateException fırlatılabilir.
            // Bukkit.getLogger().warning("setItemQuantityForPrice called when templateItemStack is null for shop: " + ownerUUID);
            return;
        }
        if (amount <= 0) {
            // Bukkit.getLogger().warning("setItemQuantityForPrice called with non-positive amount: " + amount + " for shop: " + ownerUUID);
            // Geçersiz bir miktar ayarlanmasını önlemek için hata fırlatılabilir veya işlem yapılmayabilir.
            // throw new IllegalArgumentException("Item quantity for price must be positive.");
            return; // Şimdilik geçersizse ayarlamayı engelle
        }
        this.templateItemStack.setAmount(amount);
        updateLastActivity();
    }

    // --- Setter Methods ---
    public void setShopMode(ShopMode shopMode) {
        if (shopMode == null) throw new IllegalArgumentException("Shop mode cannot be null.");
        this.shopMode = shopMode;
        updateLastActivity();
    }

    public void setTemplateItemStack(ItemStack templateItemStack) {
        if (templateItemStack == null || templateItemStack.getType() == Material.AIR) {
            throw new IllegalArgumentException("Template ItemStack cannot be null or AIR.");
        }
        if (templateItemStack.getAmount() <= 0) {
            // throw new IllegalArgumentException("Template ItemStack amount (bundle size) must be positive.");
            // Bukkit.getLogger().warning("Attempt to set template ItemStack with non-positive amount: " + templateItemStack.getAmount());
            // Geçici olarak 1'e ayarla ya da hata fırlat. Şimdilik uyarı verip devam etsin.
            templateItemStack.setAmount(1); // En az 1 olmalı
        }
        this.templateItemStack = templateItemStack.clone();
        updateLastActivity();
    }

    public void setBuyPrice(double totalPriceForBundle) {
        // if (totalPriceForBundle < 0 && totalPriceForBundle != -1) { // -1 "satmıyor" anlamına gelebilir.
        //     throw new IllegalArgumentException("Buy price (totalPriceForBundle) cannot be negative unless it's -1.");
        // }
        this.buyPrice = totalPriceForBundle; // Fiyat -1 olabilir (satış yok)
        updateLastActivity();
    }

    public void setSellPrice(double sellPrice) {
        // if (sellPrice < 0 && sellPrice != -1) { // -1 "almıyor" anlamına gelebilir.
        //     throw new IllegalArgumentException("Sell price cannot be less than -1.");
        // }
        this.sellPrice = sellPrice; // Fiyat -1 olabilir (alış yok)
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
        this.shopDisplayName = shopDisplayName;
        updateLastActivity();
    }

    private void updateLastActivity() {
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    // Transaction Methods
    public void recordTransaction(int quantitySold, double transactionPrice) {
        if (quantitySold <= 0 || transactionPrice < 0) return;
        this.totalItemsSold += quantitySold;
        this.totalEarnings += transactionPrice;
        updateLastActivity();
    }

    public void recordPlayerSaleToShop(int quantityBoughtByShop, double moneySpentByShop) {
        if (quantityBoughtByShop <= 0 || moneySpentByShop < 0) return;
        this.totalItemsBought += quantityBoughtByShop;
        this.totalMoneySpent += moneySpentByShop;
        updateLastActivity();
    }

    // ConfigurationSerializable Methods
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerUUID", ownerUUID.toString());
        map.put("shopMode", shopMode.name());
        map.put("location", locationToString(location));
        if (templateItemStack != null) {
            map.put("templateItemStack", templateItemStack.serialize());
        }
        map.put("buyPrice", buyPrice);
        map.put("sellPrice", sellPrice);
        map.put("setupComplete", setupComplete);
        map.put("isAdminShop", isAdminShop);
        map.put("totalItemsSold", totalItemsSold);
        map.put("totalEarnings", totalEarnings);
        map.put("totalItemsBought", totalItemsBought);
        map.put("totalMoneySpent", totalMoneySpent);
        if (shopDisplayName != null) {
            map.put("shopDisplayName", shopDisplayName);
        }
        map.put("lastActivityTimestamp", lastActivityTimestamp);
        // shopType'ı kaydetmek isteyip istemediğinize karar verin. Eğer ShopMode ana sistemse gereksiz olabilir.
        // if (shopType != null) {
        //     map.put("shopType", shopType.name());
        // }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Shop deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("ownerUUID"));
        Location loc = stringToLocation((String) map.get("location"));

        if (loc == null) {
            Bukkit.getLogger().severe("[Shop Deserialization] Failed to deserialize shop due to invalid location for owner: " + owner);
            return null;
        }

        ShopMode shopMode = ShopMode.MARKET_CHEST;
        if (map.containsKey("shopMode")) {
            try {
                shopMode = ShopMode.valueOf((String) map.get("shopMode"));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid shopMode value '" + map.get("shopMode") + "' found for shop at " + loc + ". Defaulting to MARKET_CHEST.");
            }
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
                    shop.templateItemStack = new ItemStack(Material.STONE, 1);
                }
            }
        }

        shop.buyPrice = ((Number) map.getOrDefault("buyPrice", 0.0)).doubleValue();
        shop.sellPrice = ((Number) map.getOrDefault("sellPrice", -1.0)).doubleValue();
        shop.setupComplete = (boolean) map.getOrDefault("setupComplete", false);
        shop.isAdminShop = (boolean) map.getOrDefault("isAdminShop", false);
        shop.totalItemsSold = ((Number) map.getOrDefault("totalItemsSold", 0L)).longValue();
        shop.totalEarnings = ((Number) map.getOrDefault("totalEarnings", 0.0)).doubleValue();
        shop.totalItemsBought = ((Number) map.getOrDefault("totalItemsBought", 0L)).longValue();
        shop.totalMoneySpent = ((Number) map.getOrDefault("totalMoneySpent", 0.0)).doubleValue();
        shop.shopDisplayName = (String) map.get("shopDisplayName");
        shop.lastActivityTimestamp = ((Number) map.getOrDefault("lastActivityTimestamp", System.currentTimeMillis())).longValue();

        // Eğer shopType'ı kaydettiyseniz, burada yükleyin.
        // if (map.containsKey("shopType")) {
        //     try {
        //         shop.shopType = com.knemis.skyblock.skyblockcoreproject.shop.ShopType.valueOf((String) map.get("shopType"));
        //     } catch (IllegalArgumentException e) {
        //         Bukkit.getLogger().warning("Invalid shopType value '" + map.get("shopType") + "' found for shop at " + loc);
        //     }
        // }


        if (shop.setupComplete && (shop.templateItemStack == null || shop.templateItemStack.getType() == Material.AIR || shop.templateItemStack.getAmount() <= 0)) {
            Bukkit.getLogger().warning("[Shop Deserialization] Completed shop at " + loc + " has invalid templateItemStack. Marking as incomplete.");
            shop.setupComplete = false;
        }
        if (shop.setupComplete && shop.buyPrice < 0 && shop.sellPrice < 0) {
            Bukkit.getLogger().warning("[Shop Deserialization] Completed shop at " + loc + " has no valid prices. Marking as incomplete.");
            shop.setupComplete = false;
        }
        return shop;
    }

    public static String locationToString(Location loc) {
        System.out.println("[TRACE] Executing locationToString for location " + loc);
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public static Location stringToLocation(String locString) {
        System.out.println("[TRACE] Executing stringToLocation for string " + locString);
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(";");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop) o;
        return location.equals(shop.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}