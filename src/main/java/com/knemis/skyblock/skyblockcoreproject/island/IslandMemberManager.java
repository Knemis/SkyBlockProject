package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
// LuckPerms importları burada doğrudan kullanılmıyor, SkyBlockProject üzerinden erişiliyor.

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            actor.sendMessage(ChatColor.RED + "İşlem yapılacak ada bulunamadı.");
            return false;
        }
        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.manage_members")) {
            actor.sendMessage(ChatColor.RED + "Bu adaya üye ekleme yetkiniz yok.");
            return false;
        }
        if (targetMember.getUniqueId().equals(island.getOwnerUUID())) {
            actor.sendMessage(ChatColor.RED + "Kendinizi veya ada sahibini üye olarak ekleyemezsiniz.");
            return false;
        }
        if (island.isMember(targetMember.getUniqueId())) {
            actor.sendMessage(ChatColor.YELLOW + targetMember.getName() + " zaten bu adanın bir üyesi.");
            return false;
        }

        int maxMembers = plugin.getConfig().getInt("island.max-members", 3);
        if (island.getMembers().size() >= maxMembers) {
            actor.sendMessage(ChatColor.RED + "Maksimum üye sayısına (" + maxMembers + ") ulaştınız.");
            return false;
        }

        island.addMember(targetMember.getUniqueId());

        // WorldGuard bölgesine üye olarak ekle
        World islandWorld = island.getWorld();
        if (islandWorld != null) {
            RegionManager regionManager = plugin.getRegionManager(islandWorld);
            if (regionManager != null) {
                String regionId = getRegionId(island.getOwnerUUID());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    region.getMembers().addPlayer(targetMember.getUniqueId());
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(targetMember.getName() + ", " + island.getIslandName() + " (" + island.getOwnerUUID() + ") adasına WorldGuard üyesi olarak eklendi.");
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "WorldGuard üyesi eklenirken bölge kaydedilemedi: " + regionId, e);
                        actor.sendMessage(ChatColor.RED + "Ada üyesi WorldGuard'a eklenirken bir hata oluştu.");
                        // Başarısız olursa üyeliği geri alabiliriz veya hata mesajı verebiliriz.
                        // Şimdilik sadece logla ve devam et.
                    }
                } else {
                    plugin.getLogger().warning(island.getIslandName() + " adası için WorldGuard bölgesi (" + regionId + ") bulunamadı. WG üyesi eklenemedi.");
                }
            } else {
                plugin.getLogger().warning("WorldGuard RegionManager, " + islandWorld.getName() + " dünyası için alınamadı. WG üyesi eklenemedi.");
            }
        } else {
            plugin.getLogger().warning(island.getIslandName() + " adası için dünya null. WG üyesi eklenemedi.");
        }

        islandDataHandler.addOrUpdateIslandData(island); // Değişikliği IslandDataHandler ile kaydet
        islandDataHandler.saveChangesToDisk(); // Diske yaz

        actor.sendMessage(ChatColor.GREEN + targetMember.getName() + " başarıyla '" + island.getIslandName() + "' adasına üye olarak eklendi.");
        if (targetMember.isOnline() && targetMember.getPlayer() != null) {
            targetMember.getPlayer().sendMessage(ChatColor.GREEN + actor.getName() + " sizi '" + island.getIslandName() + "' adlı adasına üye olarak ekledi!");
        }
        return true;
    }

    /**
     * Bir oyuncuyu bir adanın üyeliğinden çıkarır.
     * @param island Üye çıkarılacak ada.
     * @param targetMember Çıkarılacak oyuncu.
     * @param actor İşlemi yapan oyuncu (genellikle ada sahibi veya üyenin kendisi).
     * @return İşlem başarılıysa true.
     */
    public boolean removeMember(Island island, OfflinePlayer targetMember, Player actor) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "İşlem yapılacak ada bulunamadı.");
            return false;
        }

        boolean isSelfLeave = actor.getUniqueId().equals(targetMember.getUniqueId());
        if (!isSelfLeave && !island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.manage_members")) {
            actor.sendMessage(ChatColor.RED + "Bu adadan üye çıkarma yetkiniz yok.");
            return false;
        }

        if (!island.isMember(targetMember.getUniqueId())) {
            actor.sendMessage(ChatColor.YELLOW + targetMember.getName() + " zaten bu adanın bir üyesi değil.");
            return false;
        }

        island.removeMember(targetMember.getUniqueId());

        // WorldGuard bölgesinden üyeyi çıkar
        World islandWorld = island.getWorld();
        if (islandWorld != null) {
            RegionManager regionManager = plugin.getRegionManager(islandWorld);
            if (regionManager != null) {
                String regionId = getRegionId(island.getOwnerUUID());
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    region.getMembers().removePlayer(targetMember.getUniqueId());
                    try {
                        regionManager.saveChanges();
                        plugin.getLogger().info(targetMember.getName() + ", " + island.getIslandName() + " (" + island.getOwnerUUID() + ") adasından WorldGuard üyesi olarak çıkarıldı.");
                    } catch (StorageException e) {
                        plugin.getLogger().log(Level.SEVERE, "WorldGuard üyesi çıkarılırken bölge kaydedilemedi: " + regionId, e);
                        // Hata durumunda mesaj verilebilir.
                    }
                } else {
                    plugin.getLogger().warning(island.getIslandName() + " adası için WorldGuard bölgesi (" + regionId + ") bulunamadı. WG üyesi çıkarılamadı.");
                }
            } else {
                plugin.getLogger().warning("WorldGuard RegionManager, " + islandWorld.getName() + " dünyası için alınamadı. WG üyesi çıkarılamadı.");
            }
        } else {
            plugin.getLogger().warning(island.getIslandName() + " adası için dünya null. WG üyesi çıkarılamadı.");
        }

        islandDataHandler.addOrUpdateIslandData(island); // Değişikliği IslandDataHandler ile kaydet
        islandDataHandler.saveChangesToDisk(); // Diske yaz

        if (isSelfLeave) {
            actor.sendMessage(ChatColor.GREEN + "'" + island.getIslandName() + "' adasının üyeliğinden başarıyla ayrıldınız.");
        } else {
            actor.sendMessage(ChatColor.GREEN + targetMember.getName() + " başarıyla '" + island.getIslandName() + "' adasının üyeliğinden çıkarıldı.");
            if (targetMember.isOnline() && targetMember.getPlayer() != null) {
                targetMember.getPlayer().sendMessage(ChatColor.RED + actor.getName() + " sizi '" + island.getIslandName() + "' adlı adasının üyeliğinden çıkardı!");
            }
        }
        return true;
    }

    /**
     * Bir adanın üyelerini listeler.
     * @param island Bilgileri alınacak ada.
     * @return Ada üyelerinin OfflinePlayer listesi.
     */
    public List<OfflinePlayer> getIslandMembers(Island island) {
        if (island == null) {
            return Collections.emptyList();
        }
        return island.getMembers().stream()
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());
    }

    /**
     * Bir oyuncunun belirli bir adada üye olup olmadığını kontrol eder.
     * @param island Kontrol edilecek ada.
     * @param playerUUID Kontrol edilecek oyuncunun UUID'si.
     * @return Üye ise true.
     */
    public boolean isMemberOfIsland(Island island, UUID playerUUID) {
        if (island == null || playerUUID == null) {
            return false;
        }
        // Ada sahibi de bir nevi "en yetkili üye"dir, ancak isMember genellikle eklenenleri kasteder.
        // Eğer sahip de dahil edilecekse: island.getOwnerUUID().equals(playerUUID) || island.isMember(playerUUID)
        return island.isMember(playerUUID);
    }
}