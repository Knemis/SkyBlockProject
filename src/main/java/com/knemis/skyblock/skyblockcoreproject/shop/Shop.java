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
    private ShopType shopType;       // Mağaza türü (TRADE_CHEST, BANK_CHEST)

    // Geliştirilmiş Eşya Tanımlaması
    private ItemStack templateItemStack; // Satılacak eşyanın tam bir kopyası (NBT dahil)
    private int itemQuantityForPrice;  // Bu 'templateItemStack'den kaç adet için fiyat belirlendiği
    private double buyPrice;           // Oyuncunun eşyayı almak için ödeyeceği fiyat (dükkan SATIYOR)
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
     * @param location Mağaza sandığının konumu.
     * @param ownerUUID Mağaza sahibinin UUID'si.
     * @param shopType Mağaza türü.
     */
    public Shop(Location location, UUID ownerUUID, ShopType shopType) {
        if (location == null) throw new IllegalArgumentException("Shop location cannot be null.");
        if (ownerUUID == null) throw new IllegalArgumentException("Shop owner UUID cannot be null.");
        if (shopType == null) throw new IllegalArgumentException("Shop type cannot be null.");

        this.location = location.clone(); // Konumun kopyasını al
        this.ownerUUID = ownerUUID;
        this.shopType = shopType;
        this.setupComplete = false;
        this.isAdminShop = false; // Varsayılan olarak oyuncu mağazası
        this.buyPrice = 0.0;      // Kurulumda atanacak
        this.sellPrice = -1.0;    // Kurulumda atanacak, -1 alım olmadığını gösterir
        this.totalItemsSold = 0;
        this.totalEarnings = 0.0;
        this.totalItemsBought = 0;  // Initialize new field
        this.totalMoneySpent = 0.0; // Initialize new field
        this.lastActivityTimestamp = System.currentTimeMillis();
        // templateItemStack, itemQuantityForPrice kurulum sırasında atanacak
    }

    /**
     * Tamamen yapılandırılmış bir mağaza için (veya dosyadan yüklenirken) constructor.
     */
    public Shop(Location location, UUID ownerUUID, ShopType shopType,
                ItemStack templateItemStack, int itemQuantityForPrice, double buyPrice, double sellPrice,
                boolean setupComplete, boolean isAdminShop,
                long totalItemsSold, double totalEarnings, long totalItemsBought, double totalMoneySpent, // Added new params
                String shopDisplayName, long lastActivityTimestamp) {
        this(location, ownerUUID, shopType); // Temel constructor'ı çağır

        if (templateItemStack == null && setupComplete) throw new IllegalArgumentException("Template ItemStack cannot be null for a completed shop.");
        if (itemQuantityForPrice <= 0 && setupComplete) throw new IllegalArgumentException("Item quantity for price must be positive for a completed shop.");
        // buyPrice >= 0 olmalı, sellPrice >= -1 (ya da >=0 eğer -1 "not set" anlamındaysa)
        if (buyPrice < 0 && setupComplete && shopType != ShopType.BANK_CHEST) { // Bank chestler için fiyat olmayabilir
            // veya sellPrice > 0 ise (yani alım mağazasıysa) buyPrice 0 olabilir
            boolean isPotentiallyBuyShop = sellPrice >= 0;
            if (!isPotentiallyBuyShop) { // Eğer sadece satış mağazasıysa (buyPrice üzerinden) ve buyPrice < 0 ise hata.
                throw new IllegalArgumentException("Buy price cannot be negative for a completed non-bank, non-buy-only shop.");
            } else if (buyPrice < 0 && isPotentiallyBuyShop && shopType != ShopType.BANK_CHEST) {
                // Hem alım hem satım yapıyorsa ve buyPrice < 0 ise hata.
                // Aslında bu durum "sadece alım" mağazası olarak yorumlanabilir, buyPrice = 0 veya pozitif olmalı.
                // Şimdilik basit tutalım: buyPrice < 0 ise ve alım mağazası değilse (sellPrice < 0) hata.
                // Eğer sellPrice >=0 ise, buyPrice'ın 0 veya pozitif olması beklenir.
                // Bu mantık ShopManager'da daha detaylı ele alınabilir. Şimdilik buyPrice < 0 ise ve sellPrice < 0 ise hata verelim.
                if (sellPrice < 0) throw new IllegalArgumentException("Buy price cannot be negative if sell price is not set.");
            }
        }
        if (sellPrice < -1 && setupComplete && shopType != ShopType.BANK_CHEST) throw new IllegalArgumentException("Sell price cannot be less than -1 for a completed non-bank shop.");


        this.templateItemStack = (templateItemStack != null) ? templateItemStack.clone() : null;
        this.itemQuantityForPrice = itemQuantityForPrice;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
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
    public Location getLocation() { return location.clone(); } // Değiştirilemezlik için kopya
    public UUID getOwnerUUID() { return ownerUUID; }
    public ShopType getShopType() { return shopType; }
    public ItemStack getTemplateItemStack() { return (templateItemStack != null) ? templateItemStack.clone() : null; }
    public int getItemQuantityForPrice() { return itemQuantityForPrice; }
    public double getBuyPrice() { return buyPrice; } // Renamed
    public double getSellPrice() { return sellPrice; } // Added
    public boolean isSetupComplete() { return setupComplete; }
    public boolean isAdminShop() { return isAdminShop; }
    public long getTotalItemsSold() { return totalItemsSold; }
    public double getTotalEarnings() { return totalEarnings; }
    public long getTotalItemsBought() { return totalItemsBought; } // Added getter
    public double getTotalMoneySpent() { return totalMoneySpent; } // Added getter
    public String getShopDisplayName() { return shopDisplayName; }
    public long getLastActivityTimestamp() { return lastActivityTimestamp; }

    // --- Setter Metodları (Kurulum ve İşlem Sırasında Kullanılır) ---
    public void setShopType(ShopType shopType) {
        if (shopType == null) throw new IllegalArgumentException("Shop type cannot be null.");
        this.shopType = shopType;
        updateLastActivity();
    }

    public void setTemplateItemStack(ItemStack templateItemStack) {
        if (templateItemStack == null || templateItemStack.getType() == Material.AIR) {
            throw new IllegalArgumentException("Template ItemStack cannot be null or AIR.");
        }
        this.templateItemStack = templateItemStack.clone(); // Her zaman kopya ile çalış
        updateLastActivity();
    }

    public void setItemQuantityForPrice(int itemQuantityForPrice) {
        if (itemQuantityForPrice <= 0) {
            throw new IllegalArgumentException("Item quantity for price must be positive.");
        }
        this.itemQuantityForPrice = itemQuantityForPrice;
        updateLastActivity();
    }

    public void setBuyPrice(double buyPrice) { // Renamed
        if (buyPrice < 0) { // Fiyat 0 olabilir (ücretsiz itemler için)
            throw new IllegalArgumentException("Buy price cannot be negative.");
        }
        this.buyPrice = buyPrice;
        updateLastActivity();
    }

    public void setSellPrice(double sellPrice) { // Added
        if (sellPrice < -1.0) { // -1.0 "not set" anlamına gelebilir
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
        map.put("shopType", shopType.name());
        map.put("location", locationToString(location)); // Özel serileştirme
        if (templateItemStack != null) {
            map.put("templateItemStack", templateItemStack.serialize()); // ItemStack serileştirme
        }
        map.put("itemQuantityForPrice", itemQuantityForPrice);
        map.put("buyPrice", buyPrice); // Renamed
        map.put("sellPrice", sellPrice); // Added
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
        ShopType type = ShopType.valueOf((String) map.get("shopType"));
        Location loc = stringToLocation((String) map.get("location"));

        if (loc == null) { // Konum yüklenemezse, bu mağaza geçersizdir.
            Bukkit.getLogger().severe("[Shop Deserialization] Failed to deserialize shop due to invalid location for owner: " + owner);
            return null;
        }

        Shop shop = new Shop(loc, owner, type); // Önce temel constructor ile oluştur

        // ItemStack'i deserileştir
        if (map.containsKey("templateItemStack")) {
            Object itemStackData = map.get("templateItemStack");
            if (itemStackData instanceof ItemStack) { // Bazen doğrudan ItemStack olarak gelebilir (eski versiyonlar veya farklı serileştiriciler)
                shop.templateItemStack = (ItemStack) itemStackData;
            } else if (itemStackData instanceof Map) {
                try {
                    shop.templateItemStack = ItemStack.deserialize((Map<String, Object>) itemStackData);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[Shop Deserialization] Failed to deserialize templateItemStack for shop at " + loc + ": " + e.getMessage());
                    shop.templateItemStack = new ItemStack(Material.STONE); // Varsayılan bir item ata
                }
            }
        }

        shop.itemQuantityForPrice = (int) map.getOrDefault("itemQuantityForPrice", 1);

        // Backward compatibility for price -> buyPrice
        if (map.containsKey("buyPrice")) {
            shop.buyPrice = ((Number) map.get("buyPrice")).doubleValue();
        } else if (map.containsKey("price")) { // Legacy support
            shop.buyPrice = ((Number) map.get("price")).doubleValue();
        } else {
            shop.buyPrice = 0.0; // Default if neither is found
        }
        shop.sellPrice = ((Number) map.getOrDefault("sellPrice", -1.0)).doubleValue(); // Default to -1.0 if not found

        shop.setupComplete = (boolean) map.getOrDefault("setupComplete", false);
        shop.isAdminShop = (boolean) map.getOrDefault("isAdminShop", false);
        shop.totalItemsSold = ((Number) map.getOrDefault("totalItemsSold", 0L)).longValue();
        shop.totalEarnings = ((Number) map.getOrDefault("totalEarnings", 0.0)).doubleValue();
        shop.totalItemsBought = ((Number) map.getOrDefault("totalItemsBought", 0L)).longValue();   // Deserialize new field
        shop.totalMoneySpent = ((Number) map.getOrDefault("totalMoneySpent", 0.0)).doubleValue(); // Deserialize new field
        shop.shopDisplayName = (String) map.get("shopDisplayName"); // null olabilir
        shop.lastActivityTimestamp = ((Number) map.getOrDefault("lastActivityTimestamp", System.currentTimeMillis())).longValue();

        // Eğer templateItemStack null ise ve shop tamamlanmışsa, bu bir sorundur.
        if (shop.setupComplete && shop.templateItemStack == null) {
            Bukkit.getLogger().warning("[Shop Deserialization] Completed shop at " + loc + " is missing templateItemStack. Marking as incomplete.");
            shop.setupComplete = false; // Kurulumu tamamlanmamış olarak işaretle
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