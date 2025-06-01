package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager; // For teleporting to island home
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
// No ItemStack or LostItems needed as that feature is stubbed for now.

public class PlayerMoveListener implements Listener {

    private final SkyBlockProject plugin;
    private final String prefix; // Store prefix for reuse

    public PlayerMoveListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfig().getString("messages.prefix", "&b[SkyBlock] &r"); // Example prefix
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null) return; // Should not happen with PlayerMoveEvent

        // Optimization: only process if actually moved to a new block or fell significantly
        // This helps reduce how often getIslandAt is called.
        boolean significantMove = (from.getBlockX() != to.getBlockX() ||
                                   from.getBlockZ() != to.getBlockZ() ||
                                   (from.getBlockY() - to.getBlockY()) > 0.1); // Y check for falling

        if (!significantMove && from.getBlockY() == to.getBlockY()) { // if only Y changed but not falling, ignore small jitters
             if (Math.abs(from.getY() - to.getY()) < 0.05) return; // Ignore very small y changes if not block change
        }


        // TODO: Island Border Display (similar to PlayerJoinListener)
        // This is where logic to update player's view of island borders would go if needed.
        // Example:
        // Island currentIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
        // Island toIsland = plugin.getIslandDataHandler().getIslandAt(to);
        // Island fromIsland = plugin.getIslandDataHandler().getIslandAt(from);
        // if (toIsland != fromIsland) { // Player crossed an island boundary
        //    if (toIsland != null) plugin.getIslandDisplayManager().showBorder(player, toIsland);
        //    else if (fromIsland != null) plugin.getIslandDisplayManager().hideBorder(player, fromIsland);
        // }

        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        if (islandDataHandler == null) return;

        // Only check for void fall if Y coordinate has changed downwards or block has changed
        if (to.getY() < from.getY() || from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            double minHeight;
            try {
                minHeight = player.getWorld().getMinHeight();
            } catch (NoSuchMethodError e) {
                minHeight = 0; // Fallback for older Bukkit versions
            }

            if (to.getY() < minHeight) {
                Island islandAtFallLocation = islandDataHandler.getIslandAt(from); // Check island at location they fell FROM

                if (islandAtFallLocation != null) {
                    // Player fell into the void while on an island.
                    // The original code had a complex Void Enhancement system.
                    // This is now stubbed: always teleport to the island's spawn (the one they fell from).
                    // Item loss logic is omitted.

                    IslandTeleportManager teleportManager = plugin.getIslandTeleportManager();
                    if (teleportManager != null) {
                        // Teleport player to the spawn of the island they were on when they fell.
                        boolean teleported = teleportManager.teleportPlayerToIslandSpawn(player, islandAtFallLocation);

                        if (teleported) {
                            String voidTeleportMessage = plugin.getConfig().getString("messages.void-teleport.generic", "&7You fell into the void and were teleported back to the island spawn.");
                            player.sendMessage(ChatUtils.colorize(this.prefix + voidTeleportMessage));
                        } else {
                            // Fallback if specific island spawn teleport fails
                            player.teleport(player.getWorld().getSpawnLocation());
                            String voidTeleportFailMessage = plugin.getConfig().getString("messages.void-teleport.fail-fallback", "&cCould not find a safe spot on the island, teleported to world spawn.");
                            player.sendMessage(ChatUtils.colorize(this.prefix + voidTeleportFailMessage));
                        }
                        // Prevent further movement processing for this event if teleported.
                        // Setting 'to' to the new location might be an option if event isn't cancelled.
                        // However, teleporting should make this event's 'to' irrelevant.
                        return;
                    }
                }
                // If not on any island when falling into void, vanilla behavior occurs.
            }
        }
    }
}
