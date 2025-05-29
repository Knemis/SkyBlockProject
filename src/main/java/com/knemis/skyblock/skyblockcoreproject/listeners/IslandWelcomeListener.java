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
        Location to = event.getTo();

        if (to == null) { // Should not happen with PlayerMoveEvent, but good practice
            plugin.getLogger().warning(String.format("IslandWelcomeListener: PlayerMoveEvent for %s had a null 'to' location.", player.getName()));
            return;
        }

        // Aynı blok içindeyse veya dünya değişmemişse işlem yapma
        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ() &&
                from.getWorld() != null && to.getWorld() != null && from.getWorld().equals(to.getWorld())) {
            return;
        }

        // Sadece skyblock dünyasında çalışsın
        String skyblockWorldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        if (to.getWorld() == null || !to.getWorld().getName().equals(skyblockWorldName)) {
            if (lastPlayerIslandOwner.containsKey(player.getUniqueId())) {
                plugin.getLogger().info(String.format("Player %s left skyblock world %s. Clearing last island owner.", player.getName(), skyblockWorldName));
                lastPlayerIslandOwner.remove(player.getUniqueId());
            }
            return;
        }

        Island islandAtTo = islandDataHandler.getIslandAt(to);
        UUID currentIslandOwnerUUID = (islandAtTo != null) ? islandAtTo.getOwnerUUID() : null;
        UUID lastIslandOwnerUUID = lastPlayerIslandOwner.get(player.getUniqueId());

        // Log initial trigger of significant movement into skyblock world or between island regions
        // This specific log might be too verbose if every single block change is logged.
        // We are more interested when they cross into a NEW island's territory.

        if (currentIslandOwnerUUID != null && !currentIslandOwnerUUID.equals(lastIslandOwnerUUID)) {
            plugin.getLogger().info(String.format("Player %s moved to a new island region. From owner: %s, To owner: %s, Location: X:%d Y:%d Z:%d",
                    player.getName(),
                    lastIslandOwnerUUID != null ? lastIslandOwnerUUID.toString() : "None",
                    currentIslandOwnerUUID.toString(),
                    to.getBlockX(), to.getBlockY(), to.getBlockZ()));

            // islandAtTo is guaranteed not null here because currentIslandOwnerUUID is not null
            if (currentIslandOwnerUUID.equals(player.getUniqueId()) || islandAtTo.isMember(player.getUniqueId())) {
                plugin.getLogger().info(String.format("Player %s entered their own or a team island (%s). No welcome message needed.", player.getName(), islandAtTo.getIslandName()));
                lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
                return;
            }

            String welcomeMessage = welcomeManager.getWelcomeMessage(islandAtTo);
            if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMessage));
                plugin.getLogger().info(String.format("Welcome message displayed to %s for island %s (Owner: %s): '%s'",
                        player.getName(), islandAtTo.getIslandName(), islandAtTo.getOwnerUUID().toString(), welcomeMessage));
            } else {
                plugin.getLogger().info(String.format("Player %s entered island %s (Owner: %s), but no welcome message is set or it's empty.",
                        player.getName(), islandAtTo.getIslandName(), islandAtTo.getOwnerUUID().toString()));
            }
        } else if (currentIslandOwnerUUID == null && lastIslandOwnerUUID != null) {
            plugin.getLogger().info(String.format("Player %s left island region of owner %s and moved to wilderness at X:%d Y:%d Z:%d.",
                    player.getName(), lastIslandOwnerUUID.toString(), to.getBlockX(), to.getBlockY(), to.getBlockZ()));
        }


        // Update last known island owner
        if (currentIslandOwnerUUID != null) {
            if (!currentIslandOwnerUUID.equals(lastIslandOwnerUUID)) { // Only update if different to avoid redundant map puts
                lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
            }
        } else {
            if (lastPlayerIslandOwner.containsKey(player.getUniqueId())) { // Only remove if it was present
                lastPlayerIslandOwner.remove(player.getUniqueId());
            }
        }
    }
}