package com.knemis.skyblock.skyblockcoreproject;

import com.knemis.skyblock.skyblockcoreproject.commands.IslandCommand;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.WorldGuard; // WorldGuard ana sınıfı
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.FlagGUIListener;


import org.bukkit.World; // Bukkit World importu



public final class SkyBlockProject extends JavaPlugin {

    private IslandManager islandManager;
    private int nextIslandX;
    // Adalar arası X eksenindeki varsayılan mesafe.
    // Bu değeri istersen config.yml'den de okunabilir hale getirebilirsin.
    private final int islandSpacing = 300;
    private WorldGuard worldGuardInstance;
    private FlagGUIManager flagGUIManager; // YENİ


    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("general.next-island-x", 0);
        getConfig().addDefault("island-creation-cooldown-seconds", 300);
        getConfig().addDefault("skyblock-world-name", "skyblock_world");
        getConfig().addDefault("island-spawn-offset.x", 0.5);
        getConfig().addDefault("island-spawn-offset.y", 1.5);
        getConfig().addDefault("island-spawn-offset.z", 0.5);
        getConfig().addDefault("island.max-named-homes", 5);
        getConfig().addDefault("island.expansion-radius-horizontal", 50); // Yatayda X ve Z eksenlerinde genişleme yarıçapı
        getConfig().addDefault("island.allow-build-below-schematic-base", false); // Şematik tabanının altına inşa izni
        getConfig().addDefault("island.build-limit-above-schematic-top", 150); // Şematik tavanından ne kadar yukarı inşa edilebileceği
        saveConfig();


        // Kayıtlı 'nextIslandX' değerini config.yml dosyasından yükle.
        // Eğer değer bulunamazsa varsayılan olarak 0 kullanılır.
        this.nextIslandX = getConfig().getInt("general.next-island-x", 0);

        getLogger().info("SkyBlockProject Eklentisi Aktif Ediliyor...");

        // WorldEdit eklentisinin sunucuda yüklü ve aktif olup olmadığını kontrol et.
        if (!hookPlugin("WorldEdit") || !setupWorldGuard()) { // setupWorldGuard() çağrısı
            getLogger().severe("Gerekli bağımlılıklar (WorldEdit/WorldGuard) bulunamadı veya aktif değil! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("WorldEdit ve WorldGuard başarıyla bulundu ve aktif.");

        this.islandManager = new IslandManager(this);
        this.islandManager.loadSkyblockWorld(); // Skyblock dünyasını yükle/oluştur
        this.islandManager.loadIslands(); // Ada verilerini DÜNYA YÜKLENDİKTEN SONRA yükle
        this.flagGUIManager = new FlagGUIManager(this, this.islandManager); // YENİ: FlagGUIManager'ı başlat
        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, flagGUIManager, islandManager), this);
        getLogger().info("SkyBlockProject Eklentisi Başarıyla Aktif Edildi!");
        // Komutları kaydet
        IslandCommand islandCommandExecutor = new IslandCommand(this, islandManager); // Komut yöneticisini değişkene ata

        this.getCommand("island").setExecutor(new IslandCommand(this, islandManager));

        getLogger().info("SkyBlockProject Eklentisi Başarıyla Aktif Edildi! Bir sonraki ada için X koordinatı başlangıcı: " + this.nextIslandX);
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null /*Removed instanceof check for simplicity, getPlugin should be enough*/) {
            getLogger().severe("WorldGuard eklentisi bulunamadı!");
            return false;
        }
        // WorldGuard ana singleton nesnesini al
        this.worldGuardInstance = WorldGuard.getInstance();
        if (this.worldGuardInstance == null) {
            getLogger().severe("WorldGuard instance alınamadı!");
            return false;
        }
        getLogger().info("WorldGuard API başarıyla hooklandı.");
        return true;
    }
    /**
     * Belirtilen isimdeki bir eklentinin sunucuda yüklü ve aktif olup olmadığını kontrol eder.
     * Bu, eklentinin bağımlılıklarının karşılandığından emin olmak için kullanılır.
     *
     * @param pluginName Kontrol edilecek eklentinin adı (plugin.yml'deki gibi).
     * @return Eklenti yüklü ve aktifse true, aksi halde false döner.
     */
    private boolean hookPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public void onDisable() {
        // Eklenti devre dışı bırakılırken yapılacak temizleme işlemleri veya son kayıtlar.
        // Örneğin, nextIslandX'in son değerini garantiye almak için tekrar kaydedebilirsin,
        // ancak getNextIslandXAndIncrement metodu zaten her çağrıldığında kaydediyor.
        // getConfig().set("general.next-island-x", this.nextIslandX);
        // saveConfig();

        getLogger().info("SkyBlockProject Eklentisi Devre Dışı Bırakıldı!");
    }

    /**
     * Bir sonraki ada için kullanılacak mevcut X koordinatını döndürür.
     * Ardından, bir sonraki olası ada için X koordinatını hesaplar, günceller ve
     * bu yeni değeri config.yml dosyasına kaydeder.
     *
     * @return Yeni oluşturulacak ada için kullanılacak olan mevcut X koordinatı.
     */

    public int getNextIslandXAndIncrement() {
        getLogger().info("[DEBUG] getNextIslandXAndIncrement çağrıldı. Mevcut nextIslandX (artırmadan önce): " + this.nextIslandX + ", islandSpacing: " + this.islandSpacing);
        int currentX = this.nextIslandX;
        this.nextIslandX += this.islandSpacing;
        getConfig().set("general.next-island-x", this.nextIslandX);
        saveConfig();
        getLogger().info("[DEBUG] Ada için kullanılacak X: " + currentX + ". Config 'general.next-island-x' güncellendi: " + this.nextIslandX);
        return currentX;
    }

    public RegionManager getRegionManager(World bukkitWorld) {
        if (worldGuardInstance == null || bukkitWorld == null) {
            getLogger().severe("WorldGuard instance null veya verilen dünya null. RegionManager alınamıyor.");
            return null;
        }
        // WorldGuard platform adaptörünü al
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionContainer container = worldGuardInstance.getPlatform().getRegionContainer();
        if (container == null) {
            getLogger().severe("WorldGuard RegionContainer alınamadı!");
            return null;
        }
        return container.get(adaptedWorld);
    }
    // FlagGUIManager'a erişim için getter (YENİ)
    public FlagGUIManager getFlagGUIManager() {
        return flagGUIManager;
    }


    /**
     * IslandManager örneğine dışarıdan erişim sağlar.
     *
     * @return Başlatılmış IslandManager örneği.
     */
    public IslandManager getIslandManager() {
        return islandManager;
    }

}