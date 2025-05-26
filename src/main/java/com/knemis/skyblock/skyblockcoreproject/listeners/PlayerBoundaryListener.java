package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerBoundaryListener implements Listener {

    private final SkyBlockProject plugin;

    public PlayerBoundaryListener(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // If player hasn't moved between blocks, or is OP, or has bypass permission, or feature is disabled, return.
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        if (player.isOp() || player.hasPermission("skyblock.admin.bypassboundaries")) {
            return;
        }
        if (!plugin.getConfig().getBoolean("island.enforce-boundaries", true)) {
            return;
        }

        // If not in the Skyblock world, return.
        String skyblockWorldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        if (to.getWorld() == null || !to.getWorld().getName().equals(skyblockWorldName)) {
            return;
        }
        // Also check 'from' world, primarily for teleports, but good for move too.
        if (from.getWorld() == null || !from.getWorld().getName().equals(skyblockWorldName)) {
            return;
        }


        Island islandPlayerIsCurrentlyOn = plugin.getIslandDataHandler().getIslandAt(from);
        Island islandPlayerIsMovingTo = plugin.getIslandDataHandler().getIslandAt(to);

        if (islandPlayerIsCurrentlyOn != null) { // Player was on an island
            if (islandPlayerIsMovingTo == null) { // Moving to wilderness/unclaimed Skyblock space
                boolean isOwner = islandPlayerIsCurrentlyOn.getOwnerUUID().equals(player.getUniqueId());
                // Assuming Island.java has isMember or we check through IslandMemberManager via IslandDataHandler or Island object
                boolean isMember = islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId());


                if (isOwner || isMember) {
                    // Check if the island they are on is actually *their* island or one they are a member of.
                    // The getIslandAt might return an island they are just visiting but not part of.
                    Island playersOwnIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
                    boolean isTheirIslandStruct = false;
                    if (playersOwnIsland != null && playersOwnIsland.getRegionId().equals(islandPlayerIsCurrentlyOn.getRegionId())) {
                        isTheirIslandStruct = true;
                    } else {
                        // Check if they are a member of the island they are leaving
                        if (islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId())) {
                            isTheirIslandStruct = true;
                        }
                    }

                    if(isTheirIslandStruct){
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot leave your island's territory.");
                        // Optional: Teleport back. Teleporting to 'from' can sometimes glitch if 'from' is already partly outside.
                        // A safer bet might be the island's spawn point if available.
                        Location spawnPoint = islandPlayerIsCurrentlyOn.getSpawnPoint();
                        if (spawnPoint != null) {
                            player.teleport(spawnPoint);
                        } else {
                            player.teleport(from); // Fallback to 'from'
                        }
                    }
                }
            }
            // Else if (islandPlayerIsMovingTo != islandPlayerIsCurrentlyOn): Moving to another island.
            // WorldGuard entry flags on the destination island should handle this.
            // We are primarily concerned with leaving an owned/membered island into wilderness.
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (player.isOp() || player.hasPermission("skyblock.admin.bypassboundaries")) {
            return;
        }
        if (!plugin.getConfig().getBoolean("island.enforce-boundaries", true)) {
            return;
        }

        String skyblockWorldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");
        if (to.getWorld() == null || !to.getWorld().getName().equals(skyblockWorldName)) {
            // Teleporting out of skyblock world is not restricted by this listener.
            return;
        }
        // Only apply restrictions if the teleport is within the skyblock world.
        if (event.getFrom().getWorld() == null || !event.getFrom().getWorld().getName().equals(skyblockWorldName)) {
            return; // Not teleporting from within skyblock world
        }


        Island islandPlayerIsCurrentlyOn = plugin.getIslandDataHandler().getIslandAt(event.getFrom());
        Island destinationIsland = plugin.getIslandDataHandler().getIslandAt(to);

        if (islandPlayerIsCurrentlyOn != null && destinationIsland == null) {
            boolean isOwner = islandPlayerIsCurrentlyOn.getOwnerUUID().equals(player.getUniqueId());
            boolean isMember = islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId());

            if (isOwner || isMember) {
                // Check if the island they are on is actually *their* island or one they are a member of.
                Island playersOwnIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
                boolean isTheirIslandStruct = false;
                if (playersOwnIsland != null && playersOwnIsland.getRegionId().equals(islandPlayerIsCurrentlyOn.getRegionId())) {
                    isTheirIslandStruct = true;
                } else {
                    if (islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId())) {
                        isTheirIslandStruct = true;
                    }
                }

                if(isTheirIslandStruct){
                    // Certain teleport causes might be exempt, e.g., admin commands, or specific plugin teleports like /spawn
                    // For now, a simple check. More complex cause checking can be added.
                    PlayerTeleportEvent.TeleportCause cause = event.getCause();
                    if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                            cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT ||
                            cause == PlayerTeleportEvent.TeleportCause.PLUGIN || // Generic plugin teleport
                            cause == PlayerTeleportEvent.TeleportCause.COMMAND) { // Player-typed commands

                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot teleport outside your island's territory.");
                    }
                    // Teleports like SPECTATE or UNKNOWN might be allowed or need different handling.
                }
            }
        }
    }
}
