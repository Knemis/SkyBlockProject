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
                plugin.getLogger().severe("Plugin veri klasörü oluşturulamadı: " + plugin.getDataFolder().getPath());
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
                plugin.getLogger().info(shopsFile.getName() + " oluşturuldu ve varsayılan yapıyla kaydedildi.");
            } else {
                shopsConfig.load(shopsFile);
                int fileVersion = shopsConfig.getInt("file-version", 0);
                if (fileVersion < CURRENT_DATA_VERSION) {
                    plugin.getLogger().warning(shopsFile.getName() + " eski bir veri formatında (Versiyon: " + fileVersion + "). Mevcut versiyon: " + CURRENT_DATA_VERSION);
                    // Burada eski veriyi yeni formata dönüştürme (migrasyon) kodu eklenebilir.
                    // Şimdilik sadece uyarı veriyoruz. Yeni bir kayıtta dosya versiyonu güncellenecektir.
                }
                plugin.getLogger().info(shopsFile.getName() + " başarıyla yüklendi.");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " oluşturulurken/yüklenirken G/Ç hatası!", e);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " geçersiz YAML formatına sahip! Lütfen kontrol edin.", e);
            plugin.getLogger().warning("Hata nedeniyle yedek dosyadan (" + shopsBackupFile.getName() + ") yükleme deneniyor...");
            if (shopsBackupFile.exists()) {
                try {
                    shopsConfig.load(shopsBackupFile);
                    plugin.getLogger().info(shopsBackupFile.getName() + " başarıyla yedekten yüklendi.");
                } catch (IOException | InvalidConfigurationException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Yedek dosyadan ("+ shopsBackupFile.getName() +") yükleme de başarısız oldu!", ex);
                }
            } else {
                plugin.getLogger().warning("Yedek dosya (" + shopsBackupFile.getName() + ") bulunamadı.");
            }
        }
    }

    private void saveConfigFile() {
        try {
            if (shopsFile.exists()) {
                try {
                    Files.copy(shopsFile.toPath(), shopsBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().fine(shopsFile.getName() + " dosyası " + shopsBackupFile.getName() + " olarak yedeklendi.");
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.WARNING, shopsFile.getName() + " yedeklenirken hata oluştu!", ex);
                }
            }
            shopsConfig.set("file-version", CURRENT_DATA_VERSION);
            shopsConfig.save(shopsFile);
            plugin.getLogger().fine(shopsFile.getName() + " dosyasına değişiklikler kaydedildi.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, shopsFile.getName() + " dosyasına kaydedilirken G/Ç hatası oluştu!", e);
        }
    }

    public void saveShop(Shop shop) {
        if (shop == null || shop.getLocation() == null) {
            plugin.getLogger().warning("Kaydedilecek mağaza veya konumu null.");
            return;
        }
        String locString = Shop.locationToString(shop.getLocation());
        if (locString.isEmpty()) {
            plugin.getLogger().warning("Mağaza konumu string'e çevrilemedi, kaydedilemiyor: Sahip UUID " + shop.getOwnerUUID());
            return;
        }
        String path = "shops." + locString;

        shopsConfig.set(path, shop.serialize()); // Shop nesnesinin serialize metodunu kullan
        saveConfigFile();
        plugin.getLogger().fine("Mağaza (" + locString + ") shops.yml dosyasına kaydedildi/güncellendi.");
    }

    @SuppressWarnings("unchecked") // ItemStack deserileştirme için
    public Map<Location, Shop> loadShops() {
        Map<Location, Shop> loadedShops = new HashMap<>();
        try {
            shopsConfig.load(shopsFile); // Her yüklemede dosyadan en güncel veriyi oku
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Mağazalar yüklenirken shops.yml okunamadı. Yedekten deneniyor.", e);
            if (shopsBackupFile.exists()) {
                try {
                    shopsConfig.load(shopsBackupFile);
                    plugin.getLogger().info(shopsBackupFile.getName() + " başarıyla yedekten yüklendi (loadShops).");
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "Yedek dosyadan yükleme de başarısız oldu (loadShops)!", ex);
                    return loadedShops;
                }
            } else {
                plugin.getLogger().warning("Yedek dosya (loadShops) bulunamadı.");
                return loadedShops;
            }
        }

        ConfigurationSection shopsSection = shopsConfig.getConfigurationSection("shops");
        if (shopsSection == null) {
            plugin.getLogger().info("shops.yml'de 'shops' bölümü bulunamadı veya boş. Hiçbir mağaza yüklenmedi.");
            return loadedShops;
        }

        int successfullyLoaded = 0;
        int failedToLoad = 0;

        for (String locStringKey : shopsSection.getKeys(false)) {
            // DÜZELTME: 'shopsShops' yerine 'shopsSection' kullanılacak.
            ConfigurationSection shopMapSection = shopsSection.getConfigurationSection(locStringKey);
            if (shopMapSection == null) {
                plugin.getLogger().warning("Mağaza verisi okunamadı (null section) key: " + locStringKey + " altında.");
                failedToLoad++;
                continue;
            }
            Map<String, Object> shopDataMap = shopMapSection.getValues(false);

            // Shop.deserialize metodu doğrudan Map alıyor ve içinden "location" string'ini de okuyor.
            // Bu yüzden locStringKey'i ayrıca deserialize'a göndermeye gerek yok,
            // ancak Shop.deserialize içinde "location" anahtarının map'te olduğundan emin olmalıyız.
            // Shop.serialize() metodu "location" anahtarını ekliyor.

            Shop shop = null;
            try {
                shop = Shop.deserialize(shopDataMap);
                if (shop != null && shop.getLocation() != null) { // Konumun başarıyla deserialize edildiğinden emin ol
                    loadedShops.put(shop.getLocation(), shop);
                    successfullyLoaded++;
                } else {
                    plugin.getLogger().warning("Mağaza deserileştirilemedi veya konumu hatalı: " + locStringKey + (shop == null ? " (Shop null döndü)" : " (Konum null)"));
                    failedToLoad++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "shops.yml içindeki mağaza yüklenirken kritik hata: " + locStringKey, e);
                failedToLoad++;
            }
        }
        plugin.getLogger().info(successfullyLoaded + " mağaza başarıyla yüklendi. " + failedToLoad + " mağaza yüklenemedi.");
        return loadedShops;
    }

    public void removeShop(Location location) {
        if (location == null) return;
        String locString = Shop.locationToString(location);
        if (locString.isEmpty()) {
            plugin.getLogger().warning("Silinecek mağaza için geçersiz konum string'i.");
            return;
        }

        if (shopsConfig.contains("shops." + locString)) {
            shopsConfig.set("shops." + locString, null);
            saveConfigFile();
            plugin.getLogger().info("Mağaza verisi (" + locString + ") shops.yml dosyasından silindi.");
        } else {
            plugin.getLogger().warning("Silinecek mağaza (" + locString + ") shops.yml dosyasında bulunamadı.");
        }
    }
}