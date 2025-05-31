package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
// No direct Island or manager interaction in the refactored version's active code,
// but keeping plugin instance for potential future use or if sendIslandBorder was internal.

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {

    private final SkyBlockProject plugin;

    public PlayerTeleportListener(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Schedule the task for the next tick to ensure the player has fully arrived at the new location.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = event.getPlayer();
            if (player == null || !player.isOnline()) { // Player might have logged off during teleport or very quickly
                return;
            }

            // TODO: Implement or verify how island borders are visually sent to players in SkyBlockProject.
            // This functionality might reside in IslandLifecycleManager (related to their region)
            // or a dedicated IslandBorderManager / IslandDisplayManager.
            // The original Iridium code called: IridiumSkyblock.getInstance().getTeamManager().sendIslandBorder(player);
            // Example placeholder call:
            // if (plugin.getIslandDisplayManager() != null) { // Assuming such a manager exists
            //     plugin.getIslandDisplayManager().updatePlayerIslandBorderView(player);
            // }
            // plugin.getLogger().info("PlayerTeleportListener: Would attempt to update island border view for " + player.getName() + " after teleport.");
        });
    }
}
