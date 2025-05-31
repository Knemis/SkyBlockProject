package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            actor.sendMessage(Component.text("Ayarlarını değiştireceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandName failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to set island name for island %s (Owner: %s) to '%s' by actor %s (UUID: %s).",
                islandId, island.getOwnerUUID(), newName, actor.getName(), actor.getUniqueId()));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyname")) {
            actor.sendMessage(Component.text("Bu adanın ismini değiştirme yetkiniz yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: Actor %s lacks permission. New name: '%s'",
                    islandId, actor.getName(), newName));
            return false;
        }

        String namePattern = plugin.getConfig().getString("island.name.pattern", "^[a-zA-Z0-9_\\- ]{3,25}$");
        int minLength = plugin.getConfig().getInt("island.name.min-length", 3);
        int maxLength = plugin.getConfig().getInt("island.name.max-length", 25);

        if (newName == null || newName.trim().isEmpty()) {
            actor.sendMessage(Component.text("Ada ismi boş olamaz.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name is null or empty. Actor: %s", islandId, actor.getName()));
            return false;
        }
        if (newName.length() < minLength || newName.length() > maxLength) {
            actor.sendMessage(Component.text("Ada adı " + minLength + "-" + maxLength + " karakter uzunluğunda olmalı.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name '%s' (length %d) does not meet length requirements (%d-%d). Actor: %s",
                    islandId, newName, newName.length(), minLength, maxLength, actor.getName()));
            return false;
        }
        if (!newName.matches(namePattern)) {
            actor.sendMessage(Component.text("Ada adı sadece harf, rakam, boşluk, '_' veya '-' içerebilir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandName failed for island %s: New name '%s' does not match pattern '%s'. Actor: %s",
                    islandId, newName, namePattern, actor.getName()));
            return false;
        }

        island.setIslandName(newName);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(Component.text("Adanın yeni ismi '", NamedTextColor.GREEN)
                .append(Component.text(newName, NamedTextColor.AQUA))
                .append(Component.text("' olarak ayarlandı.", NamedTextColor.GREEN)));
        plugin.getLogger().info(String.format("Successfully set island name for island %s (Owner: %s) to '%s' by actor %s.",
                islandId, island.getOwnerUUID(), newName, actor.getName()));
        return true;
    }

    public boolean setIslandVisibility(Player actor, Island island, boolean isPublic) {
        if (island == null) {
            actor.sendMessage(Component.text("Ayarlarını değiştireceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandVisibility failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        plugin.getLogger().info(String.format("Attempting to set island visibility for island %s (Owner: %s) to %s by actor %s (UUID: %s).",
                islandId, island.getOwnerUUID(), (isPublic ? "PUBLIC" : "PRIVATE"), actor.getName(), actor.getUniqueId()));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.setanyvisibility")) {
            actor.sendMessage(Component.text("Bu adanın görünürlüğünü değiştirme yetkiniz yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("setIslandVisibility failed for island %s: Actor %s lacks permission. Target visibility: %s",
                    islandId, actor.getName(), (isPublic ? "PUBLIC" : "PRIVATE")));
            return false;
        }

        island.setPublic(isPublic);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        actor.sendMessage(Component.text("Adanın ziyaretçi durumu ", NamedTextColor.GREEN)
                .append(isPublic ? Component.text("HERKESE AÇIK", NamedTextColor.AQUA) : Component.text("ÖZEL (Sadece Üyeler)", NamedTextColor.GOLD))
                .append(Component.text(" olarak ayarlandı.", NamedTextColor.GREEN)));
        plugin.getLogger().info(String.format("Successfully set island visibility for island %s (Owner: %s) to %s by actor %s.",
                islandId, island.getOwnerUUID(), (isPublic ? "PUBLIC" : "PRIVATE"), actor.getName()));
        return true;
    }

    public boolean toggleIslandBoundaryEnforcement(Player actor, Island island) {
        if (island == null) {
            actor.sendMessage(Component.text("Ayarlarını değiştireceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("toggleIslandBoundaryEnforcement failed: Island object is null for actor %s (UUID: %s).", actor.getName(), actor.getUniqueId()));
            return false;
        }
        String islandId = island.getRegionId() != null ? island.getRegionId() : "UNKNOWN_ID_" + island.getOwnerUUID();
        boolean currentEnforcement = island.areBoundariesEnforced();
        plugin.getLogger().info(String.format("Attempting to toggle island boundary enforcement for island %s (Owner: %s) by actor %s (UUID: %s). Current state: %s.",
                islandId, island.getOwnerUUID(), actor.getName(), actor.getUniqueId(), (currentEnforcement ? "ENFORCED" : "NOT ENFORCED")));

        if (!island.getOwnerUUID().equals(actor.getUniqueId()) && !actor.hasPermission("skyblock.admin.toggleboundaries")) {
            actor.sendMessage(Component.text("Bu adanın sınır ayarını değiştirme yetkiniz yok.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("toggleIslandBoundaryEnforcement failed for island %s: Actor %s lacks permission.",
                    islandId, actor.getName()));
            return false;
        }

        island.setBoundariesEnforced(!currentEnforcement);
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();

        boolean newEnforcement = island.areBoundariesEnforced();
        actor.sendMessage(Component.text("Ada sınırları koruması ", NamedTextColor.GREEN)
                .append(newEnforcement ? Component.text("AKTİF", NamedTextColor.AQUA) : Component.text("PASİF", NamedTextColor.GOLD))
                .append(Component.text(" olarak ayarlandı.", NamedTextColor.GREEN)));
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