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

        if (to == null) { // Should not happen, but good practice
            plugin.getLogger().warning(String.format("PlayerBoundaryListener: PlayerMoveEvent for %s had a null 'to' location.", player.getName()));
            return;
        }

        // If player hasn't moved between blocks
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) {
            return;
        }

        String worldName = to.getWorld().getName();
        String skyblockWorldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");

        // If not in the Skyblock world, or feature is disabled, or player has bypass, return.
        if (!worldName.equals(skyblockWorldName)) {
            return;
        }
        if (player.isOp() || player.hasPermission("skyblock.admin.bypassboundaries")) {
            plugin.getLogger().finest(String.format("PlayerBoundaryListener: Player %s (UUID: %s) has bypass permission for boundary checks.", player.getName(), player.getUniqueId()));
            return;
        }
        if (!plugin.getConfig().getBoolean("island.enforce-boundaries", true)) {
            plugin.getLogger().finest("PlayerBoundaryListener: Island boundary enforcement is disabled in config.");
            return;
        }

        plugin.getLogger().info(String.format("PlayerBoundaryListener: Active for PlayerMoveEvent of player %s (UUID: %s) from (X:%.1f,Y:%.1f,Z:%.1f) to (X:%.1f,Y:%.1f,Z:%.1f) in world %s.",
                player.getName(), player.getUniqueId(), from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), worldName));

        // Also check 'from' world, primarily for teleports, but good for move too.
        if (from.getWorld() == null || !from.getWorld().getName().equals(skyblockWorldName)) {
            plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) moved into skyblock world from another world. No restriction on entry from here.", player.getName(), player.getUniqueId()));
            return;
        }

        Island islandPlayerIsCurrentlyOn = plugin.getIslandDataHandler().getIslandAt(from);
        Island islandPlayerIsMovingTo = plugin.getIslandDataHandler().getIslandAt(to);

        if (islandPlayerIsCurrentlyOn != null) {
            if (islandPlayerIsMovingTo == null) { // Moving to wilderness from an island
                // boolean isOwner = islandPlayerIsCurrentlyOn.getOwnerUUID().equals(player.getUniqueId()); // Redundant if using playersOwnIsland check
                // boolean isMember = islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId()); // Redundant

                Island playersOwnIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
                boolean isTheirIslandStruct = false;
                if (playersOwnIsland != null && playersOwnIsland.getRegionId().equals(islandPlayerIsCurrentlyOn.getRegionId())) {
                    isTheirIslandStruct = true;
                } else {
                    // Check if they are a member of the island they are leaving, only if playersOwnIsland didn't match
                    // This handles cases where a player might be a member of multiple islands.
                    // However, the primary check should be against their *owned* island first.
                    // For simplicity, if they own an island, we assume they are trying to leave *that* island's structure if they are on it.
                    // If they don't own an island, but are member of the one they are on:
                    if (playersOwnIsland == null && islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId())) {
                        isTheirIslandStruct = true; // They don't own any island, but are a member of the current one.
                    }
                }

                if(isTheirIslandStruct){
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot leave your island's territory.");
                    Location spawnPoint = islandPlayerIsCurrentlyOn.getSpawnPoint();
                    Location teleportTarget = (spawnPoint != null) ? spawnPoint : from; // Fallback to 'from'
                    player.teleport(teleportTarget);
                    plugin.getLogger().warning(String.format("PlayerBoundaryListener: Player %s (UUID: %s) DENIED leaving their island (%s) territory at (X:%.1f,Y:%.1f,Z:%.1f). Teleported to (X:%.1f,Y:%.1f,Z:%.1f).",
                            player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(),
                            to.getX(), to.getY(), to.getZ(), teleportTarget.getX(), teleportTarget.getY(), teleportTarget.getZ()));
                } else {
                    plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) allowed to leave island region %s (not their own/membered island structure they are primarily associated with) to wilderness at (X:%.1f,Y:%.1f,Z:%.1f).",
                            player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(), to.getX(), to.getY(), to.getZ()));
                }
            } else if (!islandPlayerIsMovingTo.getRegionId().equals(islandPlayerIsCurrentlyOn.getRegionId())) {
                plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) moving from island %s to island %s. Boundary check deferred to WorldGuard/other listeners.",
                        player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(), islandPlayerIsMovingTo.getRegionId()));
            }
            // Else: moving within the same island region, no action needed.
        } else if (islandPlayerIsMovingTo != null) {
            plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) moving from Wilderness into island %s. Boundary check deferred to WorldGuard/other listeners.",
                    player.getName(), player.getUniqueId(), islandPlayerIsMovingTo.getRegionId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (to == null || from == null || to.getWorld() == null || from.getWorld() == null) {
            plugin.getLogger().warning(String.format("PlayerBoundaryListener: PlayerTeleportEvent for %s had a null location/world. From: %s, To: %s", player.getName(), from, to));
            return;
        }

        String toWorldName = to.getWorld().getName();
        String skyblockWorldName = plugin.getConfig().getString("skyblock-world-name", "skyblock_world");

        if (!toWorldName.equals(skyblockWorldName) || !from.getWorld().getName().equals(skyblockWorldName)) {
            // Teleporting to/from a non-skyblock world is not restricted by this listener's core logic for leaving *own* island.
            plugin.getLogger().finest(String.format("PlayerBoundaryListener: Player %s (UUID: %s) teleport event not within skyblock world or across worlds. To: %s, From: %s. Cause: %s. No action.",
                    player.getName(), player.getUniqueId(), toWorldName, from.getWorld().getName(), cause.name()));
            return;
        }

        if (player.isOp() || player.hasPermission("skyblock.admin.bypassboundaries")) {
            plugin.getLogger().finest(String.format("PlayerBoundaryListener: Player %s (UUID: %s) has bypass permission for teleport boundary checks. Cause: %s.", player.getName(), player.getUniqueId(), cause.name()));
            return;
        }
        if (!plugin.getConfig().getBoolean("island.enforce-boundaries", true)) {
            plugin.getLogger().finest("PlayerBoundaryListener: Island boundary enforcement for teleports is disabled in config.");
            return;
        }

        plugin.getLogger().info(String.format("PlayerBoundaryListener: Active for %s of player %s (UUID: %s) from (X:%.1f,Y:%.1f,Z:%.1f) to (X:%.1f,Y:%.1f,Z:%.1f) in world %s. Cause: %s",
                event.getEventName(), player.getName(), player.getUniqueId(),
                from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(),
                toWorldName, cause.name()));

        Island islandPlayerIsCurrentlyOn = plugin.getIslandDataHandler().getIslandAt(from);
        Island destinationIsland = plugin.getIslandDataHandler().getIslandAt(to);

        if (islandPlayerIsCurrentlyOn != null && destinationIsland == null) { // Teleporting from an island to wilderness
            Island playersOwnIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
            boolean isTheirIslandStruct = false;
            if (playersOwnIsland != null && playersOwnIsland.getRegionId().equals(islandPlayerIsCurrentlyOn.getRegionId())) {
                isTheirIslandStruct = true;
            } else {
                if (playersOwnIsland == null && islandPlayerIsCurrentlyOn.getMembers().contains(player.getUniqueId())) {
                    isTheirIslandStruct = true;
                }
            }

            if(isTheirIslandStruct){
                if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                        cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT ||
                        // Consider if PLUGIN and COMMAND should always be blocked. Some admin commands might use these.
                        // For now, if it's their island, these common player-initiated teleports are blocked.
                        (cause == PlayerTeleportEvent.TeleportCause.PLUGIN && !player.hasPermission("skyblock.admin.teleport.bypasswilderness")) ||
                        (cause == PlayerTeleportEvent.TeleportCause.COMMAND && !player.hasPermission("skyblock.admin.teleport.bypasswilderness"))) {

                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot teleport outside your island's territory using " + cause.name().replace("_", " ").toLowerCase() + ".");
                    plugin.getLogger().warning(String.format("PlayerBoundaryListener: Player %s (UUID: %s) DENIED teleporting from their island (%s) to wilderness at (X:%.1f,Y:%.1f,Z:%.1f). Cause: %s.",
                            player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(),
                            to.getX(), to.getY(), to.getZ(), cause.name()));
                } else {
                    plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) teleporting from their island (%s) to wilderness with cause %s. Allowed due to cause or permissions.",
                            player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(), cause.name()));
                }
            } else {
                plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) allowed teleport from island region %s (not their own/membered island structure) to wilderness at (X:%.1f,Y:%.1f,Z:%.1f). Cause: %s.",
                        player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(), to.getX(), to.getY(), to.getZ(), cause.name()));
            }
        } else if (islandPlayerIsCurrentlyOn != null && destinationIsland != null && !islandPlayerIsCurrentlyOn.getRegionId().equals(destinationIsland.getRegionId())) {
            plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) teleporting from island %s to island %s. Cause: %s. Boundary check deferred to WorldGuard/other listeners.",
                    player.getName(), player.getUniqueId(), islandPlayerIsCurrentlyOn.getRegionId(), destinationIsland.getRegionId(), cause.name()));
        } else if (islandPlayerIsCurrentlyOn == null && destinationIsland != null) {
            plugin.getLogger().info(String.format("PlayerBoundaryListener: Player %s (UUID: %s) teleporting from Wilderness into island %s. Cause: %s. Boundary check deferred to WorldGuard/other listeners.",
                    player.getName(), player.getUniqueId(), destinationIsland.getRegionId(), cause.name()));
        }
    }
}