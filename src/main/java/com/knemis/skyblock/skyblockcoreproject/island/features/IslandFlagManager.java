package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.ChatColor;
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
        // Bu bayraklar bölgenin geneli için (varsayılan olarak ziyaretçileri etkileyen) ayarlardır.
        // Ada sahibi ve üyelerin durumu WG'nin kendi bypass mekanizmaları ve LuckPerms ile yönetilir.
        defaultIslandFlags.put(Flags.BUILD, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.INTERACT, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.CHEST_ACCESS, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.USE, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ITEM_DROP, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ITEM_PICKUP, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.PVP, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.TNT, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ENDERPEARL, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.RIDE, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.LAVA_FLOW, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.WATER_FLOW, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        // İhtiyaç duyulan diğer bayraklar buraya eklenebilir.
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
            if(changer != null) changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken iç bir hata oluştu.");
            return false;
        }

        Island island = islandDataHandler.getIslandByOwner(ownerUUID);
        if (island == null || island.getWorld() == null) {
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanacak ada veya dünya bulunamadı.");
            return false;
        }
        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region == null) {
            changer.sendMessage(ChatColor.RED + "Adanın koruma bölgesi bulunamadı.");
            return false;
        }

        if (!changer.getUniqueId().equals(ownerUUID) && !changer.hasPermission("skyblock.admin.setanyflag")) {
            changer.sendMessage(ChatColor.RED + "Bu adanın bayraklarını değiştirme izniniz yok.");
            return false;
        }

        StateFlag.State oldState = region.getFlag(flag);
        try {
            region.setFlag(flag, newState); // Sadece genel bayrağı ayarla

            RegionManager regionManager = plugin.getRegionManager(island.getWorld());
            if (regionManager == null) {
                plugin.getLogger().severe("[FlagManager] setIslandFlagState: RegionManager alınamadı.");
                changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken bir hata oluştu (RM).");
                return false;
            }
            regionManager.saveChanges();

            String oldStateString = (oldState == null) ? "VARSAYILAN" : oldState.name();
            String newStateString = (newState == null) ? "VARSAYILAN" : newState.name();
            changer.sendMessage(ChatColor.GREEN + "'" + flag.getName() + "' bayrağı (genel) " + ChatColor.AQUA + newStateString + ChatColor.GREEN + " olarak ayarlandı.");
            plugin.getLogger().info("[FlagManager] " + changer.getName() + " -> Ada: " + ownerUUID + ", Bayrak: '" + flag.getName() + "', Eski: " + oldStateString + ", Yeni: " + newStateString);
            return true;
        } catch (Exception e) { // StorageException veya diğer olası hatalar
            plugin.getLogger().log(Level.SEVERE, "[FlagManager] setIslandFlagState sırasında hata: " + e.getMessage(), e);
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken beklenmedik bir hata oluştu.");
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