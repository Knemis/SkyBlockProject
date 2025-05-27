package com.knemis.skyblock.skyblockcoreproject;

import com.knemis.skyblock.skyblockcoreproject.commands.IslandCommand;
import com.knemis.skyblock.skyblockcoreproject.economy.worth.IslandWorthManager;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // New addition
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
import com.knemis.skyblock.skyblockcoreproject.listeners.IslandWelcomeListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.MissionListener; // Added import
import com.knemis.skyblock.skyblockcoreproject.listeners.PlayerBoundaryListener; // Added import
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopSetupListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopVisitListener;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionManager;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionPlayerDataManager;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.commands.MissionCommand; // Added import

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

    // Island Management
    private IslandDataHandler islandDataHandler;
    private IslandLifecycleManager islandLifecycleManager;
    private IslandSettingsManager islandSettingsManager;
    private IslandMemberManager islandMemberManager;
    private IslandTeleportManager islandTeleportManager;

    // Island Features
    private IslandFlagManager islandFlagManager;
    private IslandBiomeManager islandBiomeManager;
    private IslandWelcomeManager islandWelcomeManager;

    // Economy and Shop Management
    private IslandWorthManager islandWorthManager;
    private ShopManager shopManager;
    private ShopSetupGUIManager shopSetupGUIManager;
    private ShopVisitGUIManager shopVisitGUIManager;
    private ShopAdminGUIManager shopAdminGUIManager; // New addition

    // GUI Managers
    private FlagGUIManager flagGUIManager;

    // Missions System
    private MissionManager missionManager;
    private MissionPlayerDataManager missionPlayerDataManager;
    private MissionGUIManager missionGUIManager; // Added field

    // Other
    private int nextIslandX;
    private WorldGuard worldGuardInstance;
    private LuckPerms luckPermsApi;
    private Economy vaultEconomy = null;

    // Player Status Tracking
    private final Map<UUID, Location> playerShopSetupState = new HashMap<>();
    private final Map<UUID, Location> playerViewingShopLocation = new HashMap<>();
    private final Map<UUID, Location> playerAdministeringShop = new HashMap<>(); // New addition: Which player is managing which shop
    private final Map<UUID, ShopAdminGUIManager.AdminInputType> playerWaitingForAdminInput = new HashMap<>(); // New addition: Expected admin input type from player
    private final Map<UUID, Location> playerChoosingShopMode = new HashMap<>();
    private final Map<UUID, ItemStack> playerInitialShopStockItem = new HashMap<>();

    @Override
    public void onEnable() {
        // 1. Config Loading and Defaults
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("general.next-island-x", 0);
        getConfig().addDefault("skyblock-world-name", "skyblock_world");
        getConfig().addDefault("island.worth.block_values.DIAMOND_BLOCK", 150.0);
        getConfig().addDefault("island.worth.level_requirements.1", 0.0);
        getConfig().addDefault("island.worth.level_up_rewards.2", "eco give {player} 250");
        getConfig().addDefault("island.enforce-boundaries", true); // Added config option
        saveConfig();

        this.nextIslandX = getConfig().getInt("general.next-island-x", 0);
        getLogger().info("SkyBlockProject Plugin Enabling...");

        // 2. Dependency Checks and Setups
        if (!setupLuckPerms()) {
            getLogger().warning("LuckPerms API not found! Island owner bypass permissions will NOT be automatically assigned.");
        }
        if (!setupEconomyVault()) {
            getLogger().severe("Could not set up economy system with Vault! Economy features will be disabled.");
        } else {
            getLogger().info("Economy system with Vault successfully set up!");
            EconomyManager.setupEconomy();
        }
        if (!hookPlugin("WorldEdit") || !setupWorldGuard()) {
            getLogger().severe("Required dependencies (WorldEdit/WorldGuard) not found or not active! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("WorldEdit and WorldGuard successfully found and active.");

        // 3. Main Data Handler
        this.islandDataHandler = new IslandDataHandler(this);
        this.islandDataHandler.loadSkyblockWorld();
        this.islandDataHandler.loadIslandsFromConfig();

        // 4. Island Feature Managers
        this.islandFlagManager = new IslandFlagManager(this, this.islandDataHandler);
        this.islandSettingsManager = new IslandSettingsManager(this, this.islandDataHandler);
        this.islandTeleportManager = new IslandTeleportManager(this, this.islandDataHandler);

        // 5. Island Lifecycle Manager
        this.islandLifecycleManager = new IslandLifecycleManager(this, this.islandDataHandler, this.islandFlagManager);

        // 6. Economy and Shop System Managers
        this.islandWorthManager = new IslandWorthManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.shopManager = new ShopManager(this);
        this.shopSetupGUIManager = new ShopSetupGUIManager(this, this.shopManager);
        this.shopVisitGUIManager = new ShopVisitGUIManager(this, this.shopManager);
        this.shopAdminGUIManager = new ShopAdminGUIManager(this, this.shopManager); // New addition: Initialization

        // 7. Other Island Managers
        this.islandMemberManager = new IslandMemberManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandBiomeManager = new IslandBiomeManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandWelcomeManager = new IslandWelcomeManager(this, this.islandDataHandler);

        // 8. GUI Managers
        this.flagGUIManager = new FlagGUIManager(this, this.islandDataHandler, this.islandFlagManager);

        // 8.5. Mission System
        this.missionManager = new MissionManager(this);
        this.missionPlayerDataManager = new MissionPlayerDataManager(this);
        this.missionGUIManager = new MissionGUIManager(this); // Instantiate MissionGUIManager

        // 9. Listener Registrations
        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, this.flagGUIManager), this);
        getServer().getPluginManager().registerEvents(new IslandWelcomeListener(this, this.islandDataHandler, this.islandWelcomeManager), this);
        // ShopListener constructor will be updated, so ShopAdminGUIManager is added
        getServer().getPluginManager().registerEvents(new ShopListener(this, this.shopManager, this.shopSetupGUIManager, this.islandDataHandler, this.shopVisitGUIManager, this.shopAdminGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopSetupListener(this, this.shopManager, this.shopSetupGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopVisitListener(this, this.shopManager, this.shopVisitGUIManager), this);
        getServer().getPluginManager().registerEvents(new MissionListener(this), this);
        if (getConfig().getBoolean("island.enforce-boundaries", true)) { // Register PlayerBoundaryListener
            getServer().getPluginManager().registerEvents(new PlayerBoundaryListener(this), this);
        }

        // 10. Command Registrations
        IslandCommand islandCommandExecutor = new IslandCommand(
                this, this.islandDataHandler, this.islandLifecycleManager, this.islandSettingsManager,
                this.islandMemberManager, this.islandTeleportManager, this.islandBiomeManager,
                this.islandWelcomeManager, this.flagGUIManager, this.islandWorthManager
        );
        if (getCommand("island") != null) {
            getCommand("island").setExecutor(islandCommandExecutor);
            getCommand("island").setTabCompleter(islandCommandExecutor);
        } else {
            getLogger().severe("'island' command not defined in plugin.yml!");
        }

        MissionCommand missionCommandExecutor = new MissionCommand(this);
        if (getCommand("missions") != null) {
            getCommand("missions").setExecutor(missionCommandExecutor);
            getCommand("missions").setTabCompleter(missionCommandExecutor);
        } else {
            getLogger().severe("'missions' command not defined in plugin.yml!");
        }


        getLogger().info("SkyBlockProject Plugin Successfully Enabled!");
    }

    @Override
    public void onDisable() {
        if (islandDataHandler != null) {
            islandDataHandler.saveAllIslandsToDisk();
        }
        if (missionPlayerDataManager != null) { // Added save all player mission data
            missionPlayerDataManager.saveAllPlayerData();
        }
        getLogger().info("SkyBlockProject Plugin Disabled.");
    }

    // --- Dependency Setup Methods ---
    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPermsApi = provider.getProvider();
            getLogger().info("Successfully connected to LuckPerms API.");
            return true;
        }
        getLogger().warning("LuckPerms API not found!");
        return false;
    }

    private boolean setupEconomyVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugin not found! Economy features will be unavailable.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No registered economy provider for Vault found! (Is EssentialsX Economy etc. installed?)");
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null || !wgPlugin.isEnabled()) {
            getLogger().severe("WorldGuard plugin not found or not active!");
            return false;
        }
        this.worldGuardInstance = WorldGuard.getInstance();
        if (this.worldGuardInstance == null) {
            getLogger().severe("Could not get WorldGuard instance!");
            return false;
        }
        getLogger().info("Successfully hooked into WorldGuard API.");
        return true;
    }

    private boolean hookPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    // --- Getter Methods ---
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
    public ShopAdminGUIManager getShopAdminGUIManager() { return shopAdminGUIManager; } // New addition
    public FlagGUIManager getFlagGUIManager() { return flagGUIManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public MissionPlayerDataManager getMissionPlayerDataManager() { return missionPlayerDataManager; }
    public MissionGUIManager getMissionGUIManager() { return missionGUIManager; } // Added getter
    public LuckPerms getLuckPermsApi() { return luckPermsApi; }

    public Map<UUID, Location> getPlayerShopSetupState() { return playerShopSetupState; }
    public Map<UUID, Location> getPlayerViewingShopLocation() { return playerViewingShopLocation; }
    public Map<UUID, Location> getPlayerAdministeringShop() { return playerAdministeringShop; } // New addition: Which player is managing which shop
    public Map<UUID, ShopAdminGUIManager.AdminInputType> getPlayerWaitingForAdminInput() { return playerWaitingForAdminInput; } // New addition: Expected admin input type from player
    public Map<UUID, Location> getPlayerChoosingShopMode() { return playerChoosingShopMode; }
    public Map<UUID, ItemStack> getPlayerInitialShopStockItem() { return playerInitialShopStockItem; }


    public int getNextIslandXAndIncrement() {
        int currentX = this.nextIslandX;
        this.nextIslandX += getConfig().getInt("island.spacing", 300);
        getConfig().set("general.next-island-x", this.nextIslandX);
        // saveConfig(); // It might be better to save onDisable or periodically instead of saving config on every X increment.
        return currentX;
    }

    public RegionManager getRegionManager(World bukkitWorld) {
        if (worldGuardInstance == null || bukkitWorld == null) {
            getLogger().severe("WorldGuard instance is null or given world is null. Cannot get RegionManager.");
            return null;
        }
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);
        RegionContainer container = worldGuardInstance.getPlatform().getRegionContainer();
        if (container == null) {
            getLogger().severe("Could not get WorldGuard RegionContainer!");
            return null;
        }
        return container.get(adaptedWorld);
    }
}