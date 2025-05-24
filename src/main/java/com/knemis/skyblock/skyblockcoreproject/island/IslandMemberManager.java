package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandMemberManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    // IslandManager'a hala ProtectedRegion almak için ihtiyaç duyulabilir veya bu metod IslandUtils'e taşınabilir.
    // Şimdilik IslandManager'daki getProtectedRegion ve getWGRegionManager metodlarını kullandığını varsayalım.
    // Bu metodlar daha sonra IslandLifecycleManager veya IslandUtils'e taşınabilir.
    private final IslandManager islandManager; // Geçici olarak ProtectedRegion işlemleri için

    public IslandMemberManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandManager = islandManager; // TODO: Bu bağımlılığı azaltmak veya kaldırmak iyi olur.
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

        int maxMembers = plugin.getConfig().getInt("island.max-members", 3); // Sahip hariç üye sayısı
        if (island.getMembers().size() >= maxMembers) {
            actor.sendMessage(ChatColor.RED + "Maksimum üye sayısına (" + maxMembers + ") ulaştınız.");
            return false;
        }

        island.addMember(targetMember.getUniqueId());

        // WorldGuard bölgesine üye olarak ekle
        ProtectedRegion region = islandManager.getProtectedRegion(island.getOwnerUUID());
        if (region != null && island.getWorld() != null) {
            region.getMembers().addPlayer(targetMember.getUniqueId());
            RegionManager regionManager = islandManager.getWGRegionManager(island.getWorld());
            if (regionManager != null) {
                try {
                    regionManager.saveChanges();
                    plugin.getLogger().info(targetMember.getName() + ", " + island.getIslandName() + " (" + island.getOwnerUUID() + ") adasına WorldGuard üyesi olarak eklendi.");
                } catch (StorageException e) {
                    plugin.getLogger().log(Level.SEVERE, "WorldGuard üyesi eklenirken bölge kaydedilemedi: " + e.getMessage(), e);
                }
            }
        } else {
            plugin.getLogger().warning(island.getIslandName() + " adasına " + targetMember.getName() + " WG üyesi olarak eklenemedi (bölge veya dünya bulunamadı).");
        }

        islandDataHandler.addOrUpdateIslandData(island); // Değişikliği kaydet
        islandDataHandler.saveChangesToDisk();       // Diske yaz

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
        ProtectedRegion region = islandManager.getProtectedRegion(island.getOwnerUUID());
        if (region != null && island.getWorld() != null) {
            region.getMembers().removePlayer(targetMember.getUniqueId());
            RegionManager regionManager = islandManager.getWGRegionManager(island.getWorld());
            if (regionManager != null) {
                try {
                    regionManager.saveChanges();
                    plugin.getLogger().info(targetMember.getName() + ", " + island.getIslandName() + " (" + island.getOwnerUUID() + ") adasından WorldGuard üyesi olarak çıkarıldı.");
                } catch (StorageException e) {
                    plugin.getLogger().log(Level.SEVERE, "WorldGuard üyesi çıkarılırken bölge kaydedilemedi: " + e.getMessage(), e);
                }
            }
        } else {
            plugin.getLogger().warning(island.getIslandName() + " adasından " + targetMember.getName() + " WG üyesi olarak çıkarılamadı (bölge veya dünya bulunamadı).");
        }

        islandDataHandler.addOrUpdateIslandData(island); // Değişikliği kaydet
        islandDataHandler.saveChangesToDisk();       // Diske yaz

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
        // Eğer sahip de dahil edilecekse, island.getOwnerUUID().equals(playerUUID) || island.isMember(playerUUID)
        return island.isMember(playerUUID);
    }
}