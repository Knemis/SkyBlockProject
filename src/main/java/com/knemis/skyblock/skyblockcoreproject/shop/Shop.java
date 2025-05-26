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
    private double price;              // Belirlenen miktar için fiyat

    private boolean setupComplete;     // Kurulum tamamlandı mı?
    private boolean isAdminShop;       // Bu bir admin mağazası mı?

    // İstatistikler
    private long totalItemsSold;
    private double totalEarnings;

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
        this.totalItemsSold = 0;
        this.totalEarnings = 0.0;
        this.lastActivityTimestamp = System.currentTimeMillis();
        // templateItemStack, itemQuantityForPrice, price kurulum sırasında atanacak
    }

    /**
     * Tamamen yapılandırılmış bir mağaza için (veya dosyadan yüklenirken) constructor.
     */
    public Shop(Location location, UUID ownerUUID, ShopType shopType,
                ItemStack templateItemStack, int itemQuantityForPrice, double price,
                boolean setupComplete, boolean isAdminShop,
                long totalItemsSold, double totalEarnings, String shopDisplayName, long lastActivityTimestamp) {
        this(location, ownerUUID, shopType); // Temel constructor'ı çağır

        if (templateItemStack == null && setupComplete) throw new IllegalArgumentException("Template ItemStack cannot be null for a completed shop.");
        if (itemQuantityForPrice <= 0 && setupComplete) throw new IllegalArgumentException("Item quantity for price must be positive for a completed shop.");
        if (price < 0 && setupComplete) throw new IllegalArgumentException("Price cannot be negative for a completed shop.");

        this.templateItemStack = (templateItemStack != null) ? templateItemStack.clone() : null;
        this.itemQuantityForPrice = itemQuantityForPrice;
        this.price = price;
        this.setupComplete = setupComplete;
        this.isAdminShop = isAdminShop;
        this.totalItemsSold = totalItemsSold;
        this.totalEarnings = totalEarnings;
        this.shopDisplayName = shopDisplayName;
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    // --- Getter Metodları ---
    public Location getLocation() { return location.clone(); } // Değiştirilemezlik için kopya
    public UUID getOwnerUUID() { return ownerUUID; }
    public ShopType getShopType() { return shopType; }
    public ItemStack getTemplateItemStack() { return (templateItemStack != null) ? templateItemStack.clone() : null; }
    public int getItemQuantityForPrice() { return itemQuantityForPrice; }
    public double getPrice() { return price; }
    public boolean isSetupComplete() { return setupComplete; }
    public boolean isAdminShop() { return isAdminShop; }
    public long getTotalItemsSold() { return totalItemsSold; }
    public double getTotalEarnings() { return totalEarnings; }
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

    public void setPrice(double price) {
        if (price < 0) { // Fiyat 0 olabilir (ücretsiz itemler için)
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
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
        map.put("price", price);
        map.put("setupComplete", setupComplete);
        map.put("isAdminShop", isAdminShop);
        map.put("totalItemsSold", totalItemsSold);
        map.put("totalEarnings", totalEarnings);
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
        shop.price = ((Number) map.getOrDefault("price", 0.0)).doubleValue();
        shop.setupComplete = (boolean) map.getOrDefault("setupComplete", false);
        shop.isAdminShop = (boolean) map.getOrDefault("isAdminShop", false);
        shop.totalItemsSold = ((Number) map.getOrDefault("totalItemsSold", 0L)).longValue();
        shop.totalEarnings = ((Number) map.getOrDefault("totalEarnings", 0.0)).doubleValue();
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