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
            plugin.getLogger().warning(String.format("setIslandName failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to set island name for island %s (Owner: %s) to '%s' by actor %s (UUID: %s).",
                islandId, island.getOwnerUUID(), newName, actor.getName(), actor.getUniqueId()));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyname")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın ismini değiştirme yetkiniz yok.");
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: Actor %s lacks permission. New name: '%s'",
                    islandId, actor.getName(), newName));
            return false;
        }

        String namePattern = plugin.getConfig().getString("island.name.pattern", "^[a-zA-Z0-9_\\- ]{3,25}$");
        int minLength = plugin.getConfig().getInt("island.name.min-length", 3);
        int maxLength = plugin.getConfig().getInt("island.name.max-length", 25);

        if (newName == null || newName.trim().isEmpty()) {
            actor.sendMessage(ChatColor.RED + "Ada ismi boş olamaz.");
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name is null or empty. Actor: %s", islandId, actor.getName()));
            return false;
        }
        if (newName.length() < minLength || newName.length() > maxLength) {
            actor.sendMessage(ChatColor.RED + "Ada adı " + minLength + "-" + maxLength + " karakter uzunluğunda olmalı.");
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name '%s' (length %d) does not meet length requirements (%d-%d). Actor: %s",
                    islandId, newName, newName.length(), minLength, maxLength, actor.getName()));
            return false;
        }
        if (!newName.matches(namePattern)) {
            actor.sendMessage(ChatColor.RED + "Ada adı sadece harf, rakam, boşluk, '_' veya '-' içerebilir.");
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name '%s' does not match pattern '%s'. Actor: %s",
                    islandId, newName, namePattern, actor.getName()));
            return false;
        }

        island.setIslandName(newName);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(ChatColor.GREEN + "Adanın yeni ismi '" + ChatColor.AQUA + newName + ChatColor.GREEN + "' olarak ayarlandı.");
        plugin.getLogger().info(String.format("Successfully set island name for island %s (Owner: %s) to '%s' by actor %s.",
                islandId, island.getOwnerUUID(), newName, actor.getName()));
        return true;
    }

    public boolean setIslandVisibility(Player actor, Island island, boolean isPublic) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "Ayarlarını değiştireceğin bir adan yok!");
            plugin.getLogger().warning(String.format("setIslandVisibility failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to set island visibility for island %s (Owner: %s) to %s by actor %s (UUID: %s).",
                islandId, island.getOwnerUUID(), (isPublic ? "PUBLIC" : "PRIVATE"), actor.getName(), actor.getUniqueId()));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyvisibility")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın görünürlüğünü değiştirme yetkiniz yok.");
            plugin.getLogger().warning(String.format("setIslandVisibility failed for island %s: Actor %s lacks permission. Target visibility: %s",
                    islandId, actor.getName(), (isPublic ? "PUBLIC" : "PRIVATE")));
            return false;
        }

        island.setPublic(isPublic);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(ChatColor.GREEN + "Adanın ziyaretçi durumu " +
                (isPublic ? ChatColor.AQUA + "HERKESE AÇIK" : ChatColor.GOLD + "ÖZEL (Sadece Üyeler)") +
                ChatColor.GREEN + " olarak ayarlandı.");
        plugin.getLogger().info(String.format("Successfully set island visibility for island %s (Owner: %s) to %s by actor %s.",
                islandId, island.getOwnerUUID(), (isPublic ? "PUBLIC" : "PRIVATE"), actor.getName()));
        return true;
    }

    public boolean toggleIslandBoundaryEnforcement(Player actor, Island island) {
        if (island == null) {
            actor.sendMessage(ChatColor.RED + "Ayarlarını değiştireceğin bir adan yok!");
            plugin.getLogger().warning(String.format("toggleIslandBoundaryEnforcement failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        boolean currentEnforcement = island.areBoundariesEnforced();
        plugin.getLogger().info(String.format("Attempting to toggle island boundary enforcement for island %s (Owner: %s) by actor %s (UUID: %s). Current state: %s.",
                islandId, island.getOwnerUUID(), actor.getName(), actor.getUniqueId(), (currentEnforcement ? "ENFORCED" : "NOT ENFORCED")));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.toggleboundaries")) {
            actor.sendMessage(ChatColor.RED + "Bu adanın sınır ayarını değiştirme yetkiniz yok.");
            plugin.getLogger().warning(String.format("toggleIslandBoundaryEnforcement failed for island %s: Actor %s lacks permission.",
                    islandId, actor.getName()));
            return false;
        }

        island.setBoundariesEnforced(!currentEnforcement);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        boolean newEnforcement = island.areBoundariesEnforced();
        actor.sendMessage(ChatColor.GREEN + "Ada sınırları koruması " +
                (newEnforcement ? ChatColor.AQUA + "AKTİF" : ChatColor.GOLD + "PASİF") +
                ChatColor.GREEN + " olarak ayarlandı.");
        plugin.getLogger().info(String.format("Successfully toggled island boundary enforcement for island %s (Owner: %s) to %s by actor %s. Old state: %s.",
                islandId, island.getOwnerUUID(), (newEnforcement ? "ENFORCED" : "NOT ENFORCED"), actor.getName(), (currentEnforcement ? "ENFORCED" : "NOT ENFORCED")));
        return true;
    }

    // İleride eklenebilecek diğer ayar metodları:
    // - public boolean setIslandDescription(Player actor, Island island, String description)
    // - public boolean setIslandIcon(Player actor, Island island, Material iconMaterial)
    // - public boolean setVisitorSpawn(Player actor, Island island, Location visitorSpawnLocation)
    //   (Bu IslandTeleportManager'a da gidebilir veya buradan çağrılabilir)
}