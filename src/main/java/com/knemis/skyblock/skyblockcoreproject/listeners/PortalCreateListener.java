package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
// No direct Island or manager interaction needed for this listener's core logic.

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateListener implements Listener {

    private final SkyBlockProject plugin;
    // Cached Skyblock End world instance
    private World skyblockEndWorld;

    public PortalCreateListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        // Initialize skyblockEndWorld during construction.
        this.skyblockEndWorld = getSkyblockDimensionWorld(World.Environment.THE_END);
        if (this.skyblockEndWorld == null) {
            plugin.getLogger().warning("[PortalCreateListener] Skyblock End world could not be determined at startup. Nether portal creation in End may not be correctly prevented until world loads.");
        }
    }

    private World getSkyblockDimensionWorld(World.Environment environment) {
        if (environment != World.Environment.THE_END) {
            return null;
        }

        // Get the main skyblock world name from IslandDataHandler if available, else from config, else default
        String mainSkyblockWorldName = "skyblock_world"; // Ultimate fallback
        if (plugin.getIslandDataHandler() != null && plugin.getIslandDataHandler().getSkyblockWorld() != null) {
            mainSkyblockWorldName = plugin.getIslandDataHandler().getSkyblockWorld().getName();
        } else {
            mainSkyblockWorldName = plugin.getConfig().getString("worlds.skyblock-world-name", mainSkyblockWorldName); // Assuming a config for main world too
        }

        String worldNameKey = "worlds.skyblock-end-name";
        String defaultEndWorldName = mainSkyblockWorldName + "_the_end";

        String configuredName = plugin.getConfig().getString(worldNameKey, defaultEndWorldName);
        World world = Bukkit.getWorld(configuredName);

        if (world == null) {
            // Log this potential issue, but allow constructor to complete.
            // The event handler will re-check or skip if world is still null.
             // plugin.getLogger().info("[PortalCreateListener] Attempted to find Skyblock End world: '" + configuredName + "'. Result: " + (world != null ? "Found" : "NOT FOUND"));
        }
        return world;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (this.skyblockEndWorld == null) {
            // Attempt to re-fetch if it was null during construction (e.g., world loaded after listener init)
            this.skyblockEndWorld = getSkyblockDimensionWorld(World.Environment.THE_END);
            if (this.skyblockEndWorld == null) {
                // plugin.getLogger().fine("[PortalCreateListener] Skyblock End world still not available. Cannot check portal creation.");
                return;
            }
        }

        if (event.getWorld().equals(this.skyblockEndWorld)) {
            // Check if any of the blocks forming the portal are Obsidian (indicating a Nether Portal).
            // PortalCreateEvent.getBlocks() returns a List<BlockState> of blocks that would form the frame.
            boolean isObsidianPortal = false;
            for (BlockState blockState : event.getBlocks()) {
                if (blockState.getType() == Material.OBSIDIAN) {
                    isObsidianPortal = true;
                    break;
                }
            }

            if (isObsidianPortal) {
                // Prevent creation of (typically Nether) portals made of obsidian in The End.
                event.setCancelled(true);
                // plugin.getLogger().fine("Prevented Obsidian-based portal creation in Skyblock End world: " + event.getReason());
            }
        }
    }
}
