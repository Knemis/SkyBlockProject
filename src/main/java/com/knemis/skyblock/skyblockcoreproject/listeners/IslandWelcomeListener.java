package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandWelcomeListener implements Listener {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandWelcomeManager welcomeManager;
    private final Map<UUID, UUID> lastPlayerIslandOwner;

    public IslandWelcomeListener(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandWelcomeManager welcomeManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.welcomeManager = welcomeManager;
        this.lastPlayerIslandOwner = new HashMap<>();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerIslandEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo(); // to değişkeni PlayerMoveEvent'te null olmaz.

        // DÜZELTME 1: 'to == null' kontrolü kaldırıldı.
        // Aynı blok içindeyse veya dünya değişmemişse işlem yapma
        // 'to' null olamayacağı için to.getBlockX() vb. güvenle çağrılabilir.
        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ() &&
                from.getWorld().equals(to.getWorld())) {
            return;
        }

        // Sadece skyblock dünyasında çalışsın
        if (!to.getWorld().getName().equals(plugin.getConfig().getString("skyblock-world-name", "skyblock_world"))) {
            lastPlayerIslandOwner.remove(player.getUniqueId());
            return;
        }

        Island islandAtTo = islandDataHandler.getIslandAt(to);
        UUID currentIslandOwnerUUID = (islandAtTo != null) ? islandAtTo.getOwnerUUID() : null;
        UUID lastIslandOwnerUUID = lastPlayerIslandOwner.get(player.getUniqueId());

        if (currentIslandOwnerUUID != null && !currentIslandOwnerUUID.equals(lastIslandOwnerUUID)) {
            // currentIslandOwnerUUID null değilse, islandAtTo da null değildir.

            // DÜZELTME 2: Gereksiz 'islandAtTo != null' kontrolü kaldırıldı.
            if (currentIslandOwnerUUID.equals(player.getUniqueId()) || islandAtTo.isMember(player.getUniqueId())) {
                lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
                return;
            }

            String welcomeMessage = welcomeManager.getWelcomeMessage(islandAtTo);
            if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMessage));
            }
        }

        if (currentIslandOwnerUUID != null) {
            lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
        } else {
            lastPlayerIslandOwner.remove(player.getUniqueId());
        }
    }
}