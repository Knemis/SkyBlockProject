// com/knemis/skyblock/skyblockcoreproject/shop/ShopStorage.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ShopStorage {

    private final SkyBlockProject plugin;
    private final File shopsFile;
    private FileConfiguration shopsConfig;

    public ShopStorage(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopsFile.exists()) {
            try {
                shopsFile.createNewFile();
                plugin.getLogger().info("shops.yml oluşturuldu.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "shops.yml oluşturulamadı!", e);
            }
        }
        this.shopsConfig = YamlConfiguration.loadConfiguration(shopsFile);
    }

    public void saveShop(Shop shop) {
        if (shop == null || shop.getLocation() == null) return;
        String locString = Shop.locationToString(shop.getLocation());
        String path = "shops." + locString;

        shopsConfig.set(path + ".ownerUUID", shop.getOwnerUUID().toString());
        shopsConfig.set(path + ".shopType", shop.getShopType().name());
        shopsConfig.set(path + ".setupComplete", shop.isSetupComplete());

        if (shop.isSetupComplete()) {
            shopsConfig.set(path + ".itemType", shop.getItemType().name());
            shopsConfig.set(path + ".itemQuantityForPrice", shop.getItemQuantityForPrice());
            shopsConfig.set(path + ".price", shop.getPrice());
        } else {
            // Kurulum tamamlanmadıysa bu alanları null yapabiliriz veya hiç yazmayabiliriz.
            shopsConfig.set(path + ".itemType", null);
            shopsConfig.set(path + ".itemQuantityForPrice", null);
            shopsConfig.set(path + ".price", null);
        }

        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "shops.yml dosyasına kaydedilirken hata oluştu!", e);
        }
    }

    public Map<Location, Shop> loadShops() {
        Map<Location, Shop> loadedShops = new HashMap<>();
        ConfigurationSection shopsSection = shopsConfig.getConfigurationSection("shops");
        if (shopsSection == null) {
            return loadedShops;
        }

        for (String locString : shopsSection.getKeys(false)) {
            Location location = Shop.stringToLocation(locString);
            if (location == null) {
                plugin.getLogger().warning("shops.yml içinde geçersiz konum formatı: " + locString);
                continue;
            }
            String path = "shops." + locString;
            try {
                UUID ownerUUID = UUID.fromString(shopsConfig.getString(path + ".ownerUUID"));
                ShopType shopType = ShopType.valueOf(shopsConfig.getString(path + ".shopType", ShopType.TRADE_CHEST.name()));
                boolean setupComplete = shopsConfig.getBoolean(path + ".setupComplete", false);

                if (setupComplete) {
                    Material itemType = Material.valueOf(shopsConfig.getString(path + ".itemType"));
                    int itemQuantity = shopsConfig.getInt(path + ".itemQuantityForPrice");
                    double price = shopsConfig.getDouble(path + ".price");
                    loadedShops.put(location, new Shop(location, ownerUUID, shopType, itemType, itemQuantity, price, true));
                } else {
                    loadedShops.put(location, new Shop(location, ownerUUID, shopType)); // Kurulumu tamamlanmamış
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("shops.yml içindeki '" + locString + "' mağazası yüklenirken hata: " + e.getMessage());
            }
        }
        plugin.getLogger().info(loadedShops.size() + " mağaza shops.yml dosyasından yüklendi.");
        return loadedShops;
    }

    public void removeShop(Location location) {
        if (location == null) return;
        String locString = Shop.locationToString(location);
        shopsConfig.set("shops." + locString, null);
        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "shops.yml dosyasına kaydedilirken hata oluştu (mağaza silinirken)!", e);
        }
    }
}