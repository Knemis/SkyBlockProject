package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // IslandManager yerine

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class IslandFlagManager {

    private final SkyBlockProject plugin;
    // IslandManager, IslandDataHandler ile değiştirildi
    private final IslandDataHandler islandDataHandler;
    private final Map<StateFlag, StateFlag.State> defaultIslandFlags;
    private final boolean detailedFlagLogging;

    // Bu liste, hangi bayrakların sahip için kritik olduğu bilgisini tutar.
    // WorldGuard'ın sahip muafiyeti ve LuckPerms bypass izinleri ana mekanizmadır.
    private final List<StateFlag> criticalOwnerBypassFlags = Arrays.asList(
            Flags.BUILD,
            Flags.INTERACT,
            Flags.CHEST_ACCESS,
            Flags.USE,
            Flags.ITEM_DROP,
            Flags.ITEM_PICKUP,
            Flags.TRAMPLE_BLOCKS,
            Flags.RIDE
    );

    // Constructor güncellendi
    public IslandFlagManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.defaultIslandFlags = new LinkedHashMap<>();
        this.detailedFlagLogging = plugin.getConfig().getBoolean("logging.detailed-flag-changes", false);
        initializeDefaultFlags();
    }

    private void initializeDefaultFlags() {
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
        defaultIslandFlags.put(Flags.POTION_SPLASH, StateFlag.State.ALLOW); // Genellikle izin verilir
        defaultIslandFlags.put(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.LAVA_FLOW, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.WATER_FLOW, StateFlag.State.ALLOW); // Ada yapımı için önemli olabilir
        defaultIslandFlags.put(Flags.MOB_SPAWNING, StateFlag.State.ALLOW); // Oyuncunun kontrolünde olmalı
        defaultIslandFlags.put(Flags.LEAF_DECAY, StateFlag.State.ALLOW);
        defaultIslandFlags.put(Flags.LIGHTNING, StateFlag.State.DENY);
        defaultIslandFlags.put(Flags.SNOW_FALL, StateFlag.State.ALLOW);
    }

    private String getRegionId(UUID ownerUUID) {
        return "skyblock_island_" + ownerUUID.toString();
    }

    private ProtectedRegion getProtectedRegionForIsland(UUID ownerUUID) {
        if (ownerUUID == null) {
            plugin.getLogger().warning("[FlagManager] getProtectedRegionForIsland çağrısında ownerUUID null geldi.");
            return null;
        }
        Island island = islandDataHandler.getIslandByOwner(ownerUUID);
        if (island == null || island.getWorld() == null) {
            if (detailedFlagLogging) {
                plugin.getLogger().warning("[FlagManager-DEBUG] getProtectedRegionForIsland: Oyuncu " + ownerUUID + " için ada veya dünya bulunamadı.");
            }
            return null;
        }
        RegionManager regionManager = plugin.getRegionManager(island.getWorld());
        if (regionManager == null) {
            if (detailedFlagLogging) {
                plugin.getLogger().warning("[FlagManager-DEBUG] getProtectedRegionForIsland: Dünya '" + island.getWorld().getName() + "' için RegionManager alınamadı.");
            }
            return null;
        }
        return regionManager.getRegion(getRegionId(ownerUUID));
    }

    public StateFlag.State getIslandFlagState(UUID ownerUUID, StateFlag flag) {
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

        Island island = islandDataHandler.getIslandByOwner(ownerUUID); // IslandDataHandler kullanıldı
        if (island == null || island.getWorld() == null) {
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanacak ada veya dünya bulunamadı.");
            return false;
        }

        ProtectedRegion region = getProtectedRegionForIsland(ownerUUID);
        if (region == null) {
            changer.sendMessage(ChatColor.RED + "Adanın koruma bölgesi bulunamadı.");
            return false;
        }

        // İzin kontrolleri
        if (!changer.getUniqueId().equals(ownerUUID) && !changer.hasPermission("skyblock.admin.setanyflag")) {
            changer.sendMessage(ChatColor.RED + "Bu adanın bayraklarını değiştirme izniniz yok.");
            return false;
        }
        String specificFlagPermission = "skyblock.flags.manage." + flag.getName().toLowerCase().replace("_", "-");
        if (!changer.getUniqueId().equals(ownerUUID) && !changer.hasPermission("skyblock.admin.setanyflag") && !changer.hasPermission(specificFlagPermission)) {
            changer.sendMessage(ChatColor.RED + "'" + flag.getName() + "' bayrağını değiştirmek için özel izniniz ("+ specificFlagPermission +") bulunmuyor.");
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager] Oyuncu " + changer.getName() + ", " + ownerUUID + " adasındaki '" + flag.getName() + "' bayrağını değiştirmeye çalıştı ancak '" + specificFlagPermission + "' izni yok.");
            }
            return false;
        }

        StateFlag.State oldState = region.getFlag(flag);
        try {
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-ATTEMPT] Genel bayrak ayarlanıyor: region=" + region.getId() + ", flag=" + flag.getName() + ", newState=" + (newState == null ? "null (VARSAYILAN)" : newState.name()));
            }
            region.setFlag(flag, newState); // Genel bayrağı ayarla (herkes için)

            // Sahip (OWNERS) grubu için bayrakları ayarlama WorldGuard'ın kendi iç mantığına
            // (region owners always bypass) ve LuckPerms bypass izinlerine bırakılmıştır.
            // WorldGuard'da region.setFlag(flag, RegionGroup.OWNERS, state) kullanımı
            // bayrağın kendisine RegionGroupFlag özelliği eklenmesini gerektirir ve bu karmaşıktır.
            // En temizi, sahiplerin bypass izinleri (LuckPerms) veya WG'nin varsayılan sahip bypass'ı ile çalışmasıdır.
            if (criticalOwnerBypassFlags.contains(flag) && detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-INFO] Kritik bayrak ("+flag.getName()+") ayarlandı. Sahip muafiyeti WorldGuard'ın varsayılan davranışına ve sunucu izinlerine bırakıldı.");
            }

            RegionManager regionManager = plugin.getRegionManager(island.getWorld()); // Ana plugin üzerinden RegionManager al
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
            }
            return true;
        } catch (Exception e) { // StorageException dahil genel hatalar için
            plugin.getLogger().log(Level.SEVERE, "[FlagManager] setIslandFlagState sırasında bayrak ayarlanırken hata: " + e.getMessage(), e);
            changer.sendMessage(ChatColor.RED + "Bayrak ayarlanırken beklenmedik bir hata oluştu. Lütfen konsolu kontrol edin.");
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
            StateFlag flag = entry.getKey();
            StateFlag.State defaultStateForRegion = entry.getValue();

            region.setFlag(flag, defaultStateForRegion); // Sadece genel bayrağı (herkes için) ayarla
            if (detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-DEBUG] Varsayılan genel bayrak uygulandı: " + flag.getName() + " -> " + (defaultStateForRegion == null ? "VARSAYILAN" : defaultStateForRegion.name()) + ", Bölge=" + region.getId());
            }
            // Sahip grubu için özel bayrak ayarları WorldGuard'ın bypass mekanizmalarına ve LuckPerms'e bırakılmıştır.
            if (criticalOwnerBypassFlags.contains(flag) && detailedFlagLogging) {
                plugin.getLogger().info("[FlagManager-INFO] applyDefaultFlags - Kritik bayrak ("+flag.getName()+") için sahip muafiyeti WG varsayılanlarına ve sunucu izinlerine bırakıldı.");
            }
        }
        // Değişikliklerin kaydedilmesi bu metodu çağıran yerin sorumluluğundadır (örn: IslandLifecycleManager içinde createIsland).
    }

    public List<StateFlag> getManagableFlags() {
        // GUI'de gösterilecek ve yönetilebilecek bayrakların listesi
        return Collections.unmodifiableList(new ArrayList<>(defaultIslandFlags.keySet()));
    }

    public StateFlag.State getDefaultStateFor(StateFlag flag) {
        // Bir bayrağın bu eklenti tarafından tanımlanan varsayılan durumunu döndürür
        return defaultIslandFlags.get(flag);
    }

    public Map<StateFlag, StateFlag.State> getAllDefaultIslandFlags() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(defaultIslandFlags));
    }
}