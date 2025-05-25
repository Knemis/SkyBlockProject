package com.knemis.skyblock.skyblockcoreproject.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ShopManager {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // Sandığı mağaza olarak kaydet
    public void createShop(Location location, String ownerUUID, double price) {
        String locKey = locationToString(location);
        config.set("shops." + locKey + ".owner", ownerUUID);
        config.set("shops." + locKey + ".price", price);
        plugin.saveConfig();
    }

    // Mağaza var mı?
    public boolean isShop(Location location) {
        String locKey = locationToString(location);
        return config.contains("shops." + locKey);
    }

    // Fiyatı al
    public double getPrice(Location location) {
        String locKey = locationToString(location);
        return config.getDouble("shops." + locKey + ".price");
    }

    // Sahibini al
    public String getOwner(Location location) {
        String locKey = locationToString(location);
        return config.getString("shops." + locKey + ".owner");
    }

    // Sandığı sil
    public void removeShop(Location location) {
        String locKey = locationToString(location);
        config.set("shops." + locKey, null);
        plugin.saveConfig();
    }

    // Konumu String'e çevir (x,y,z,world)
    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    // Geriye tüm mağazaları verir (gelişmiş kullanım için)
    public Map<Location, Double> getAllShops() {
        Map<Location, Double> shops = new HashMap<>();
        if (config.contains("shops")) {
            for (String key : config.getConfigurationSection("shops").getKeys(false)) {
                String[] parts = key.split(",");
                Location loc = new Location(
                        Bukkit.getWorld(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])
                );
                double price = config.getDouble("shops." + key + ".price");
                shops.put(loc, price);
            }
        }
        return shops;
    }
}
