// com/knemis/skyblock/skyblockcoreproject/shop/Shop.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;


import java.util.UUID;

public class Shop {
    private final Location location;
    private final UUID ownerUUID;
    private ShopType shopType;
    private Material itemType;
    private int itemQuantityForPrice; // Bu miktardaki item için fiyat belirlenir
    private double price;
    private boolean setupComplete; // Kurulum tamamlandı mı?

    // Kurulum aşamasındaki bir mağaza için constructor
    public Shop(Location location, UUID ownerUUID, ShopType shopType) {
        this.location = location;
        this.ownerUUID = ownerUUID;
        this.shopType = shopType;
        this.setupComplete = false;
        // Diğer alanlar kurulum sırasında atanacak
    }

    // Tamamen kurulmuş veya yüklenmiş bir mağaza için constructor
    public Shop(Location location, UUID ownerUUID, ShopType shopType, Material itemType, int itemQuantityForPrice, double price, boolean setupComplete) {
        this.location = location;
        this.ownerUUID = ownerUUID;
        this.shopType = shopType;
        this.itemType = itemType;
        this.itemQuantityForPrice = itemQuantityForPrice;
        this.price = price;
        this.setupComplete = setupComplete;
    }

    // Getter'lar
    public Location getLocation() { return location; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public ShopType getShopType() { return shopType; }
    public Material getItemType() { return itemType; }
    public int getItemQuantityForPrice() { return itemQuantityForPrice; }
    public double getPrice() { return price; }
    public boolean isSetupComplete() { return setupComplete; }

    // Setter'lar (kurulum sırasında kullanılacak)
    public void setShopType(ShopType shopType) { this.shopType = shopType; } // Genelde başlangıçta ayarlanır
    public void setItemType(Material itemType) { this.itemType = itemType; }
    public void setItemQuantityForPrice(int itemQuantityForPrice) { this.itemQuantityForPrice = itemQuantityForPrice; }
    public void setPrice(double price) { this.price = price; }
    public void setSetupComplete(boolean setupComplete) { this.setupComplete = setupComplete; }

    // Konumu String'e çevirme (ShopStorage için)
    public static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    // String'den Konum'a çevirme (ShopStorage için)
    public static Location stringToLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(";");
        if (parts.length != 4) return null;
        org.bukkit.World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}