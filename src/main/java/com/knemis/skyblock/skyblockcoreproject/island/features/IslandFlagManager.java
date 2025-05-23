package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


import com.sk89q.worldguard.domains.DefaultDomain; // OWNERS grubu için EKLENDİ

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList; // EKLENDİ: ArrayList importu

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level; // Detaylı loglama için

public class IslandFlagManager {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    private final Map<StateFlag, StateFlag.State> defaultIslandFlags;
    private final boolean detailedFlagLogging;

    private final List<StateFlag> ownerBypassFlags = Arrays.asList(
            Flags.BUILD,
            Flags.INTERACT,
            Flags.CHEST_ACCESS,
            Flags.USE,
            Flags.ITEM_DROP, // Sahip kendi adasına eşya atabilmeli
            Flags.ITEM_PICKUP // Sahip kendi adasından eşya toplayabilmeli
    );

    public IslandFlagManager(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.defaultIslandFlags = new LinkedHashMap<>();
        this.detailedFlagLogging = plugin.getConfig().getBoolean("logging.detailed-flag-changes", false);

        // Varsayılan bayraklar (genellikle ziyaretçiler için)
        defaultIslandFlags.put(Flags.BUILD, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.INTERACT, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.CHEST_ACCESS, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.USE, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ITEM_DROP, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ITEM_PICKUP, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.TRAMPLE_BLOCKS, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.RIDE, StateFlag.State.DENY);

        defaultIslandFlags.put(Flags.PVP, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.TNT, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.ENDERPEARL, StateFlag.State.ALLOW); // Genellikle izin verilir
        defaultIslandFlags.put(Flags.POTION_SPLASH, StateFlag.State.ALLOW);

        defaultIslandFlags.put(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.LAVA_FLOW, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.WATER_FLOW, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.LEAF_DECAY, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.LIGHTNING, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.SNOW_FALL, StateFlag.State.ALLOW);
    }

    private ProtectedRegion getProtectedRegionForIsland(UUID ownerUUID) {
        if (ownerUUID == null) {
            plugin.getLogger().warning("[FlagManager] getProtectedRegionForIsland çağrısında ownerUUID null geldi.");
            return null;
        }
        return islandManager.getProtectedRegion(ownerUUID);
    }

    public StateFlag.State getIslandFlagState(UUID ownerUUID, StateFlag flag) {
        // ... (Bu metod aynı kalabilir, önceki haliyle doğru) ...
        if (ownerUUID == null || flag == null) {
            plugin.getLogger().warning("[FlagManager] getIslandFlagState çağrısında ownerUUID veya flag null geldi.");
            return null;
        }
        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region != null) {
            StateFlag.State state = region.getFlag(flag);
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-DEBUG] Bayrak durumu sorgulandı: AdaSahibi=" + ownerUUID + ", Bayrak=" + flag.getName() + ", Durum=" + (state == null ? "VARSAYILAN" : state.name()));
            }
            return state;
        }
        if (detailedFlagLogging) {
            plugin.getLogger().warning("[FlagManager-DEBUG] getIslandFlagState: Oyuncu " + ownerUUID + " için WorldGuard bölgesi bulunamadı.");
        }
        return null;
    }

    public boolean setIslandFlagState(Player changer, UUID ownerUUID, StateFlag flag, StateFlag.State newState) {
        if (changer == null || ownerUUID == null || flag == null) {
            plugin.getLogger().severe("[FlagManager] setIslandFlagState çağrısında kritik bir parametre null geldi!");
            if(changer != null) changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken dahili bir hata oluştu (parametre eksik).");
            return false;
        }

        Island island = islandManager.getIslandByOwner(ownerUUID);
        if (island == null || island.getWorld() == null) {
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanacak ada veya dünya bulunamadı.");
            plugin.getLogger().warning("[FlagManager] setIslandFlagState: Oyuncu " + ownerUUID + " için ada veya ada dünyası null.");
            return false;
        }

        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region == null) {
            changer.sendMessage(ChatColor.RED + "Adanın koruma bölgesi bulunamadı.");
            plugin.getLogger().warning("[FlagManager] setIslandFlagState: Oyuncu " + ownerUUID + " için WorldGuard bölgesi bulunamadı.");
            return false;
        }

        if (!changer.getUniqueId().equals(ownerUUID) && !changer.hasPermission("skyblock.admin.setanyflag")) {
            changer.sendMessage(ChatColor.RED + "Bu adanın bayraklarını değiştirme izniniz yok.");
            return false;
        }

        String specificFlagPermission = "skyblock.flags.manage." + flag.getName().toLowerCase().replace("_", "-");
        if (!changer.hasPermission(specificFlagPermission) && !changer.getUniqueId().equals(ownerUUID)) {
            if(!changer.hasPermission("skyblock.admin.setanyflag")){
                changer.sendMessage(ChatColor.RED + "'" + flag.getName() + "' bayrağını değiştirmek için özel izniniz ("+ specificFlagPermission +") bulunmuyor.");
                if (detailedFlagLogging) {
                    plugin.getLogger().info("[FlagManager] Oyuncu " + changer.getName() + ", " + ownerUUID + " adasındaki '" + flag.getName() + "' bayrağını değiştirmeye çalıştı ancak '" + specificFlagPermission + "' izni yok.");
                }
                return false;
            }
        }

        StateFlag.State oldState = region.getFlag(flag);

        try {
            // Genel bayrak durumunu ayarla (bu genellikle ziyaretçileri etkiler)
            region.setFlag(flag, newState);
            String logMessageAction = "ayarladı";

            // Eğer bu bayrak, sahiplerin normalde muaf olması gereken bir bayraksa
            // ve genel durum DENY olarak ayarlanıyorsa, sahipler için ayrıca ALLOW yapmayı dene.
            // Bu, WorldGuard'ın normal sahip muafiyeti çalışmıyorsa bir güvence olabilir.
            if (ownerBypassFlags.contains(flag)) {
                DefaultDomain owners = region.getOwners();
                if (!owners.contains(ownerUUID)) { // Double check if player is actually in WG owner list
                    owners.addPlayer(ownerUUID); // Add if somehow missing
                    plugin.getLogger().warning("[FlagManager] Oyuncu " + ownerUUID + " WorldGuard bölge sahipleri listesinde değildi, eklendi. Bölge: " + region.getId());
                }
                // region.setFlag(flag, DefaultDomain.OWNERS, StateFlag.State.ALLOW); // WorldGuard 7'de bu şekilde grup ayarı olmayabilir, yerine bypass permi kullanılır genelde.
                // Sahip olmanın kendisi yeterli olmalı.
                // Şimdilik, WorldGuard'ın sahip muafiyetine güveniyoruz. Eğer hala sorun varsa, bu noktada ek loglama veya farklı bir yaklaşım gerekebilir.
                if (newState == StateFlag.State.DENY && detailedFlagLogging) {
                    plugin.getLogger().info("[FlagManager-DETAY] Bayrak '" + flag.getName() + "' DENY olarak ayarlandı. Ada sahibi (" + ownerUUID + ") WorldGuard mekanizmalarıyla bundan muaf olmalıdır.");
                }
            }


            RegionManager regionManager = islandManager.getWGRegionManager(island.getWorld());
            if (regionManager == null) {
                plugin.getLogger().severe("[FlagManager] setIslandFlagState: Bayrak ayarlanırken RegionManager alınamadı.");
                changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken bir hata oluştu (RM).");
                return false;
            }
            regionManager.saveChanges();

            String oldStateString = (oldState == null) ? "VARSAYILAN" : oldState.name();
            String newStateString = (newState == null) ? "VARSAYILAN" : newState.name();

            changer.sendMessage(ChatColor.GREEN + "'" + flag.getName() + "' bayrağı " + ChatColor.AQUA + newStateString + ChatColor.GREEN + " olarak ayarlandı.");
            plugin.getLogger().info("[FlagManager] " + changer.getName() + " -> Ada: " + ownerUUID + ", Bayrak: '" + flag.getName() + "', Eski Durum: " + oldStateString + ", Yeni Durum: " + newStateString + ".");

            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-DETAY] Bölge ID: " + region.getId() + ". Sahipler: " + region.getOwners().toPlayersString() + ", Üyeler: " + region.getMembers().toPlayersString());
                plugin.getLogger().info("[FlagManager-DETAY] Not: Bu ayar genellikle ziyaretçileri etkiler, ada sahibi/üyeleri WorldGuard tarafından genellikle muaftır.");
            }
            return true;
        } catch (StorageException e) {
            plugin.getLogger().log(Level.SEVERE, "[FlagManager] WorldGuard bölgesi '" + region.getId() + "' kaydedilirken hata (bayrak ayarı): " + e.getMessage(), e);
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken bir depolama hatası oluştu.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[FlagManager] setIslandFlagState sırasında beklenmedik hata: " + e.getMessage(), e);
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken beklenmedik bir hata oluştu.");
            return false;
        }
    }

    public void applyDefaultFlagsToRegion(ProtectedRegion region) {
        if (region == null) {
            plugin.getLogger().warning("[FlagManager] applyDefaultFlagsToRegion: Bölge null geldi, varsayılan bayraklar uygulanamadı.");
            return;
        }
        plugin.getLogger().info("[FlagManager] '" + region.getId() + "' bölgesi için varsayılan ada bayrakları uygulanıyor...");
        for (Map.Entry<StateFlag, StateFlag.State> entry : defaultIslandFlags.entrySet()) {
            region.setFlag(entry.getKey(), entry.getValue());
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-DEBUG] Varsayılan bayrak uygulandı: Bölge=" + region.getId() + ", Bayrak=" + entry.getKey().getName() + " -> " + (entry.getValue() == null ? "VARSAYILAN" : entry.getValue().name()));
            }
        }
        // Sahip muafiyetini pekiştirmek için (WorldGuard'ın normalde yapması gerekse de):
        // DefaultDomain owners = region.getOwners();
        // if (!owners.isEmpty()) {
        //     for (StateFlag crucialFlag : ownerBypassFlags) {
        //         region.setFlag(crucialFlag, DefaultDomain.OWNERS, StateFlag.State.ALLOW);
        //         if (detailedFlagLogging) {
        //             plugin.getLogger().info("[FlagManager-DEBUG] Sahip muafiyeti pekiştirildi: Bayrak=" + crucialFlag.getName() + " -> OWNERS ALLOW. Bölge=" + region.getId());
        //         }
        //     }
        // }
    }

    public List<StateFlag> getManagableFlags() {
        return Collections.unmodifiableList(new ArrayList<>(defaultIslandFlags.keySet()));
    }

    public StateFlag.State getDefaultStateFor(StateFlag flag) {
        return defaultIslandFlags.get(flag);
    }

    public Map<StateFlag, StateFlag.State> getAllDefaultIslandFlags() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(defaultIslandFlags));
    }
}