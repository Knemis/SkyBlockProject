package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager; // For island home

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnEventListener implements Listener {

    private final SkyBlockProject plugin;
    private final boolean spawnOnIslandEnabled;

    public PlayerRespawnEventListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.spawnOnIslandEnabled = plugin.getConfig().getBoolean("settings.respawn.on-island-if-member", true);
    }

    private Location getGlobalSpawnLocation(Player player) {
        String worldName = plugin.getConfig().getString("settings.respawn.global-spawn.world");
        World world = null;
        if (worldName != null && !worldName.isEmpty()) {
            world = Bukkit.getWorld(worldName);
        }

        if (world == null) { // Fallback to first loaded world if specified world not found or not configured
            plugin.getLogger().warning("Global respawn world '" + worldName + "' not found or not configured. Defaulting to main world spawn.");
            world = Bukkit.getServer().getWorlds().get(0);
        }

        // Check if specific coordinates are configured
        if (plugin.getConfig().isSet("settings.respawn.global-spawn.x") &&
            plugin.getConfig().isSet("settings.respawn.global-spawn.y") &&
            plugin.getConfig().isSet("settings.respawn.global-spawn.z")) {

            double x = plugin.getConfig().getDouble("settings.respawn.global-spawn.x");
            double y = plugin.getConfig().getDouble("settings.respawn.global-spawn.y");
            double z = plugin.getConfig().getDouble("settings.respawn.global-spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("settings.respawn.global-spawn.yaw", 0.0); // Default yaw
            float pitch = (float) plugin.getConfig().getDouble("settings.respawn.global-spawn.pitch", 0.0); // Default pitch
            return new Location(world, x, y, z, yaw, pitch);
        }

        // Fallback to the world's actual spawn location if coordinates not set
        return world.getSpawnLocation();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        IslandTeleportManager islandTeleportManager = plugin.getIslandTeleportManager();

        if (islandDataHandler == null || islandTeleportManager == null) {
            plugin.getLogger().severe("IslandDataHandler or IslandTeleportManager is null! PlayerRespawnEventListener cannot function correctly.");
            event.setRespawnLocation(getGlobalSpawnLocation(player)); // Fallback to global spawn
            return;
        }

        Island playerIsland = islandDataHandler.getIslandByOwner(player.getUniqueId());
        boolean isBedOrAnchorSpawn = event.isBedSpawn() || event.isAnchorSpawn();

        // Scenario 1: Player has an island, spawnOnIsland is enabled, and it's not a bed/anchor spawn.
        if (spawnOnIslandEnabled && playerIsland != null && !isBedOrAnchorSpawn) {
            Location islandSpawnLoc = islandTeleportManager.getSafeIslandSpawnLocation(playerIsland);
            if (islandSpawnLoc != null) {
                event.setRespawnLocation(islandSpawnLoc);
                return;
            } else {
                // plugin.getLogger().warning("Could not find safe spawn for " + player.getName() + " on their island " + playerIsland.getRegionId() + ". Using global spawn.");
                event.setRespawnLocation(getGlobalSpawnLocation(player));
                return;
            }
        }

        // Scenario 2: Bed/Anchor spawn, or player has no island, or spawnOnIsland is disabled.
        // Check the nature of the current respawnLocation.
        Location currentRespawnLocation = event.getRespawnLocation();
        Island islandAtRespawnLocation = islandDataHandler.getIslandAt(currentRespawnLocation);

        if (islandAtRespawnLocation != null) {
            // The bed/anchor/default spawn point is within an island's territory.
            boolean isOwnIsland = playerIsland != null && playerIsland.getRegionId().equals(islandAtRespawnLocation.getRegionId());

            if (!isOwnIsland) {
                // Player is trying to respawn (likely via bed/anchor) on an island that isn't theirs.
                // This should be prevented unless specific "allow visitor respawn" flags exist (future feature).
                // plugin.getLogger().info(player.getName() + " attempted to respawn at a bed/anchor on island " + islandAtRespawnLocation.getRegionId() + " (not their own). Redirecting to global spawn.");
                event.setRespawnLocation(getGlobalSpawnLocation(player));
            }
            // If it IS their own island and a bed/anchor spawn, vanilla behavior is fine (respawnLocation is already correct).
        } else if (!isBedOrAnchorSpawn) {
            // Not a bed/anchor spawn, and not on any island territory (e.g., vanilla world spawn in a non-island area,
            // or player has no island and spawnOnIsland was true but failed to find island spawn).
            // Ensure they go to the configured global spawn.
            event.setRespawnLocation(getGlobalSpawnLocation(player));
        }
        // If it's a bed/anchor spawn AND not on any island territory (e.g. bed in wilderness), vanilla behavior is fine.
    }
}
