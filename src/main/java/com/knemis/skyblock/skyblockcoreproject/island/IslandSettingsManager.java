package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException; // IslandDataHandler.saveChangesToDisk() için
import java.util.logging.Level;

public class IslandSettingsManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;

    public IslandSettingsManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
    }

    /**
     * Bir adanın ismini ayarlar.
     * @param island Ayarlanacak ada.
     * @param actor İşlemi yapan oyuncu.
     * @param newName Yeni ada ismi.
     * @return İşlem başarılıysa true.
     */
    public boolean setIslandName(Player actor, Island island, String newName) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "Ayarlarını değiştireceğin bir adan yok!");
            return false;
        }
        // Sadece ada sahibi veya özel izni olan değiştirebilsin
        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyname")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın ismini değiştirme yetkiniz yok.");
            return false;
        }

        String namePattern = plugin.getConfig().getString("island.name.pattern", "^[a-zA-Z0-9_\\- ]{3,25}$");
        int minLength = plugin.getConfig().getInt("island.name.min-length", 3);
        int maxLength = plugin.getConfig().getInt("island.name.max-length", 25);

        if (newName == null || newName.trim().isEmpty()) {
            actor.sendMessage(ChatColor.RED + "Ada ismi boş olamaz.");
            return false;
        }
        if (newName.length() < minLength || newName.length() > maxLength) {
            actor.sendMessage(ChatColor.RED + "Ada adı " + minLength + "-" + maxLength + " karakter uzunluğunda olmalı.");
            return false;
        }
        if (!newName.matches(namePattern)) {
            actor.sendMessage(ChatColor.RED + "Ada adı sadece harf, rakam, boşluk, '_' veya '-' içerebilir.");
            return false;
        }
        // TODO: İsteğe bağlı olarak, aynı isimde başka bir ada olup olmadığını kontrol et.

        island.setIslandName(newName);
        islandDataHandler.addOrUpdateIslandData(island); // Veriyi güncelle
        islandDataHandler.saveChangesToDisk();          // Değişiklikleri diske yaz

        actor.sendMessage(ChatColor.GREEN + "Adanın yeni ismi '" + ChatColor.AQUA + newName + ChatColor.GREEN + "' olarak ayarlandı.");
        plugin.getLogger().info(actor.getName() + " adlı oyuncu, " + island.getOwnerUUID() + " ID'li adanın ismini '" + newName + "' olarak değiştirdi.");
        return true;
    }

    /**
     * Bir adanın ziyaretçi görünürlüğünü ayarlar.
     * @param island Ayarlanacak ada.
     * @param actor İşlemi yapan oyuncu.
     * @param isPublic Herkese açık olup olmayacağı.
     * @return İşlem başarılıysa true.
     */
    public boolean setIslandVisibility(Player actor, Island island, boolean isPublic) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "Ayarlarını değiştireceğin bir adan yok!");
            return false;
        }
        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyvisibility")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın görünürlüğünü değiştirme yetkiniz yok.");
            return false;
        }

        island.setPublic(isPublic);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(ChatColor.GREEN + "Adanın ziyaretçi durumu " +
                (isPublic ? ChatColor.AQUA + "HERKESE AÇIK" : ChatColor.GOLD + "ÖZEL (Sadece Üyeler)") +
                ChatColor.GREEN + " olarak ayarlandı.");
        plugin.getLogger().info(actor.getName() + " adlı oyuncu, " + island.getOwnerUUID() + " ID'li adanın görünürlüğünü " + (isPublic ? "PUBLIC" : "PRIVATE") + " olarak ayarladı.");
        return true;
    }

    /**
     * Bir adanın sınırlarının zorunlu olup olmadığını değiştirir (toggle).
     * @param island Ayarlanacak ada.
     * @param actor İşlemi yapan oyuncu.
     * @return İşlem başarılıysa true.
     */
    public boolean toggleIslandBoundaryEnforcement(Player actor, Island island) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "Ayarlarını değiştireceğin bir adan yok!");
            return false;
        }
        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.toggleboundaries")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın sınır ayarını değiştirme yetkiniz yok.");
            return false;
        }

        island.setBoundariesEnforced(!island.areBoundariesEnforced()); // Mevcut durumun tersini ayarla
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(ChatColor.GREEN + "Ada sınırları koruması " +
                (island.areBoundariesEnforced() ? ChatColor.AQUA + "AKTİF" : ChatColor.GOLD + "PASİF") +
                ChatColor.GREEN + " olarak ayarlandı.");
        plugin.getLogger().info(actor.getName() + " adlı oyuncu, " + island.getOwnerUUID() + " ID'li adanın sınır korumasını " + (island.areBoundariesEnforced() ? "AKTİF" : "PASİF") + " yaptı.");
        return true;
    }

    // İleride eklenebilecek diğer ayar metodları:
    // - public boolean setIslandDescription(Player actor, Island island, String description)
    // - public boolean setIslandIcon(Player actor, Island island, Material iconMaterial)
    // - public boolean setVisitorSpawn(Player actor, Island island, Location visitorSpawnLocation)
    //   (Bu IslandTeleportManager'a da gidebilir veya buradan çağrılabilir)
}