package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager; // For potential safe Y finding
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerPortalListener implements Listener {

    private final SkyBlockProject plugin;
    private final int netherUnlockLevel;
    private final int endUnlockLevel;
    private final String netherLockedMessage;
    private final String endLockedMessage;
    private final String netherDisabledMessage;
    private final String endDisabledMessage;
    private final String noSafeLocationMessage;
    private final String prefix;

    public PlayerPortalListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfig().getString("messages.prefix", "&b[SkyBlock] &r");

        this.netherUnlockLevel = plugin.getConfig().getInt("settings.portals.nether.min-level", 10);
        this.endUnlockLevel = plugin.getConfig().getInt("settings.portals.end.min-level", 20);

        this.netherLockedMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.portals.nether-locked", "&cYour island must be level %level% to use Nether portals.").replace("%level%", String.valueOf(netherUnlockLevel)));
        this.endLockedMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.portals.end-locked", "&cYour island must be level %level% to use End portals.").replace("%level%", String.valueOf(endUnlockLevel)));
        this.netherDisabledMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.portals.nether-disabled", "&cNether islands are disabled on this server."));
        this.endDisabledMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.portals.end-disabled", "&cEnd islands are disabled on this server."));
        this.noSafeLocationMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.teleport.no-safe-location", "&cCould not find a safe location for portal travel."));
    }

    private World getSkyblockDimensionWorld(World.Environment environment) {
        String worldNameKey;
        String defaultWorldName = null; // Set to null initially
        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        World mainSkyblockWorld = (islandDataHandler != null) ? islandDataHandler.getSkyblockWorld() : null;
        String mainWorldName = (mainSkyblockWorld != null) ? mainSkyblockWorld.getName() : "skyblock_world"; // Default if main world is somehow null

        switch (environment) {
            case NETHER:
                worldNameKey = "worlds.skyblock-nether-name";
                defaultWorldName = mainWorldName + "_nether";
                break;
            case THE_END:
                worldNameKey = "worlds.skyblock-end-name";
                defaultWorldName = mainWorldName + "_the_end";
                break;
            default: // NORMAL or CUSTOM
                return mainSkyblockWorld; // Return the main skyblock world
        }
        String configuredName = plugin.getConfig().getString(worldNameKey, defaultWorldName);
        return Bukkit.getWorld(configuredName);
    }

    private Location getTargetIslandLocationInDimension(Island island, World targetDimension) {
        Location islandBase = island.getBaseLocation();
        double targetY;

        if (targetDimension.getEnvironment() == World.Environment.NETHER) {
            targetY = plugin.getConfig().getDouble("settings.portals.nether.target-y", 64.0);
        } else if (targetDimension.getEnvironment() == World.Environment.THE_END) {
            targetY = plugin.getConfig().getDouble("settings.portals.end.target-y", 100.0);
        } else { // Overworld or custom - use island's original Y, or a default safe Y for overworld.
            targetY = islandBase.getY(); // Or a configured "overworld-portal-return-y"
        }

        Location preliminaryTarget = new Location(targetDimension, islandBase.getX(), targetY, islandBase.getZ(), islandBase.getYaw(), islandBase.getPitch());

        // For The End, attempt a slightly safer Y. This is still very basic.
        if (targetDimension.getEnvironment() == World.Environment.THE_END) {
            // Look for a 2-block air gap above a solid block, starting from targetY downwards.
            for (int y = (int) targetY; y > targetDimension.getMinHeight() + 1; y--) {
                Location checkLoc = new Location(targetDimension, preliminaryTarget.getX(), y, preliminaryTarget.getZ());
                if (checkLoc.getBlock().getType().isSolid() &&
                    checkLoc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                    checkLoc.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
                    return checkLoc.clone().add(0, 1, 0); // Return location of the first air block
                }
            }
            // If no such spot, return preliminaryTarget, hoping for the best or let event be cancelled later
        }
        // For Nether and Overworld, the preliminary Y is often fine, or specific platform generation is expected.
        return preliminaryTarget;
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        if (islandDataHandler == null) return;

        Island island = islandDataHandler.getIslandAt(event.getFrom());

        if (island == null) return;

        TeleportCause cause = event.getCause();
        Location targetLocation = null;

        if (cause == TeleportCause.NETHER_PORTAL) {
            if (island.getIslandLevel() < netherUnlockLevel) {
                player.sendMessage(netherLockedMessage);
                event.setCancelled(true);
                return;
            }
            World currentWorld = event.getFrom().getWorld();
            World netherDimWorld = getSkyblockDimensionWorld(World.Environment.NETHER);
            World overworldDimWorld = getSkyblockDimensionWorld(World.Environment.NORMAL);

            if (netherDimWorld == null && (currentWorld.equals(overworldDimWorld) || currentWorld.getEnvironment() == World.Environment.NORMAL)) {
                player.sendMessage(netherDisabledMessage); // Nether is disabled, and trying to go there
                event.setCancelled(true);
                return;
            }
             if (overworldDimWorld == null && currentWorld.equals(netherDimWorld)) {
                event.setCancelled(true); // Cannot determine return world
                return;
            }


            World destinationWorld = currentWorld.equals(netherDimWorld) ? overworldDimWorld : netherDimWorld;
            if (destinationWorld == null) { // Should only happen if nether is disabled and trying to go there
                 event.setCancelled(true); return;
            }
            targetLocation = getTargetIslandLocationInDimension(island, destinationWorld);

        } else if (cause == TeleportCause.END_PORTAL) {
            if (island.getIslandLevel() < endUnlockLevel) {
                player.sendMessage(endLockedMessage);
                event.setCancelled(true);
                return;
            }
            World currentWorld = event.getFrom().getWorld();
            World endDimWorld = getSkyblockDimensionWorld(World.Environment.THE_END);
            World overworldDimWorld = getSkyblockDimensionWorld(World.Environment.NORMAL);

            if (endDimWorld == null && currentWorld.getEnvironment() == World.Environment.NORMAL) {
                player.sendMessage(endDisabledMessage);
                event.setCancelled(true);
                return;
            }
             if (overworldDimWorld == null && currentWorld.equals(endDimWorld)) {
                 event.setCancelled(true); // Cannot determine return world
                 return;
            }


            World destinationWorld = (currentWorld.getEnvironment() == World.Environment.NORMAL) ? endDimWorld : overworldDimWorld;
             if (destinationWorld == null) {
                 event.setCancelled(true); return;
            }
            targetLocation = getTargetIslandLocationInDimension(island, destinationWorld);

            // If going to The End, and the target location is the preliminary one (safe Y not found by simple check)
            // we might want to cancel if it's deemed too unsafe.
            if (destinationWorld.getEnvironment() == World.Environment.THE_END) {
                // The simple safe Y check in getTargetIslandLocationInDimension might return the original Y if no ideal spot found.
                // A more robust check here or there is needed. For now, if targetY is still the default high Y:
                if (targetLocation.getY() == plugin.getConfig().getDouble("settings.portals.end.target-y", 100.0)) {
                     // This implies the simple downward scan didn't find a better spot.
                     // This could still be unsafe (e.g. spawning high on a small platform).
                     // A more robust IslandTeleportManager.findSafeSpot(location) would be better.
                     // For now, we proceed, but this is a known simplification.
                }
            }
        }

        if (targetLocation != null) {
            event.setTo(targetLocation);
            // For End Portals, try to prevent vanilla portal platform creation at destination if on Paper
            if (cause == TeleportCause.END_PORTAL && targetLocation.getWorld().getEnvironment() == World.Environment.THE_END) {
                try {
                    event.setCanCreatePortal(false);
                } catch (NoSuchMethodError e) {
                    // Paper API not available, or older version. Ignore.
                }
            }
        } else if (event.getCause() == TeleportCause.NETHER_PORTAL || event.getCause() == TeleportCause.END_PORTAL) {
            // If targetLocation is null but it was a portal we should handle, means a world was disabled/not found
            // Messages for this are handled above, ensure event is cancelled.
            event.setCancelled(true);
        }
    }
}
