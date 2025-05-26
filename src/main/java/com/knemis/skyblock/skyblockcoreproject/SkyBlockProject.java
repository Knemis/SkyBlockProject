package com.knemis.skyblock.skyblockcoreproject;

import com.knemis.skyblock.skyblockcoreproject.commands.IslandCommand;
import com.knemis.skyblock.skyblockcoreproject.economy.worth.IslandWorthManager;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // Yeni eklendi
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandSettingsManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.FlagGUIListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.IslandWelcomeListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopSetupListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopVisitListener;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkyBlockProject extends JavaPlugin {

    // Ada Yönetimi
    private IslandDataHandler islandDataHandler;
    private IslandLifecycleManager islandLifecycleManager;
    private IslandSettingsManager islandSettingsManager;
    private IslandMemberManager islandMemberManager;
    private IslandTeleportManager islandTeleportManager;

    // Ada Özellikleri
    private IslandFlagManager islandFlagManager;
    private IslandBiomeManager islandBiomeManager;
    private IslandWelcomeManager islandWelcomeManager;

    // Ekonomi ve Mağaza Yönetimi
    private IslandWorthManager islandWorthManager;
    private ShopManager shopManager;
    private ShopSetupGUIManager shopSetupGUIManager;
    private ShopVisitGUIManager shopVisitGUIManager;
    private ShopAdminGUIManager shopAdminGUIManager; // Yeni eklendi

    // GUI Yöneticileri
    private FlagGUIManager flagGUIManager;

    // Diğer
    private int nextIslandX;
    private WorldGuard worldGuardInstance;
    private LuckPerms luckPermsApi;
    private Economy vaultEconomy = null;

    // Oyuncu Durum Takibi
    private final Map<UUID, Location> playerShopSetupState = new HashMap<>();
    private final Map<UUID, Location> playerViewingShopLocation = new HashMap<>();
    private final Map<UUID, Location> playerAdministeringShop = new HashMap<>(); // Yeni eklendi: Hangi oyuncu hangi dükkanı yönetiyor
    private final Map<UUID, ShopAdminGUIManager.AdminInputType> playerWaitingForAdminInput = new HashMap<>(); // Yeni eklendi: Oyuncudan beklenen admin girişi türü

    @Override
    public void onEnable() {
        // 1. Config Yükleme ve Varsayılanlar
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("general.next-island-x", 0);
        getConfig().addDefault("skyblock-world-name", "skyblock_world");
        getConfig().addDefault("island.worth.block_values.DIAMOND_BLOCK", 150.0);
        getConfig().addDefault("island.worth.level_requirements.1", 0.0);
        getConfig().addDefault("island.worth.level_up_rewards.2", "eco give {player} 250");
        saveConfig();

        this.nextIslandX = getConfig().getInt("general.next-island-x", 0);
        getLogger().info("SkyBlockProject Eklentisi Aktif Ediliyor...");

        // 2. Bağımlılık Kontrolleri ve Kurulumları
        if (!setupLuckPerms()) {
            getLogger().warning("LuckPerms API bulunamadı! Ada sahibi bypass izinleri otomatik olarak ATANAMAYACAK.");
        }
        if (!setupEconomyVault()) {
            getLogger().severe("Vault ile ekonomi sistemi kurulamadı! Ekonomi özellikleri devre dışı kalacak.");
        } else {
            getLogger().info("Vault ile ekonomi sistemi başarıyla kuruldu!");
            EconomyManager.setupEconomy();
        }
        if (!hookPlugin("WorldEdit") || !setupWorldGuard()) {
            getLogger().severe("Gerekli bağımlılıklar (WorldEdit/WorldGuard) bulunamadı veya aktif değil! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("WorldEdit ve WorldGuard başarıyla bulundu ve aktif.");

        // 3. Ana Veri Yöneticisi
        this.islandDataHandler = new IslandDataHandler(this);
        this.islandDataHandler.loadSkyblockWorld();
        this.islandDataHandler.loadIslandsFromConfig();

        // 4. Ada Özellik Yöneticileri
        this.islandFlagManager = new IslandFlagManager(this, this.islandDataHandler);
        this.islandSettingsManager = new IslandSettingsManager(this, this.islandDataHandler);
        this.islandTeleportManager = new IslandTeleportManager(this, this.islandDataHandler);

        // 5. Ada Yaşam Döngüsü Yöneticisi
        this.islandLifecycleManager = new IslandLifecycleManager(this, this.islandDataHandler, this.islandFlagManager);

        // 6. Ekonomi ve Mağaza Sistemi Yöneticileri
        this.islandWorthManager = new IslandWorthManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.shopManager = new ShopManager(this);
        this.shopSetupGUIManager = new ShopSetupGUIManager(this, this.shopManager);
        this.shopVisitGUIManager = new ShopVisitGUIManager(this, this.shopManager);
        this.shopAdminGUIManager = new ShopAdminGUIManager(this, this.shopManager); // Yeni eklendi: Başlatma

        // 7. Diğer Ada Yöneticileri
        this.islandMemberManager = new IslandMemberManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandBiomeManager = new IslandBiomeManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandWelcomeManager = new IslandWelcomeManager(this, this.islandDataHandler);

        // 8. GUI Yöneticileri
        this.flagGUIManager = new FlagGUIManager(this, this.islandDataHandler, this.islandFlagManager);

        // 9. Listener Kayıtları
        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, this.flagGUIManager), this);
        getServer().getPluginManager().registerEvents(new IslandWelcomeListener(this, this.islandDataHandler, this.islandWelcomeManager), this);
        // ShopListener constructor'ı güncelleneceği için ShopAdminGUIManager eklendi
        getServer().getPluginManager().registerEvents(new ShopListener(this, this.shopManager, this.shopSetupGUIManager, this.islandDataHandler, this.shopVisitGUIManager, this.shopAdminGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopSetupListener(this, this.shopManager, this.shopSetupGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopVisitListener(this, this.shopManager, this.shopVisitGUIManager), this);

        // 10. Komut Kayıtları
        IslandCommand islandCommandExecutor = new IslandCommand(
                this, this.islandDataHandler, this.islandLifecycleManager, this.islandSettingsManager,
                this.islandMemberManager, this.islandTeleportManager, this.islandBiomeManager,
                this.islandWelcomeManager, this.flagGUIManager, this.islandWorthManager
        );
        if (getCommand("island") != null) {
            getCommand("island").setExecutor(islandCommandExecutor);
            getCommand("island").setTabCompleter(islandCommandExecutor);
        } else {
            getLogger().severe("'island' komutu plugin.yml dosyasında tanımlanmamış!");
        }

        getLogger().info("SkyBlockProject Eklentisi Başarıyla Aktif Edildi!");
    }

    @Override
    public void onDisable() {
        if (islandDataHandler != null) {
            islandDataHandler.saveAllIslandsToDisk();
        }
        getLogger().info("SkyBlockProject Eklentisi Devre Dışı Bırakıldı.");
    }

    // --- Bağımlılık Kurulum Metodları ---
    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPermsApi = provider.getProvider();
            getLogger().info("LuckPerms API başarıyla bağlandı.");
            return true;
        }
        getLogger().warning("LuckPerms API bulunamadı!");
        return false;
    }

    private boolean setupEconomyVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugini bulunamadı! Ekonomi özellikleri kullanılamayacak.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault için kayıtlı bir ekonomi sağlayıcısı bulunamadı! (EssentialsX Economy vb. yüklü mü?)");
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null || !wgPlugin.isEnabled()) {
            getLogger().severe("WorldGuard eklentisi bulunamadı veya aktif değil!");
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

    // --- Getter Metodları ---
    public Economy getEconomy() {
        return vaultEconomy;
    }

    public IslandDataHandler getIslandDataHandler() { return islandDataHandler; }
    public IslandLifecycleManager getIslandLifecycleManager() { return islandLifecycleManager; }
    public IslandSettingsManager getIslandSettingsManager() { return islandSettingsManager; }
    public IslandMemberManager getIslandMemberManager() { return islandMemberManager; }
    public IslandTeleportManager getIslandTeleportManager() { return islandTeleportManager; }
    public IslandFlagManager getIslandFlagManager() { return islandFlagManager; }
    public IslandBiomeManager getIslandBiomeManager() { return islandBiomeManager; }
    public IslandWelcomeManager getIslandWelcomeManager() { return islandWelcomeManager; }
    public IslandWorthManager getIslandWorthManager() { return islandWorthManager; }
    public ShopManager getShopManager() { return shopManager; }
    public ShopSetupGUIManager getShopSetupGUIManager() { return shopSetupGUIManager; }
    public ShopVisitGUIManager getShopVisitGUIManager() { return shopVisitGUIManager; }
    public ShopAdminGUIManager getShopAdminGUIManager() { return shopAdminGUIManager; } // Yeni eklendi
    public FlagGUIManager getFlagGUIManager() { return flagGUIManager; }
    public LuckPerms getLuckPermsApi() { return luckPermsApi; }

    public Map<UUID, Location> getPlayerShopSetupState() { return playerShopSetupState; }
    public Map<UUID, Location> getPlayerViewingShopLocation() { return playerViewingShopLocation; }
    public Map<UUID, Location> getPlayerAdministeringShop() { return playerAdministeringShop; } // Yeni eklendi
    public Map<UUID, ShopAdminGUIManager.AdminInputType> getPlayerWaitingForAdminInput() { return playerWaitingForAdminInput; } // Yeni eklendi


    public int getNextIslandXAndIncrement() {
        int currentX = this.nextIslandX;
        this.nextIslandX += getConfig().getInt("island.spacing", 300);
        getConfig().set("general.next-island-x", this.nextIslandX);
        // saveConfig(); // Her X artışında config kaydetmek yerine onDisable'da veya periyodik kaydetmek daha iyi olabilir.
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
}