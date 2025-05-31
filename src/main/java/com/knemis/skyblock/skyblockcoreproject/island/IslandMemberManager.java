package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
// LuckPerms importları burada doğrudan kullanılmıyor, SkyBlockProject üzerinden erişiliyor.

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandMemberManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    // Eski IslandManager yerine IslandLifecycleManager, özellikle regionId almak için
    private final IslandLifecycleManager islandLifecycleManager;

    // Constructor güncellendi
    public IslandMemberManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandLifecycleManager islandLifecycleManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
    }

    private String getRegionId(UUID ownerUUID) {
        // IslandLifecycleManager'da bu metodun olduğunu varsayıyoruz veya doğrudan oluşturuyoruz.
        // IslandLifecycleManager'da getRegionId metodu (source 536) bulunuyor.
        if (this.islandLifecycleManager != null) {
            return this.islandLifecycleManager.getRegionId(ownerUUID);
        }
        // Fallback veya hata durumu
        plugin.getLogger().warning("IslandMemberManager: IslandLifecycleManager null olduğu için region ID oluşturulamadı, manuel olarak oluşturuluyor.");
        return "skyblock_island_" + ownerUUID.toString();
    }


    /**
     * Bir oyuncuyu bir adaya üye olarak ekler.
     * @param island Üye eklenecek ada.
     * @param targetMember Eklenecek oyuncu.
     * @param actor İşlemi yapan oyuncu (genellikle ada sahibi).
     * @return İşlem başarılıysa true.
     */
    public boolean addMember(Island island, OfflinePlayer targetMember, Player actor) {
        if (island == null) {
            actor.sendMessage(Component.text("İşlem yapılacak ada bulunamadı.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("addMember failed: Island object is null. Actor: %s, Target: %s", actor.getName(), targetMember.getName()));
            return false;
        }

        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to add member %s (UUID: %s) to island %s (Owner: %s) by actor %s (UUID: %s)",
                targetMember.getName(), targetMember.getUniqueId(), islandId, island.getOwnerUUID(), actor.getName(), actor.getUniqueId()));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.manage_members")) {
            actor.sendMessage(Component.text("Bu adaya üye ekleme yetkiniz yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("addMember failed for island %s: Actor %s lacks permission. Target: %s",
                    islandId, actor.getName(), targetMember.getName()));
            return false;
        }
        if (targetMember.getUniqueId().equals(island.getOwnerUUID())) {
            actor.sendMessage(Component.text("Kendinizi veya ada sahibini üye olarak ekleyemezsiniz.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("addMember failed for island %s: Target %s is the owner. Actor: %s",
                    islandId, targetMember.getName(), actor.getName()));
            return false;
        }
        if (island.isMember(targetMember.getUniqueId())) {
            actor.sendMessage(Component.text(targetMember.getName() + " zaten bu adanın bir üyesi.", NamedTextColor.YELLOW));
            plugin.getLogger().warning(String.format("addMember failed for island %s: Target %s is already a member. Actor: %s",
                    islandId, targetMember.getName(), actor.getName()));
            return false;
        }

        int maxMembers = plugin.getConfig().getInt("island.max-members", 3);
        if (island.getMembers().size() >= maxMembers) {
            actor.sendMessage(Component.text("Maksimum üye sayısına (" + maxMembers + ") ulaştınız.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("addMember failed for island %s: Island is full (Max members: %d). Target: %s, Actor: %s",
                    islandId, maxMembers, targetMember.getName(), actor.getName()));
            return false;
        }

        island.addMember(targetMember.getUniqueId());

        World islandWorld = island.getWorld();
        if (islandWorld != null) {
            RegionManager regionManager = plugin.getRegionManager(islandWorld);
            String wgRegionId = getRegionId(island.getOwnerUUID()); // Use this for consistency in logs
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(wgRegionId);
                if (region != null) {
                    region.getMembers().addPlayer(targetMember.getUniqueId());
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(String.format("Added %s to WorldGuard region %s members for island %s.",
                                targetMember.getName(), wgRegionId, islandId));
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not save WorldGuard region changes for " + wgRegionId + " after adding member " + targetMember.getName(), e);
                        actor.sendMessage(Component.text("Ada üyesi WorldGuard'a eklenirken bir hata oluştu.", NamedTextColor.RED));
                        // Consider rolling back island.addMember if WG fails critically
                    }
                } else {
                    plugin.getLogger().warning(String.format("WorldGuard region %s not found for island %s. Cannot add %s to WG members.",
                            wgRegionId, islandId, targetMember.getName()));
                }
            } else {
                plugin.getLogger().warning(String.format("WorldGuard RegionManager is null for world %s. Cannot add %s to WG members for island %s.",
                        islandWorld.getName(), targetMember.getName(), islandId));
            }
        } else {
            plugin.getLogger().warning(String.format("Island world is null for island %s. Cannot add %s to WG members.", islandId, targetMember.getName()));
        }

        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(Component.text(targetMember.getName() + " başarıyla '" + island.getIslandName() + "' adasına üye olarak eklendi.", NamedTextColor.GREEN));
        if (targetMember.isOnline() && targetMember.getPlayer() != null) {
            targetMember.getPlayer().sendMessage(Component.text(actor.getName() + " sizi '" + island.getIslandName() + "' adlı adasına üye olarak ekledi!", NamedTextColor.GREEN));
        }
        plugin.getLogger().info(String.format("Successfully added member %s (UUID: %s) to island %s. Actor: %s",
                targetMember.getName(), targetMember.getUniqueId(), islandId, actor.getName()));
        return true;
    }

    public boolean removeMember(Island island, OfflinePlayer targetMember, Player actor) {
        if (island == null) {
            actor.sendMessage(Component.text("İşlem yapılacak ada bulunamadı.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("removeMember failed: Island object is null. Actor: %s, Target: %s", actor.getName(), targetMember.getName()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to remove member %s (UUID: %s) from island %s (Owner: %s) by actor %s (UUID: %s)",
                targetMember.getName(), targetMember.getUniqueId(), islandId, island.getOwnerUUID(), actor.getName(), actor.getUniqueId()));

        boolean isSelfLeave = actor.getUniqueId().equals(targetMember.getUniqueId());
        if (!isSelfLeave && !island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.manage_members")) {
            actor.sendMessage(Component.text("Bu adadan üye çıkarma yetkiniz yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("removeMember failed for island %s: Actor %s lacks permission. Target: %s",
                    islandId, actor.getName(), targetMember.getName()));
            return false;
        }

        if (!island.isMember(targetMember.getUniqueId())) {
            actor.sendMessage(Component.text(targetMember.getName() + " zaten bu adanın bir üyesi değil.", NamedTextColor.YELLOW));
            plugin.getLogger().warning(String.format("removeMember failed for island %s: Target %s is not a member. Actor: %s",
                    islandId, targetMember.getName(), actor.getName()));
            return false;
        }

        island.removeMember(targetMember.getUniqueId());

        World islandWorld = island.getWorld();
        if (islandWorld != null) {
            RegionManager regionManager = plugin.getRegionManager(islandWorld);
            String wgRegionId = getRegionId(island.getOwnerUUID());
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(wgRegionId);
                if (region != null) {
                    region.getMembers().removePlayer(targetMember.getUniqueId());
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(String.format("Removed %s from WorldGuard region %s members for island %s.",
                                targetMember.getName(), wgRegionId, islandId));
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not save WorldGuard region changes for " + wgRegionId + " after removing member " + targetMember.getName(), e);
                    }
                } else {
                    plugin.getLogger().warning(String.format("WorldGuard region %s not found for island %s. Cannot remove %s from WG members.",
                            wgRegionId, islandId, targetMember.getName()));
                }
            } else {
                plugin.getLogger().warning(String.format("WorldGuard RegionManager is null for world %s. Cannot remove %s from WG members for island %s.",
                        islandWorld.getName(), targetMember.getName(), islandId));
            }
        } else {
            plugin.getLogger().warning(String.format("Island world is null for island %s. Cannot remove %s from WG members.", islandId, targetMember.getName()));
        }

        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        if (isSelfLeave) {
            actor.sendMessage(Component.text("'" + island.getIslandName() + "' adasının üyeliğinden başarıyla ayrıldınız.", NamedTextColor.GREEN));
        } else {
            actor.sendMessage(Component.text(targetMember.getName() + " başarıyla '" + island.getIslandName() + "' adasının üyeliğinden çıkarıldı.", NamedTextColor.GREEN));
            if (targetMember.isOnline() && targetMember.getPlayer() != null) {
                targetMember.getPlayer().sendMessage(Component.text(actor.getName() + " sizi '" + island.getIslandName() + "' adlı adasının üyeliğinden çıkardı!", NamedTextColor.RED));
            }
        }
        plugin.getLogger().info(String.format("Successfully removed member %s (UUID: %s) from island %s. Actor: %s",
                targetMember.getName(), targetMember.getUniqueId(), islandId, actor.getName()));
        return true;
    }

    public List<OfflinePlayer> getIslandMembers(Island island) {
        if (island == null) {
            plugin.getLogger().fine("getIslandMembers called with null island."); // Fine level, as this might be called often
            return Collections.emptyList();
        }
        // No specific log for successful retrieval unless debugging member list issues.
        return island.getMembers().stream()
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());
    }

    public boolean isMemberOfIsland(Island island, UUID playerUUID) {
        if (island == null || playerUUID == null) {
            return false;
        }
        // Ada sahibi de bir nevi "en yetkili üye"dir, ancak isMember genellikle eklenenleri kasteder.
        // Eğer sahip de dahil edilecekse: island.getOwnerUUID().equals(playerUUID) || island.isMember(playerUUID)
        return island.isMember(playerUUID);
    }
}