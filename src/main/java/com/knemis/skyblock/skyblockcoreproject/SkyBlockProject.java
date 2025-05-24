package com.knemis.skyblock.skyblockcoreproject;

import com.knemis.skyblock.skyblockcoreproject.commands.IslandCommand;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.*;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.FlagGUIListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.IslandWelcomeListener;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public final class SkyBlockProject extends JavaPlugin {

    // --- New Specific Managers ---
    private IslandDataHandler islandDataHandler;
    private IslandLifecycleManager islandLifecycleManager;
    private IslandSettingsManager islandSettingsManager;
    private IslandMemberManager islandMemberManager;
    private IslandTeleportManager islandTeleportManager;

    // --- Existing Feature Managers (constructors will be adapted) ---
    private IslandFlagManager islandFlagManager;
    private IslandBiomeManager islandBiomeManager;
    private IslandWelcomeManager islandWelcomeManager;
    private FlagGUIManager flagGUIManager;

    private int nextIslandX;
    private WorldGuard worldGuardInstance;
    private LuckPerms luckPermsApi;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        // Config defaults from source [cite: 8, 9, 10]
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
        getConfig().addDefault("logging.detailed-flag-changes", false);
        saveConfig(); // [cite: 11]

        this.nextIslandX = getConfig().getInt("general.next-island-x", 0); // [cite: 11]

        getLogger().info("SkyBlockProject Eklentisi Aktif Ediliyor...");

        // Load dependencies (LuckPerms, WorldEdit, WorldGuard)
        if (!setupLuckPerms()) {
            getLogger().warning("LuckPerms API bulunamadı! Ada sahibi bypass izinleri otomatik olarak ATANAMAYACAK."); // [cite: 13]
        }
        if (!hookPlugin("WorldEdit") || !setupWorldGuard()) { // [cite: 14]
            getLogger().severe("Gerekli bağımlılıklar (WorldEdit/WorldGuard) bulunamadı veya aktif değil! Eklenti devre dışı bırakılıyor."); // [cite: 14]
            getServer().getPluginManager().disablePlugin(this); // [cite: 15]
            return;
        }
        getLogger().info("WorldEdit ve WorldGuard başarıyla bulundu ve aktif."); // [cite: 15]

        // --- Manager Initialization Order ---

        // 1. Core Data Handler
        this.islandDataHandler = new IslandDataHandler(this);
        this.islandDataHandler.loadSkyblockWorld(); // Creates or loads the skyblock world
        this.islandDataHandler.loadIslandsFromConfig(); // Loads island data from islands.yml

        // 2. Feature Managers that primarily depend on IslandDataHandler or base plugin
        //    (Assuming their constructors will be refactored to accept new dependencies)

        // IslandFlagManager will depend on IslandDataHandler (for Island objects)
        // and use plugin.getRegionManager() internally, instead of the old IslandManager.
        this.islandFlagManager = new IslandFlagManager(this, this.islandDataHandler);

        this.islandSettingsManager = new IslandSettingsManager(this, this.islandDataHandler);
        this.islandTeleportManager = new IslandTeleportManager(this, this.islandDataHandler);

        // 3. Lifecycle Manager (depends on IslandDataHandler and IslandFlagManager)
        this.islandLifecycleManager = new IslandLifecycleManager(this, this.islandDataHandler, this.islandFlagManager);

        // 4. Managers that might depend on IslandLifecycleManager for more complex operations or specific data access.
        // IslandMemberManager will depend on IslandDataHandler and IslandLifecycleManager (for region context)
        // instead of the old IslandManager.
        this.islandMemberManager = new IslandMemberManager(this, this.islandDataHandler, this.islandLifecycleManager);

        // IslandBiomeManager will depend on IslandDataHandler and IslandLifecycleManager
        // (for territory and saving island state) instead of the old IslandManager.
        this.islandBiomeManager = new IslandBiomeManager(this, this.islandDataHandler, this.islandLifecycleManager);

        // IslandWelcomeManager will depend on IslandDataHandler (for island data and saving)
        // instead of the old IslandManager.
        this.islandWelcomeManager = new IslandWelcomeManager(this, this.islandDataHandler);

        // 5. GUI Managers
        // FlagGUIManager will depend on IslandDataHandler (to get island for player) and IslandFlagManager
        // instead of the old IslandManager.
        this.flagGUIManager = new FlagGUIManager(this, this.islandDataHandler, this.islandFlagManager);

        // 6. Listeners (Their constructors will be updated to use new manager dependencies)
        // FlagGUIListener's constructor in KodlarProje.txt is (SkyBlockProject, FlagGUIManager) which is fine.
        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, this.flagGUIManager), this); // [cite: 21]
        // IslandWelcomeListener's constructor will change from (plugin, IslandManager, IslandWelcomeManager)
        // to (plugin, IslandDataHandler, IslandWelcomeManager).
        getServer().getPluginManager().registerEvents(new IslandWelcomeListener(this, this.islandDataHandler, this.islandWelcomeManager), this); // [cite: 22]

        // 7. Commands
        // IslandCommand's constructor will be refactored to take all necessary new specific managers.
        IslandCommand islandCommandExecutor = new IslandCommand(
                this,
                this.islandDataHandler,
                this.islandLifecycleManager,
                this.islandSettingsManager,
                this.islandMemberManager,
                this.islandTeleportManager,
                this.islandBiomeManager,
                this.islandWelcomeManager,
                this.flagGUIManager // FlagGUIManager is passed for /is flags
        );
        getCommand("island").setExecutor(islandCommandExecutor); // [cite: 23]
        getCommand("island").setTabCompleter(islandCommandExecutor); // [cite: 23]

        getLogger().info("SkyBlockProject Eklentisi Başarıyla Aktif Edildi! Bir sonraki ada için X koordinatı başlangıcı: " + this.nextIslandX); // [cite: 24]
    }

    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class); // [cite: 12]
        if (provider != null) {
            this.luckPermsApi = provider.getProvider(); // [cite: 12]
            getLogger().info("LuckPerms API başarıyla bağlandı."); // [cite: 13]
            return true;
        }
        // this.luckPermsApi will remain null, null checks should be performed in other code. [cite: 14]
        return false;
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard"); // [cite: 25]
        if (wgPlugin == null) {
            getLogger().severe("WorldGuard eklentisi bulunamadı!"); // [cite: 26]
            return false; // [cite: 27]
        }
        this.worldGuardInstance = WorldGuard.getInstance(); // [cite: 27]
        if (this.worldGuardInstance == null) {
            getLogger().severe("WorldGuard instance alınamadı!"); // [cite: 28]
            return false; // [cite: 29]
        }
        getLogger().info("WorldGuard API başarıyla hooklandı.");
        return true;
    }

    private boolean hookPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName); // [cite: 30]
        return plugin != null && plugin.isEnabled(); // [cite: 31]
    }

    @Override
    public void onDisable() {
        if (islandDataHandler != null) {
            // Use the new IslandDataHandler to save all island data
            islandDataHandler.saveAllIslandsToDisk();
        }
        getLogger().info("SkyBlockProject Eklentisi Devre Dışı Bırakıldı!"); // [cite: 32]
    }

    public int getNextIslandXAndIncrement() {
        int currentX = this.nextIslandX; // [cite: 33]
        this.nextIslandX += getConfig().getInt("island.spacing", 300); // [cite: 34]
        getConfig().set("general.next-island-x", this.nextIslandX); // [cite: 34]
        saveConfig();
        return currentX;
    }

    public RegionManager getRegionManager(World bukkitWorld) {
        if (worldGuardInstance == null || bukkitWorld == null) {
            getLogger().severe("WorldGuard instance null veya verilen dünya null. RegionManager alınamıyor."); // [cite: 34]
            return null; // [cite: 35]
        }
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld); // [cite: 35]
        RegionContainer container = worldGuardInstance.getPlatform().getRegionContainer(); // [cite: 35]
        if (container == null) {
            getLogger().severe("WorldGuard RegionContainer alınamadı!"); // [cite: 36]
            return null; // [cite: 37]
        }
        return container.get(adaptedWorld); // [cite: 37]
    }

    // --- Getters for New Managers ---
    public IslandDataHandler getIslandDataHandler() {
        return islandDataHandler;
    }

    public IslandLifecycleManager getIslandLifecycleManager() {
        return islandLifecycleManager;
    }

    public IslandSettingsManager getIslandSettingsManager() {
        return islandSettingsManager;
    }

    public IslandMemberManager getIslandMemberManager() {
        return islandMemberManager;
    }

    public IslandTeleportManager getIslandTeleportManager() {
        return islandTeleportManager;
    }

    // --- Getters for Existing Managers ---
    public IslandFlagManager getIslandFlagManager() { // [cite: 43]
        return islandFlagManager;
    }

    public FlagGUIManager getFlagGUIManager() { // [cite: 40]
        return flagGUIManager;
    }

    public IslandBiomeManager getIslandBiomeManager() { // [cite: 41]
        return islandBiomeManager;
    }

    public IslandWelcomeManager getIslandWelcomeManager() { // [cite: 42]
        return islandWelcomeManager;
    }

    public LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }

    // Optional: Getter for WorldGuard instance if other classes need it directly
    // public WorldGuard getWorldGuardInstance() {
    // return worldGuardInstance;
    // }
}