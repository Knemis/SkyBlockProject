
package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags; // Added for new custom flag
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.configuration.ConfigurationSection; // Added for dynamic flag loading

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class IslandFlagManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final boolean detailedFlagLogging;

    // Sadece genel varsayılan bayrakları tutar
    private final Map<StateFlag, StateFlag.State> defaultIslandFlags;

    // criticalOwnerAllowFlags listesi kaldırıldı.

    public IslandFlagManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.detailedFlagLogging = plugin.getConfig().getBoolean("logging.detailed-flag-changes", false);
        this.defaultIslandFlags = new LinkedHashMap<>();
        initializeDefaultFlags();
    }

    private void initializeDefaultFlags() {
        // Inside initializeDefaultFlags() method body
        this.defaultIslandFlags.clear(); // Start fresh

        org.bukkit.configuration.ConfigurationSection defaultFlagsConfig = plugin.getConfig().getConfigurationSection("island.default-flags");

        // Ensure WorldGuard API is available
        com.sk89q.worldguard.WorldGuard worldGuardInstance = null;
        try {
            worldGuardInstance = com.sk89q.worldguard.WorldGuard.getInstance();
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "[IslandFlagManager] WorldGuard API not found. This is a critical error. Default flags cannot be loaded.", e);
            // Initialize with an empty map or some very basic defaults if absolutely necessary,
            // otherwise, the plugin might not function correctly with flags.
            // For now, just return, as flag operations would fail.
            return;
        }


        if (defaultFlagsConfig != null) {
            plugin.getLogger().info("[IslandFlagManager] Loading default island flags from config.yml...");
            for (String flagKey : defaultFlagsConfig.getKeys(false)) {
                String stateValueStr = defaultFlagsConfig.getString(flagKey, "").toUpperCase();

                com.sk89q.worldguard.protection.flags.Flag<?> genericFlag = worldGuardInstance.getFlagRegistry().get(flagKey);
                com.sk89q.worldguard.protection.flags.StateFlag stateFlagInstance = null;

                if (genericFlag instanceof com.sk89q.worldguard.protection.flags.StateFlag) {
                    stateFlagInstance = (com.sk89q.worldguard.protection.flags.StateFlag) genericFlag;
                } else {
                    if (genericFlag != null) {
                        plugin.getLogger().warning("[IslandFlagManager] Configured default flag '" + flagKey + "' is not a StateFlag (it's a " + genericFlag.getClass().getSimpleName() + "). Only StateFlags can be set as default island flags. Skipping.");
                    } else {
                        plugin.getLogger().warning("[IslandFlagManager] Unknown flag name '" + flagKey + "' in config.yml under island.default-flags. Skipping.");
                    }
                    continue;
                }

                com.sk89q.worldguard.protection.flags.StateFlag.State wgState; // Changed to non-final to allow assignment in if/else
                if (stateValueStr.equals("ALLOW")) {
                    wgState = com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
                } else if (stateValueStr.equals("DENY")) {
                    wgState = com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
                } else if (stateValueStr.isEmpty() || stateValueStr.equals("NONE")) {
                    wgState = null;
                } else {
                    plugin.getLogger().warning("[IslandFlagManager] Invalid state value '" + defaultFlagsConfig.getString(flagKey) + "' for flag '" + flagKey + "' in config.yml. Must be ALLOW, DENY, or NONE/empty. Skipping.");
                    continue;
                }

                this.defaultIslandFlags.put(stateFlagInstance, wgState);
                plugin.getLogger().info("[IslandFlagManager] Loaded default flag from config: " + flagKey + " -> " + (wgState == null ? "UNSET/NONE" : stateValueStr));
            }
        } else {
            plugin.getLogger().warning("[IslandFlagManager] 'island.default-flags' section not found in config.yml. No default flags will be loaded dynamically from config. Only programmatic fallbacks might be applied.");
        }

        // Fallback for VISITOR_SHOP_USE
        // Ensure the CustomFlags class path is correct.
        // Assuming: import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags;
        if (com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags.VISITOR_SHOP_USE != null) {
            if (!this.defaultIslandFlags.containsKey(com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags.VISITOR_SHOP_USE)) {
                this.defaultIslandFlags.put(com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags.VISITOR_SHOP_USE, com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW);
                plugin.getLogger().info("[IslandFlagManager] Custom flag 'VISITOR_SHOP_USE' was not defined in config.yml, adding with default state ALLOW as a fallback.");
            }
        } else {
             plugin.getLogger().info("[IslandFlagManager] Custom flag 'VISITOR_SHOP_USE' (CustomFlags.VISITOR_SHOP_USE) is not registered or is null. Cannot add it as a default fallback.");
        }
        // End of initializeDefaultFlags method body
    }

    private String getRegionId(UUID ownerUUID) {
        return "skyblock_island_" + ownerUUID.toString();
    }

    private ProtectedRegion getProtectedRegionForIsland(UUID ownerUUID) {
        if (ownerUUID == null) return null;
        Island island = islandDataHandler.getIslandByOwner(ownerUUID);
        if (island == null || island.getWorld() == null) return null;
        RegionManager regionManager = plugin.getRegionManager(island.getWorld());
        if (regionManager == null) {
            plugin.getLogger().warning("Bölge Yöneticisi alınamadı: " + island.getWorld().getName());
            return null;
        }
        return regionManager.getRegion(getRegionId(ownerUUID));
    }

    public StateFlag.State getIslandFlagState(UUID ownerUUID, StateFlag flag) {
        if (ownerUUID == null || flag == null) {
            plugin.getLogger().warning("[FlagManager] getIslandFlagState: Gerekli parametrelerden biri null.");
            return null;
        }
        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region != null) {
            StateFlag.State state = region.getFlag(flag);
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-DEBUG] Bayrak durumu (Genel): Ada=" + ownerUUID + ", Bayrak=" + flag.getName() + ", Durum=" + (state == null ? "VARSAYILAN" : state.name()));
            }
            return state;
        }
        if (detailedFlagLogging) {
            plugin.getLogger().warning("[FlagManager-DEBUG] getIslandFlagState: Oyuncu " + ownerUUID + " için WorldGuard bölgesi bulunamadı.");
        }
        return null;
    }

    public boolean setIslandFlagState(Player changer, UUID ownerUUID, StateFlag flag, StateFlag.State newState) {
        if (changer == null || ownerUUID == null || flag == null ) {
            plugin.getLogger().severe("[FlagManager] setIslandFlagState: Gerekli parametrelerden biri null!");
            if(changer != null) changer.sendMessage(Component.text("Bayrak ayarlanırken iç bir hata oluştu.", NamedTextColor.RED));
            return false;
        }

        Island island = islandDataHandler.getIslandByOwner(ownerUUID);
        if (island == null || island.getWorld() == null) {
            changer.sendMessage(Component.text("Bayrak ayarlanacak ada veya dünya bulunamadı.", NamedTextColor.RED));
            return false;
        }
        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region == null) {
            changer.sendMessage(Component.text("Adanın koruma bölgesi bulunamadı.", NamedTextColor.RED));
            return false;
        }

        if (!changer.getUniqueId().equals(ownerUUID) && !changer.hasPermission("skyblock.admin.setanyflag")) {
            changer.sendMessage(Component.text("Bu adanın bayraklarını değiştirme izniniz yok.", NamedTextColor.RED));
            return false;
        }

        StateFlag.State oldState = region.getFlag(flag);
        try {
            region.setFlag(flag, newState); // Sadece genel bayrağı ayarla

            RegionManager regionManager = plugin.getRegionManager(island.getWorld());
            if (regionManager == null) {
                plugin.getLogger().severe("[FlagManager] setIslandFlagState: RegionManager alınamadı.");
                changer.sendMessage(Component.text("Bayrak ayarlanırken bir hata oluştu (RM).", NamedTextColor.RED));
                return false;
            }
            regionManager.saveChanges();

            String oldStateString = (oldState == null) ? "VARSAYILAN" : oldState.name();
            String newStateString = (newState == null) ? "VARSAYILAN" : newState.name();
            changer.sendMessage(Component.text("'" + flag.getName() + "' bayrağı (genel) ", NamedTextColor.GREEN)
                    .append(Component.text(newStateString, NamedTextColor.AQUA))
                    .append(Component.text(" olarak ayarlandı.", NamedTextColor.GREEN)));
            plugin.getLogger().info("[FlagManager] " + changer.getName() + " -> Ada: " + ownerUUID + ", Bayrak: '" + flag.getName() + "', Eski: " + oldStateString + ", Yeni: " + newStateString);
            return true;
        } catch (Exception e) { // StorageException veya diğer olası hatalar
            plugin.getLogger().log(Level.SEVERE, "[FlagManager] setIslandFlagState sırasında hata: " + e.getMessage(), e);
            changer.sendMessage(Component.text("Bayrak ayarlanırken beklenmedik bir hata oluştu.", NamedTextColor.RED));
            return false;
        }
    }

    /**
     * Yeni oluşturulan bir bölgeye varsayılan genel bayrakları uygular.
     * @param region Bayrakların uygulanacağı ProtectedRegion nesnesi.
     */
    public void applyDefaultFlagsToRegion(ProtectedRegion region) {
        if (region == null) {
            plugin.getLogger().warning("[FlagManager] applyDefaultFlagsToRegion: Bölge null geldi, varsayılan bayraklar uygulanamadı.");
            return;
        }
        plugin.getLogger().info("[FlagManager] '" + region.getId() + "' bölgesi için varsayılan genel ada bayrakları uygulanıyor...");

        for (Map.Entry<StateFlag, StateFlag.State> entry : defaultIslandFlags.entrySet()) {
            StateFlag flag = entry.getKey();
            StateFlag.State defaultState = entry.getValue();
            region.setFlag(flag, defaultState); // Genel bayrağı ayarla
            if (detailedFlagLogging) {
                plugin.getLogger().info("  -> Genel Varsayılan: " + flag.getName() + " -> " + (defaultState == null ? "KALDIRILDI (VARSAYILAN)" : defaultState.name()) + ", Bölge=" + region.getId());
            }
        }
        // Ada sahibinin muafiyeti ve üyelerin özel izinleri artık tamamen
        // WorldGuard'ın kendi iç bypass mekanizmaları ve LuckPerms üzerinden yönetilecektir.
        // Bu metod artık OWNERS veya MEMBERS grubuna özel bir atama yapmaz.
    }

    /**
     * GUI'de yönetilebilecek bayrakların listesini döndürür.
     */
    public List<StateFlag> getManagableFlags() {
        return Collections.unmodifiableList(new ArrayList<>(defaultIslandFlags.keySet()));
    }

    /**
     * Bir bayrağın bu eklenti tarafından tanımlanan genel varsayılan durumunu döndürür.
     * @param flag Durumu sorgulanacak bayrak.
     * @return Bayrağın varsayılan durumu veya map'te yoksa null.
     */
    public StateFlag.State getDefaultStateForFlag(StateFlag flag) {
        return defaultIslandFlags.get(flag);
    }
}
