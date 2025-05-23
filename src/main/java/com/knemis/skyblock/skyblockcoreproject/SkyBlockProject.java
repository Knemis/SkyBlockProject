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
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager; // EKLENDİ
import com.knemis.skyblock.skyblockcoreproject.listeners.IslandWelcomeListener;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;

import org.bukkit.World; // Bukkit World importu



public final class SkyBlockProject extends JavaPlugin {

    private IslandManager islandManager;
    private FlagGUIManager flagGUIManager;
    private IslandBiomeManager islandBiomeManager;
    private IslandWelcomeManager islandWelcomeManager;
    private IslandFlagManager islandFlagManager;

    private int nextIslandX;
    // private final int islandSpacing = 300; // Artık config'den okunuyor

    private WorldGuard worldGuardInstance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        // Config varsayılanları
        getConfig().addDefault("general.next-island-x", 0);
        getConfig().addDefault("island.creation-cooldown-seconds", 300);
        getConfig().addDefault("skyblock-world-name", "skyblock_world");
        getConfig().addDefault("island-spawn-offset.x", 0.5);
        getConfig().addDefault("island-spawn-offset.y", 1.5);
        getConfig().addDefault("island-spawn-offset.z", 0.5);
        getConfig().addDefault("island.max-named-homes", 5);
        getConfig().addDefault("island.expansion-radius-horizontal", 50);
        getConfig().addDefault("island.allow-build-below-schematic-base", false);
        getConfig().addDefault("island.build-limit-above-schematic-top", 150);
        getConfig().addDefault("island.default-name-prefix", "Ada");
        getConfig().addDefault("island.max-members", 3);
        getConfig().addDefault("island.home-name-pattern", "^[a-zA-Z0-9_]{2,16}$");
        getConfig().addDefault("island.name.pattern", "^[a-zA-Z0-9_\\- ]{3,25}$");
        getConfig().addDefault("island.name.min-length", 3);
        getConfig().addDefault("island.name.max-length", 25);
        getConfig().addDefault("island.region-priority", 10);
        getConfig().addDefault("island.spacing", 300);
        getConfig().addDefault("island.welcome-message.max-length", 100);
        getConfig().addDefault("logging.detailed-flag-changes", false); // IslandFlagManager için
        saveConfig();

        this.nextIslandX = getConfig().getInt("general.next-island-x", 0);

        getLogger().info("SkyBlockProject Eklentisi Aktif Ediliyor...");

        if (!hookPlugin("WorldEdit") || !setupWorldGuard()) {
            getLogger().severe("Gerekli bağımlılıklar (WorldEdit/WorldGuard) bulunamadı veya aktif değil! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("WorldEdit ve WorldGuard başarıyla bulundu ve aktif.");

        // Yöneticilerin doğru sırada başlatılması:
        this.islandManager = new IslandManager(this); // En temel yönetici
        this.islandManager.loadSkyblockWorld();
        this.islandManager.loadIslands();

        // Diğer özellik yöneticileri IslandManager'a bağımlı olabilir veya olmayabilir,
        // ancak GUI yöneticilerinden önce başlatılmaları genellikle daha iyidir.
        this.islandBiomeManager = new IslandBiomeManager(this, this.islandManager);
        this.islandWelcomeManager = new IslandWelcomeManager(this, this.islandManager);
        this.islandFlagManager = new IslandFlagManager(this, this.islandManager); // FlagGUIManager'dan ÖNCE

        // GUI Yöneticileri (diğer yöneticilere bağımlı olabilirler)
        this.flagGUIManager = new FlagGUIManager(this, this.islandManager); // FlagGUIManager artık islandFlagManager'ı plugin.getIslandFlagManager() ile alacak

        // Listener'lar
        // FlagGUIListener constructor'ı 2 argüman alacak şekilde güncellenmişti: (this, flagGUIManager)
        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, this.flagGUIManager), this);
        getServer().getPluginManager().registerEvents(new IslandWelcomeListener(this, this.islandManager, this.islandWelcomeManager), this);

        // Komutlar
        IslandCommand islandCommandExecutor = new IslandCommand(this, this.islandManager);
        getCommand("island").setExecutor(islandCommandExecutor);
        getCommand("island").setTabCompleter(islandCommandExecutor);
        // this.getCommand("island").setExecutor(new IslandCommand(this, islandManager)); // Bu satır fazladan ve zaten yukarıda yapıldı.

        getLogger().info("SkyBlockProject Eklentisi Başarıyla Aktif Edildi! Bir sonraki ada için X koordinatı başlangıcı: " + this.nextIslandX);
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null) {
            getLogger().severe("WorldGuard eklentisi bulunamadı!");
            return false;
        }
        this.worldGuardInstance = WorldGuard.getInstance();
        if (this.worldGuardInstance == null) {
            getLogger().severe("WorldGuard instance alınamadı!");
            return false;
        }
        getLogger().info("WorldGuard API başarıyla hooklandı.");
        return true;
    }

    private boolean hookPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public void onDisable() {
        if (islandManager != null) {
            islandManager.saveAllIslandData();
        }
        getLogger().info("SkyBlockProject Eklentisi Devre Dışı Bırakıldı!");
    }

    public int getNextIslandXAndIncrement() {
        int currentX = this.nextIslandX;
        this.nextIslandX += getConfig().getInt("island.spacing", 300);
        getConfig().set("general.next-island-x", this.nextIslandX);
        saveConfig();
        return currentX;
    }

    public RegionManager getRegionManager(World bukkitWorld) {
        if (worldGuardInstance == null || bukkitWorld == null) {
            getLogger().severe("WorldGuard instance null veya verilen dünya null. RegionManager alınamıyor.");
            return null;
        }
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionContainer container = worldGuardInstance.getPlatform().getRegionContainer();
        if (container == null) {
            getLogger().severe("WorldGuard RegionContainer alınamadı!");
            return null;
        }
        return container.get(adaptedWorld);
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public FlagGUIManager getFlagGUIManager() {
        return flagGUIManager;
    }

    public IslandBiomeManager getIslandBiomeManager() {
        return islandBiomeManager;
    }

    public IslandWelcomeManager getIslandWelcomeManager() {
        return islandWelcomeManager;
    }

    public IslandFlagManager getIslandFlagManager() {
        return islandFlagManager;
    }
}