// com/knemis/skyblock/skyblockcoreproject/shop/ShopStorage.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ShopStorage {

    private final SkyBlockProject plugin;
    private final File shopsFile;
    private final File shopsBackupFile;
    private FileConfiguration shopsConfig;
    private static final int CURRENT_DATA_VERSION = 1;

    public ShopStorage(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        this.shopsBackupFile = new File(plugin.getDataFolder(), "shops.yml.bak");

        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().severe("Plugin data folder could not be created: " + plugin.getDataFolder().getPath());
            }
        }
        loadAndInitializeConfigFile();
    }

    private void loadAndInitializeConfigFile() {
        this.shopsConfig = new YamlConfiguration();
        try {
            if (!shopsFile.exists()) {
                if (shopsFile.getParentFile() != null && !shopsFile.getParentFile().exists()){
                    shopsFile.getParentFile().mkdirs();
                }
                shopsFile.createNewFile();
                shopsConfig.set("file-version", CURRENT_DATA_VERSION);
                shopsConfig.createSection("shops");
                shopsConfig.save(shopsFile);
                plugin.getLogger().info(shopsFile.getName() + " created and saved with default structure.");
            } else {
                shopsConfig.load(shopsFile);
                int fileVersion = shopsConfig.getInt("file-version", 0);
                if (fileVersion < CURRENT_DATA_VERSION) {
                    plugin.getLogger().warning(shopsFile.getName() + " is in an old data format (Version: " + fileVersion + "). Current version: " + CURRENT_DATA_VERSION);
                    // Code for converting (migrating) old data to the new format can be added here.
                    // For now, we are just giving a warning. The file version will be updated on a new save.
                }
                plugin.getLogger().info(shopsFile.getName() + " successfully loaded.");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " I/O error while creating/loading!", e);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " has an invalid YAML format! Please check.", e);
            plugin.getLogger().warning("Attempting to load from backup file (" + shopsBackupFile.getName() + ") due to error...");
            if (shopsBackupFile.exists()) {
                try {
                    shopsConfig.load(shopsBackupFile);
                    plugin.getLogger().info(shopsBackupFile.getName() + " successfully loaded from backup.");
                } catch (IOException | InvalidConfigurationException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Loading from backup file ("+ shopsBackupFile.getName() +") also failed!", ex);
                }
            } else {
                plugin.getLogger().warning("Backup file (" + shopsBackupFile.getName() + ") not found.");
            }
        }
    }

    private void saveConfigFile() {
        try {
            if (shopsFile.exists()) {
                try {
                    Files.copy(shopsFile.toPath(), shopsBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().fine(shopsFile.getName() + " file backed up as " + shopsBackupFile.getName() + ".");
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.WARNING, shopsFile.getName() + " error occurred while backing up!", ex);
                }
            }
            shopsConfig.set("file-version", CURRENT_DATA_VERSION);
            shopsConfig.save(shopsFile);
            plugin.getLogger().fine(shopsFile.getName() + " changes saved to file.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " I/O error while saving to file!", e);
        }
    }

    public void saveShop(Shop shop) {
        if (shop == null || shop.getLocation() == null) {
            plugin.getLogger().warning("[ShopStorage] saveShop called but shop or its location is null. Shop: " + shop);
            return;
        }
        String locString = Shop.locationToString(shop.getLocation());
        if (locString.isEmpty()) {
            plugin.getLogger().warning(String.format("[ShopStorage] Shop location could not be converted to string for shop (Owner: %s), cannot save. Shop ID for ref: %s.",
                    shop.getOwnerUUID(), shop.getShopId()));
            return;
        }
        plugin.getLogger().info(String.format("[ShopStorage] Attempting to save shop at %s (Owner: %s).", locString, shop.getOwnerUUID()));
        String path = "shops." + locString;

        try {
            shopsConfig.set(path, shop.serialize());
            saveConfigFile(); // This method already logs its own success/failure at FINE/SEVERE
            plugin.getLogger().info(String.format("[ShopStorage] Shop at %s (Owner: %s) successfully saved/updated to shops.yml.", locString, shop.getOwnerUUID()));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, String.format("[ShopStorage] Failed to save shop at %s (Owner: %s) due to an exception during serialization or saving config.",
                    locString, shop.getOwnerUUID()), e);
        }
    }

    public void saveAllShops(Map<Location, Shop> shopsToSave) {
        if (shopsConfig == null) {
            plugin.getLogger().severe("[ShopStorage] Cannot save all shops, shopsConfig is null!");
            return;
        }
        if (shopsToSave == null) {
            plugin.getLogger().warning("[ShopStorage] Attempted to save all shops, but the provided map was null. Clearing shops in config.");
            shopsConfig.set("shops", null);
            saveConfigFile();
            return;
        }

        plugin.getLogger().info(String.format("[ShopStorage] Attempting to save all %d shops.", shopsToSave.size()));
        shopsConfig.set("shops", null); // Clear existing section to remove deleted shops

        if (shopsToSave.isEmpty()) {
            plugin.getLogger().info("[ShopStorage] No active shops to save to shops.yml.");
        } else {
            int savedCount = 0;
            for (Shop shop : shopsToSave.values()) {
                if (shop == null || shop.getLocation() == null) {
                    plugin.getLogger().warning("[ShopStorage] Skipping save for a null shop or shop with null location during saveAllShops.");
                    continue;
                }
                String locString = Shop.locationToString(shop.getLocation());
                if (locString.isEmpty()) {
                    plugin.getLogger().warning(String.format("[ShopStorage] Shop location could not be converted to string for shop (Owner: %s) during saveAllShops. Shop not saved. Shop ID for ref: %s.",
                            shop.getOwnerUUID(), shop.getShopId()));
                    continue;
                }
                String path = "shops." + locString;
                try {
                    shopsConfig.set(path, shop.serialize());
                    savedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, String.format("[ShopStorage] Failed to serialize shop at %s (Owner: %s) during saveAllShops.",
                            locString, shop.getOwnerUUID()), e);
                }
            }
            plugin.getLogger().info(String.format("[ShopStorage] %d shops prepared for saving. (Total attempted: %d)", savedCount, shopsToSave.size()));
        }
        saveConfigFile(); // This logs actual file write success/failure
    }


    @SuppressWarnings("unchecked")
    public Map<Location, Shop> loadShops() {
        plugin.getLogger().info("[ShopStorage] Attempting to load shops from shops.yml...");
        Map<Location, Shop> loadedShops = new HashMap<>();
        try {
            shopsConfig.load(shopsFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "[ShopStorage] Could not read shops.yml while loading shops. Attempting to load from backup.", e);
            if (shopsBackupFile.exists()) {
                try {
                    shopsConfig.load(shopsBackupFile);
                    plugin.getLogger().info("[ShopStorage] Successfully loaded shops from backup file: " + shopsBackupFile.getName());
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "[ShopStorage] Loading from backup file " + shopsBackupFile.getName() + " also failed!", ex);
                    return loadedShops; // Return empty map, as primary and backup failed
                }
            } else {
                plugin.getLogger().warning("[ShopStorage] Backup file " + shopsBackupFile.getName() + " not found. Cannot load shops.");
                return loadedShops; // Return empty map
            }
        }

        ConfigurationSection shopsSection = shopsConfig.getConfigurationSection("shops");
        if (shopsSection == null) {
            plugin.getLogger().info("[ShopStorage] 'shops' section not found or empty in shops.yml. No shops loaded.");
            return loadedShops;
        }

        int successfullyLoaded = 0;
        int failedToLoad = 0;

        for (String locStringKey : shopsSection.getKeys(false)) {
            ConfigurationSection shopMapSection = shopsSection.getConfigurationSection(locStringKey);
            if (shopMapSection == null) {
                plugin.getLogger().warning(String.format("[ShopStorage] Corrupt shop entry: Could not read shop data (null section) under key: %s.", locStringKey));
                failedToLoad++;
                continue;
            }
            Map<String, Object> shopDataMap = shopMapSection.getValues(false);
            Shop shop = null;
            try {
                // Pass the key itself for context in case location deserialization fails
                shopDataMap.put("_locStringKeyForDebug", locStringKey);
                shop = Shop.deserialize(shopDataMap);
                if (shop != null && shop.getLocation() != null) {
                    loadedShops.put(shop.getLocation(), shop);
                    successfullyLoaded++;
                    plugin.getLogger().fine(String.format("[ShopStorage] Successfully deserialized shop at key %s, Location: %s.", locStringKey, Shop.locationToString(shop.getLocation())));
                } else {
                    plugin.getLogger().warning(String.format("[ShopStorage] Corrupt shop entry: Shop could not be fully deserialized or location is incorrect for key: %s. Shop object (ref ID %s): %s",
                            locStringKey, (shop == null ? "null" : "location_null")));
                    failedToLoad++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, String.format("[ShopStorage] Critical error while deserializing shop from shops.yml at key: %s. Data: %s",
                        locStringKey, shopDataMap), e);
                failedToLoad++;
            }
        }
        plugin.getLogger().info(String.format("[ShopStorage] Shop loading complete. Successfully loaded: %d shops. Failed/Corrupt entries: %d shops.", successfullyLoaded, failedToLoad));
        return loadedShops;
    }

    public void removeShop(Location location) {
        if (location == null) {
            plugin.getLogger().warning("[ShopStorage] removeShop called with null location.");
            return;
        }
        String locString = Shop.locationToString(location);
        if (locString.isEmpty()) {
            plugin.getLogger().warning("[ShopStorage] Invalid location string for shop to be deleted. Location object: " + location.toString());
            return;
        }
        plugin.getLogger().info(String.format("[ShopStorage] Attempting to remove shop data for location %s from shops.yml.", locString));

        if (shopsConfig.contains("shops." + locString)) {
            shopsConfig.set("shops." + locString, null);
            saveConfigFile(); // This logs success/failure of file write
            plugin.getLogger().info(String.format("[ShopStorage] Shop data for location %s successfully marked for deletion in shops.yml and config saved.", locString));
        } else {
            plugin.getLogger().warning(String.format("[ShopStorage] Shop data for location %s to be deleted was not found in shops.yml.", locString));
        }
    }
}