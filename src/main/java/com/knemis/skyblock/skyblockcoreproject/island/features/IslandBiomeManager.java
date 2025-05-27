package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandBiomeManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandLifecycleManager islandLifecycleManager;

    public IslandBiomeManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandLifecycleManager islandLifecycleManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
    }

    public boolean setIslandBiome(Player player, Island island, String biomeName) {
        if (island == null || island.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "You don't have an island or island world whose biome you can change.");
            return false;
        }

        Biome targetBiome;
        try {
            targetBiome = Biome.valueOf(biomeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid biome name: " + biomeName);
            player.sendMessage(ChatColor.YELLOW + "To see available biomes: /island biome list");
            return false;
        }

        World world = island.getWorld();
        CuboidRegion islandTerritory;
        try {
            islandTerritory = islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while calculating island boundaries: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "IO Error while calculating island boundaries (setIslandBiome): " + e.getMessage(), e);
            return false;
        }

        if (islandTerritory == null) {
            player.sendMessage(ChatColor.RED + "Island region not found.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Island biome is being set to '" + targetBiome.name() + "'... This process may take some time.");

        try {
            BlockVector3 min = islandTerritory.getMinimumPoint();
            BlockVector3 max = islandTerritory.getMaximumPoint();

            for (int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++) {
                for (int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) {
                        // WARNING FIX: The return value of chunk.load() method was used (or at least assigned to a variable).
                        @SuppressWarnings("unused") // If the "loaded" variable will not be used, this warning can be suppressed with this annotation.
                        boolean loaded = chunk.load(false);
                    }
                    for (int x = chunk.getX() * 16; x < chunk.getX() * 16 + 16; x++) {
                        for (int z = chunk.getZ() * 16; z < chunk.getZ() * 16 + 16; z++) {
                            // ERROR FIX: contains method called with BlockVector3.at().
                            if (islandTerritory.contains(BlockVector3.at(x, min.getBlockY(), z))) {
                                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                                    if (y >= world.getMinHeight() && y < world.getMaxHeight()) {
                                        world.setBiome(x, y, z, targetBiome);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            world.getPlayers().stream()
                    .filter(p -> islandTerritory.contains(BlockVector3.at(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())))
                    .forEach(pOnline -> pOnline.sendMessage(ChatColor.AQUA + "Island biome changed! To see the changes fully, you can leave and re-enter the area or log back in."));


            island.setCurrentBiome(targetBiome.name());
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();

            plugin.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") set their island's (" + island.getOwnerUUID() + ") biome to " + targetBiome.name() + ".");
            player.sendMessage(ChatColor.GREEN + "Island biome successfully set to " + ChatColor.AQUA + targetBiome.name() + ChatColor.GREEN + "!");
            return true;

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An unexpected error occurred while setting the biome. Please check the console.");
            plugin.getLogger().log(Level.SEVERE, "Error during setIslandBiome: " + e.getMessage(), e);
            return false;
        }
    }

    public String getIslandBiome(Island island) {
        if (island != null && island.getCurrentBiome() != null) {
            return island.getCurrentBiome();
        }
        if (island != null && island.getBaseLocation() != null && island.getBaseLocation().getWorld() != null) {
            return island.getBaseLocation().getBlock().getBiome().name();
        }
        return "Unknown";
    }

    public void sendAvailableBiomes(Player player) {
        List<String> availableBiomes = Arrays.stream(Biome.values())
                .filter(b -> b != Biome.CUSTOM && !b.name().startsWith("THE_VOID"))
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        player.sendMessage(ChatColor.GOLD + "--- Available Biomes ---");
        player.sendMessage(ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, availableBiomes));
        player.sendMessage(ChatColor.GRAY + "Note: Some biomes may differ based on server configuration or game version.");
    }
}