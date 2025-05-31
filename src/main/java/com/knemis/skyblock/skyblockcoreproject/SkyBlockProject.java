package com.knemis.skyblock.skyblockcoreproject;

// Mevcut importlarınız (kısaltıldı, tam listeyi dosyanızdan alınız)
import com.knemis.skyblock.skyblockcoreproject.commands.AdminShopCommand;
import com.knemis.skyblock.skyblockcoreproject.commands.IslandCommand;
import com.knemis.skyblock.skyblockcoreproject.commands.MissionCommand;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import com.knemis.skyblock.skyblockcoreproject.economy.worth.IslandWorthManager;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.PlayerShopAdminGUIManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.PlayerShopAdminAnvilListener;
import com.knemis.skyblock.skyblockcoreproject.shop.admin.AdminShopGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.admin.AdminShopListener;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandSettingsManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopAnvilListener;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import com.knemis.skyblock.skyblockcoreproject.listeners.FlagGUIListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.IslandWelcomeListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.MissionListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.MissionObjectiveListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.PlayerBoundaryListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopSetupListener;
import com.knemis.skyblock.skyblockcoreproject.listeners.ShopVisitListener;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionManager;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionPlayerDataManager;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags;

// Rank Manager Sistemi için importlar (paket adlarını kendi yapınıza göre düzeltin)
import com.knemis.skyblock.skyblockcoreproject.rankmanager.config.RankConfigManager;
import com.knemis.skyblock.skyblockcoreproject.rankmanager.gui.OwnerGuiManager;
import com.knemis.skyblock.skyblockcoreproject.rankmanager.gui.PlayerInteractionListener; // Rank Manager için olan listener
import com.knemis.skyblock.skyblockcoreproject.rankmanager.luckperms.LuckPermsHelper; // Corrected import
import com.knemis.skyblock.skyblockcoreproject.rankmanager.util.RepairLogger;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class SkyBlockProject extends JavaPlugin {

    // --- Mevcut Alanlarınız ---
    private IslandDataHandler islandDataHandler;
    private IslandLifecycleManager islandLifecycleManager;
    private IslandSettingsManager islandSettingsManager;
    private IslandMemberManager islandMemberManager;
    private IslandTeleportManager islandTeleportManager;
    private IslandFlagManager islandFlagManager;
    private IslandBiomeManager islandBiomeManager;
    private IslandWelcomeManager islandWelcomeManager;
    private IslandWorthManager islandWorthManager;
    private ShopManager shopManager;
    private ShopSetupGUIManager shopSetupGUIManager;
    private ShopVisitGUIManager shopVisitGUIManager;
    private PlayerShopAdminGUIManager playerShopAdminGUIManager;
    private AdminShopGUIManager adminShopGUIManager;
    private FlagGUIManager flagGUIManager;
    private MissionManager missionManager;
    private MissionPlayerDataManager missionPlayerDataManager;
    private MissionGUIManager missionGUIManager;
    private int nextIslandX;
    private WorldGuard worldGuardInstance;
    private LuckPerms luckPermsApi;
    private Economy vaultEconomy = null;
    private final Map<UUID, Location> playerViewingShopLocation = new HashMap<>();
    private final Map<UUID, Location> playerEnteringBuyQuantity = new HashMap<>();
    private final Map<UUID, Location> playerEnteringSellQuantity = new HashMap<>();
    // --- Mevcut Alanlarınızın Sonu ---

    // --- Rank Manager Sistemi Alanları ---
    private RankConfigManager rankConfigManager;
    private LuckPermsHelper luckPermsHelper;
    private RepairLogger repairLogger;
    private OwnerGuiManager ownerGuiManager;
    private PlayerInteractionListener rankManagerGuiListener;

    private BukkitTask autoReloadTask = null;
    private final Set<UUID> ownersPendingConfirmation = new HashSet<>();
    private boolean reloadPending = false;
    public static final String PLUGIN_PREFIX = ChatColor.GOLD + "[RankManager] " + ChatColor.RESET;
    // --- Rank Manager Sistemi Alanlarının Sonu ---


    @Override
    public void onEnable() {
        getLogger().info("SkyBlockProject Plugin Enabling...");
        // 1. Config Yükleme ve Varsayılanlar
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.nextIslandX = getConfig().getInt("general.next-island-x", 0);

        // 2. Bağımlılık Kontrolleri ve Kurulumları
        if (!setupLuckPerms()) {
            getLogger().severe("LuckPerms API bulunamadı! SkyBlockProject devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomyVault()) {
            getLogger().severe("Vault ile ekonomi sistemi kurulamadı! Ekonomi özellikleri devre dışı bırakılacak.");
        } else {
            getLogger().info("Vault ile ekonomi sistemi başarıyla kuruldu!");
            EconomyManager.setupEconomy(this);
        }
        if (!hookPlugin("FastAsyncWorldEdit") || !setupWorldGuard()) {
            getLogger().severe("Gerekli bağımlılıklar (FastAsyncWorldEdit/WorldGuard) bulunamadı veya aktif değil! Plugin devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("FastAsyncWorldEdit ve WorldGuard başarıyla bulundu ve aktif.");

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            CustomFlags.VISITOR_SHOP_USE = new StateFlag("visitor-shop-use", true);
            registry.register(CustomFlags.VISITOR_SHOP_USE);
            getLogger().info("Özel 'visitor-shop-use' flag'i başarıyla kaydedildi.");
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("visitor-shop-use");
            if (existing instanceof StateFlag) {
                CustomFlags.VISITOR_SHOP_USE = (StateFlag) existing;
                getLogger().info("Özel 'visitor-shop-use' flag'i zaten kayıtlıydı. Mevcut örnek kullanılıyor.");
            } else {
                getLogger().severe("'visitor-shop-use' flag'i kaydedilemedi veya alınamadı: " + e.getMessage());
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Özel flag kayıt edilirken hata oluştu", e);
        }

        this.islandDataHandler = new IslandDataHandler(this);
        this.islandDataHandler.loadSkyblockWorld();
        this.islandDataHandler.loadIslandsFromConfig();

        this.islandFlagManager = new IslandFlagManager(this, this.islandDataHandler);
        this.islandSettingsManager = new IslandSettingsManager(this, this.islandDataHandler);
        this.islandTeleportManager = new IslandTeleportManager(this, this.islandDataHandler);

        this.islandLifecycleManager = new IslandLifecycleManager(this, this.islandDataHandler, this.islandFlagManager);

        this.islandWorthManager = new IslandWorthManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.shopManager = new ShopManager(this);
        this.shopSetupGUIManager = new ShopSetupGUIManager(this, this.shopManager);
        this.shopVisitGUIManager = new ShopVisitGUIManager(this, this.shopManager);
        this.playerShopAdminGUIManager = new PlayerShopAdminGUIManager(this, this.shopManager);
        this.adminShopGUIManager = new AdminShopGUIManager(this);

        this.islandMemberManager = new IslandMemberManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandBiomeManager = new IslandBiomeManager(this, this.islandDataHandler, this.islandLifecycleManager);
        this.islandWelcomeManager = new IslandWelcomeManager(this, this.islandDataHandler);

        this.flagGUIManager = new FlagGUIManager(this, this.islandDataHandler, this.islandFlagManager);

        this.missionManager = new MissionManager(this);
        this.missionPlayerDataManager = new MissionPlayerDataManager(this);
        this.missionGUIManager = new MissionGUIManager(this);


        // --- Rank Manager Sistemi Başlatma ---
        this.repairLogger = new RepairLogger(this);
        this.rankConfigManager = new RankConfigManager(this);

        if (this.luckPermsApi != null) {
            this.luckPermsHelper = new LuckPermsHelper(this, this.luckPermsApi, this.rankConfigManager, this.repairLogger);
        } else {
            getLogger().severe("LuckPermsHelper başlatılamadı çünkü LuckPerms API null!");
        }
        this.ownerGuiManager = new OwnerGuiManager(this);
        this.rankManagerGuiListener = new PlayerInteractionListener(this, this.ownerGuiManager);
        getServer().getPluginManager().registerEvents(this.rankManagerGuiListener, this);
        getLogger().info("Rank Manager GUI Listener kaydedildi.");

        if (!rankConfigManager.loadRankTemplates()) {
            getLogger().severe("Rank Manager için rütbe şablonları yüklenemedi. Fonksiyonellik sınırlı olacak.");
        } else {
            getLogger().info("Rank Manager için rütbe şablonları başarıyla yüklendi.");
        }

        if (this.luckPermsHelper != null) {
            getServer().getScheduler().runTaskLater(this, () -> {
                getLogger().info("Rank Manager: İlk rütbe kurulumu ve doğrulaması yapılıyor...");
                luckPermsHelper.initializeAndValidateRanks(repairsWereMade -> {
                    if (repairsWereMade) {
                        getLogger().warning("Rank Manager: İlk rütbe doğrulaması sırasında tutarsızlıklar bulundu ve düzeltildi.");
                        handleRepairsMade();
                    } else {
                        getLogger().info("Rank Manager: İlk rütbe doğrulaması tamamlandı. Tutarsızlık bulunamadı.");
                    }
                });
            }, 20L);
        } else {
            getLogger().warning("Rank Manager doğrulama atlandı çünkü LuckPermsHelper başlatılamadı.");
        }
        getLogger().info("Rank Manager sistemi başlatıldı.");
        // --- Rank Manager Sistemi Başlatma Sonu ---

        getServer().getPluginManager().registerEvents(new FlagGUIListener(this, this.flagGUIManager), this);
        getServer().getPluginManager().registerEvents(new IslandWelcomeListener(this, this.islandDataHandler, this.islandWelcomeManager), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this, this.shopManager, this.shopSetupGUIManager, this.islandDataHandler, this.shopVisitGUIManager, this.playerShopAdminGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopSetupListener(this, this.shopManager, this.shopSetupGUIManager), this);
        getServer().getPluginManager().registerEvents(new ShopVisitListener(this, this.shopManager, this.shopVisitGUIManager), this);
        getServer().getPluginManager().registerEvents(new MissionListener(this), this);
        getServer().getPluginManager().registerEvents(new MissionObjectiveListener(this, this.missionManager), this);
        getServer().getPluginManager().registerEvents(new ShopAnvilListener(this, this.shopSetupGUIManager), this);
        getServer().getPluginManager().registerEvents(new PlayerShopAdminAnvilListener(this, this.playerShopAdminGUIManager), this);
        getServer().getPluginManager().registerEvents(new AdminShopListener(this, this.adminShopGUIManager), this);
        if (getConfig().getBoolean("island.enforce-boundaries", true)) {
            getServer().getPluginManager().registerEvents(new PlayerBoundaryListener(this), this);
        }


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

        MissionCommand missionCommandExecutor = new MissionCommand(this);
        if (getCommand("missions") != null) {
            getCommand("missions").setExecutor(missionCommandExecutor);
            getCommand("missions").setTabCompleter(missionCommandExecutor);
        } else {
            getLogger().severe("'missions' komutu plugin.yml dosyasında tanımlanmamış!");
        }

        if (getCommand("adminshop") != null) {
            getCommand("adminshop").setExecutor(new AdminShopCommand(this, this.adminShopGUIManager));
        } else {
            getLogger().severe("'adminshop' komutu plugin.yml dosyasında tanımlanmamış!");
        }

        getLogger().info("SkyBlockProject Plugin Successfully Enabled!");
    }

    @Override
    public void onDisable() {
        if (islandDataHandler != null) {
            islandDataHandler.saveAllIslandsToDisk();
        }
        if (missionPlayerDataManager != null) {
            missionPlayerDataManager.saveAllPlayerData();
        }
        if (shopManager != null && shopManager.getShopStorage() != null) {
            Map<Location, Shop> allActiveShops = shopManager.getActiveShopsMap();
            if (allActiveShops != null) {
                shopManager.getShopStorage().saveAllShops(allActiveShops);
                getLogger().info(allActiveShops.size() + " dükkanın kaydedilmesi istendi.");
            } else {
                getLogger().warning("Aktif dükkan haritası null idi, dükkanlar kaydedilemedi.");
            }
        }
        getConfig().set("general.next-island-x", this.nextIslandX);
        saveConfig();

        if (autoReloadTask != null && !autoReloadTask.isCancelled()) {
            autoReloadTask.cancel();
            autoReloadTask = null;
        }
        if (ownerGuiManager != null) ownerGuiManager.closeAllGuis();
        ownersPendingConfirmation.clear();
        getLogger().info("Rank Manager kapatılıyor...");

        getLogger().info("SkyBlockProject Plugin Disabled.");
    }

    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPermsApi = provider.getProvider();
            getLogger().info("LuckPerms API'sine başarıyla bağlanıldı.");
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
            getLogger().severe("Vault için kayıtlı ekonomi sağlayıcısı bulunamadı! (EssentialsX Economy vb. yüklü mü?)");
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null || !wgPlugin.isEnabled()) {
            getLogger().severe("WorldGuard plugini bulunamadı veya aktif değil!");
            return false;
        }
        this.worldGuardInstance = WorldGuard.getInstance();
        if (this.worldGuardInstance == null) {
            getLogger().severe("WorldGuard örneği alınamadı!");
            return false;
        }
        getLogger().info("WorldGuard API'sine başarıyla bağlanıldı.");
        return true;
    }

    private boolean hookPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    public Economy getEconomy() { return vaultEconomy; }
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
    public PlayerShopAdminGUIManager getPlayerShopAdminGUIManager() { return playerShopAdminGUIManager; }
    public AdminShopGUIManager getAdminShopGUIManager() { return adminShopGUIManager; }
    public FlagGUIManager getFlagGUIManager() { return flagGUIManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public MissionPlayerDataManager getMissionPlayerDataManager() { return missionPlayerDataManager; }
    public MissionGUIManager getMissionGUIManager() { return missionGUIManager; }
    public LuckPerms getLuckPermsApi() { return luckPermsApi; }
    public Map<UUID, Location> getPlayerViewingShopLocation() { return playerViewingShopLocation; }
    public Map<UUID, Location> getPlayerEnteringBuyQuantity() { return playerEnteringBuyQuantity; }
    public Map<UUID, Location> getPlayerEnteringSellQuantity() { return playerEnteringSellQuantity; }
    public WorldGuard getWorldGuardInstance() { return worldGuardInstance; }
    public int getNextIslandXAndIncrement() {
        int currentX = this.nextIslandX;
        this.nextIslandX += getConfig().getInt("island.spacing", 300);
        return currentX;
    }
    public RegionManager getRegionManager(World bukkitWorld) {
        if (worldGuardInstance == null || bukkitWorld == null) {
            getLogger().severe("WorldGuard örneği null veya verilen dünya null. RegionManager alınamıyor.");
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

    // --- Rank Manager Metodları ---
    public void handleRepairsMade() {
        this.reloadPending = true;
        this.ownersPendingConfirmation.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("skyblockrankmanager.owner.notify")) {
                if (this.ownerGuiManager != null) this.ownerGuiManager.openReloadGui(player);
                this.ownersPendingConfirmation.add(player.getUniqueId());
                if (this.rankManagerGuiListener != null) {
                    this.rankManagerGuiListener.addPlayerToLockdown(player.getUniqueId());
                } else {
                    getLogger().warning("RankManagerGuiListener null, oyuncu kilitleme yapılamadı: " + player.getName());
                }
            }
        }

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW +
                "LuckPerms grupları düzeltildi. LuckPerms'in yeniden yüklenmesi gerekiyor!");
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW +
                "Owner'lar bilgilendirildi. Konsolda '/srm confirmreload' veya '/srm cancelreload' yazın.");
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW +
                "Eylem yapılmazsa LuckPerms 30 saniye içinde otomatik olarak yeniden yüklenecek...");

        if (this.autoReloadTask != null && !this.autoReloadTask.isCancelled()) {
            this.autoReloadTask.cancel();
        }
        this.autoReloadTask = getServer().getScheduler().runTaskLater(this, () -> {
            if (this.reloadPending) {
                Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.GOLD + "LuckPerms şimdi otomatik olarak yeniden yükleniyor...");
                performLuckPermsReload("Otomatik yeniden yükleme (30s zaman aşımı)");
            }
        }, 30 * 20L);
    }

    public void confirmReload(String source) {
        if (!this.reloadPending) {
            String msg = PLUGIN_PREFIX + ChatColor.GREEN + "Bekleyen bir yeniden yükleme yoktu.";
            Bukkit.getConsoleSender().sendMessage(msg);
            if (source.startsWith("GUI")) {
                try {
                    String playerName = source.substring(source.indexOf('(') + 1, source.indexOf(')')); // Get player name
                    Player player = Bukkit.getPlayerExact(playerName); // Use getPlayerExact
                    if (player != null) player.sendMessage(msg);
                } catch (Exception ignored) {}
            }
            return;
        }
        performLuckPermsReload(source);
    }

    public void cancelReload(String source) {
        if (!this.reloadPending) {
            String msg = PLUGIN_PREFIX + ChatColor.GREEN + "Bekleyen bir yeniden yükleme yoktu.";
            Bukkit.getConsoleSender().sendMessage(msg);
            if (source.startsWith("GUI")) {
                 try {
                    String playerName = source.substring(source.indexOf('(') + 1, source.indexOf(')'));
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) player.sendMessage(msg);
                } catch (Exception ignored) {}
            }
            return;
        }
        this.reloadPending = false;
        if (this.autoReloadTask != null && !this.autoReloadTask.isCancelled()) {
            this.autoReloadTask.cancel();
            this.autoReloadTask = null;
        }
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.RED + "LuckPerms yeniden yüklemesi " + source + " tarafından iptal edildi.");
        if (this.repairLogger != null) this.repairLogger.logInfo("LuckPerms yeniden yüklemesi " + source + " tarafından iptal edildi.");
        releaseAllOwnersFromLockdown();
        if (this.ownerGuiManager != null) this.ownerGuiManager.closeAllGuis();
    }

    private void performLuckPermsReload(String source) {
        if (!this.reloadPending && !source.toLowerCase().contains("force") && !source.toLowerCase().contains("otomatik")) {
            getLogger().info("performLuckPermsReload çağrıldı ancak bekleyen bir yeniden yükleme yoktu ve zorlanmadı/otomatik değildi.");
            return;
        }

        this.reloadPending = false;
        if (this.autoReloadTask != null && !this.autoReloadTask.isCancelled()) {
            this.autoReloadTask.cancel();
            this.autoReloadTask = null;
        }

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "LuckPerms yeniden yükleniyor (tetikleyen: " + source + ")...");
        if (this.repairLogger != null) this.repairLogger.logInfo("LuckPerms yeniden yüklemesi başlatıldı: " + source);

        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp reload");

        if (success) {
            Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "LuckPerms yeniden yükleme komutu çalıştırıldı.");
            getServer().getScheduler().runTaskLater(this, () -> {
                if (this.luckPermsHelper != null) {
                    getLogger().info("Rank Manager: Yeniden yüklemeden sonra rütbeler yeniden doğrulanıyor...");
                    this.luckPermsHelper.initializeAndValidateRanks(repairsMade -> {
                        if (repairsMade) {
                            getLogger().severe(PLUGIN_PREFIX + ChatColor.RED + "KRİTİK: LuckPerms yeniden yüklemesi ve yeniden doğrulamadan SONRA bile tutarsızlıklar bulundu!");
                            if (this.repairLogger != null) this.repairLogger.logSevere("KRİTİK: LP yeniden yüklemesi ve yeniden doğrulamadan sonra tutarsızlıklar bulundu.");
                        } else {
                            getLogger().info(PLUGIN_PREFIX + ChatColor.GREEN + "Yeniden doğrulama başarılı. Tüm rütbeler uyumlu.");
                            if (this.repairLogger != null) this.repairLogger.logInfo("Yeniden yükleme sonrası yeniden doğrulama başarılı.");
                        }
                    });
                }
            }, 40L);
        } else {
            Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.RED + "LuckPerms yeniden yükleme komutu gönderilemedi veya düzgün çalıştırılamadı.");
            if (this.repairLogger != null) this.repairLogger.logSevere("LuckPerms yeniden yükleme komutu başarısız oldu.");
            getLogger().warning(PLUGIN_PREFIX + ChatColor.DARK_RED + "LuckPerms yeniden yüklemesi başarısız oldu. Sorunlar devam ederse sunucuyu yeniden başlatmayı düşünün!");
        }
        releaseAllOwnersFromLockdown();
        if (this.ownerGuiManager != null) this.ownerGuiManager.closeAllGuis();
    }

    public void releaseOwnerFromLockdown(UUID playerUuid) {
        this.ownersPendingConfirmation.remove(playerUuid);
        if (this.rankManagerGuiListener != null) {
            this.rankManagerGuiListener.removePlayerFromLockdown(playerUuid);
        }
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            if (player.getOpenInventory().getTitle().equals(OwnerGuiManager.GUI_TITLE)) {
                player.closeInventory();
            }
            player.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "Artık normal şekilde etkileşimde bulunabilirsiniz.");
        }
    }

    private void releaseAllOwnersFromLockdown() {
        new HashSet<>(this.ownersPendingConfirmation).forEach(this::releaseOwnerFromLockdown);
        this.ownersPendingConfirmation.clear(); // Clear after iterating over copy
    }

    public Set<UUID> getOwnersPendingConfirmation() {
        return this.ownersPendingConfirmation;
    }

    public RepairLogger getRepairLogger() {
        return this.repairLogger;
    }
    // --- Rank Manager Metodları Sonu ---

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rankmanageradmin") || command.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(label))) {
            if (!sender.hasPermission("skyblockrankmanager.admin")) {
                sender.sendMessage(ChatColor.RED + "Bu komutu kullanma izniniz yok.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Kullanım: /" + label + " <eylem>");
                sender.sendMessage(PLUGIN_PREFIX + ChatColor.AQUA + "Eylemler: validate, forcereload, confirmreload, cancelreload");
                return true;
            }

            String action = args[0].toLowerCase();
            switch (action) {
                case "validate":
                    sender.sendMessage(PLUGIN_PREFIX + ChatColor.AQUA + "Manuel rütbe doğrulaması yapılıyor...");
                    if (this.luckPermsHelper != null) {
                        this.luckPermsHelper.initializeAndValidateRanks(repairsMade -> {
                            if (repairsMade) {
                                sender.sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Doğrulama tamamlandı. Düzeltmeler yapıldı. Yeniden yükleme başlatıldı.");
                                handleRepairsMade();
                            } else {
                                sender.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "Doğrulama tamamlandı. Tutarsızlık bulunamadı.");
                            }
                        });
                    } else {
                        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + "Rütbe doğrulama sistemi başlatılmamış.");
                    }
                    break;
                case "forcereload":
                    sender.sendMessage(PLUGIN_PREFIX + ChatColor.AQUA + "LuckPerms yeniden yüklemesi zorlanıyor...");
                    performLuckPermsReload("Zorla (" + sender.getName() + ")");
                    break;
                case "confirmreload":
                    if (this.reloadPending) {
                        sender.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + "Konsol LuckPerms yeniden yüklemesini onayladı.");
                        confirmReload("Konsol (" + sender.getName() + ")");
                    } else {
                        sender.sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Şu anda onay bekleyen bir yeniden yükleme yok.");
                    }
                    break;
                case "cancelreload":
                    if (this.reloadPending) {
                        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + "Konsol LuckPerms yeniden yüklemesini iptal etti.");
                        cancelReload("Konsol (" + sender.getName() + ")");
                    } else {
                        sender.sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Şu anda onay bekleyen bir yeniden yükleme yok.");
                    }
                    break;
                default:
                    sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + "Bilinmeyen eylem. Yardım için /" + label + " kullanın.");
                    break;
            }
            return true;
        }
        return false;
    }
}
