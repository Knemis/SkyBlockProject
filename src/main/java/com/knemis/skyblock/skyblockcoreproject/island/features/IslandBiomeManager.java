package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.IOException;
import java.util.ArrayList; // Added import
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
            player.sendMessage(Component.text("You don't have an island or island world whose biome you can change.", NamedTextColor.RED));
            return false;
        }

        Biome targetBiome;
        try {
            targetBiome = Registry.BIOME.get(NamespacedKey.minecraft(biomeName.toLowerCase(java.util.Locale.ROOT)));
            if (targetBiome == null) throw new IllegalArgumentException("Invalid biome name provided.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid biome name: " + biomeName, NamedTextColor.RED));
            player.sendMessage(Component.text("To see available biomes: /island biome list", NamedTextColor.YELLOW));
            return false;
        }

        World world = island.getWorld();
        CuboidRegion islandTerritory;
        try {
            islandTerritory = islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation());
        } catch (IOException e) {
            player.sendMessage(Component.text("An error occurred while calculating island boundaries: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().log(Level.SEVERE, "IO Error while calculating island boundaries (setIslandBiome): " + e.getMessage(), e);
            return false;
        }

        if (islandTerritory == null) {
            player.sendMessage(Component.text("Island region not found.", NamedTextColor.RED));
            return false;
        }

        player.sendMessage(Component.text("Island biome is being set to '" + targetBiome.getKey().getKey() + "'... This process may take some time.", NamedTextColor.YELLOW));

        try {
            BlockVector3 min = islandTerritory.getMinimumPoint();
            BlockVector3 max = islandTerritory.getMaximumPoint();

            for (int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++) {
                for (int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) {
                        chunk.load(false);
                    }
                    for (int x = chunk.getX() * 16; x < chunk.getX() * 16 + 16; x++) {
                        for (int z = chunk.getZ() * 16; z < chunk.getZ() * 16 + 16; z++) {
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
                    .forEach(pOnline -> pOnline.sendMessage(Component.text("Island biome changed! To see the changes fully, you can leave and re-enter the area or log back in.", NamedTextColor.AQUA)));


            island.setCurrentBiome(targetBiome.getKey().getKey()); // Use getKey().getKey()
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();

            plugin.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") set their island's (" + island.getOwnerUUID() + ") biome to " + targetBiome.getKey().getKey() + ".");
            player.sendMessage(Component.text("Island biome successfully set to ", NamedTextColor.GREEN)
                    .append(Component.text(targetBiome.getKey().getKey(), NamedTextColor.AQUA))
                    .append(Component.text("!", NamedTextColor.GREEN)));
            return true;

        } catch (Exception e) {
            player.sendMessage(Component.text("An unexpected error occurred while setting the biome. Please check the console.", NamedTextColor.RED));
            plugin.getLogger().log(Level.SEVERE, "Error during setIslandBiome: " + e.getMessage(), e);
            return false;
        }
    }

    public String getIslandBiome(Island island) {
        if (island != null && island.getCurrentBiome() != null) {
            return island.getCurrentBiome(); // This already stores the string key
        }
        if (island != null && island.getBaseLocation() != null && island.getBaseLocation().getWorld() != null) {
            return island.getBaseLocation().getBlock().getBiome().getKey().getKey(); // Use getKey().getKey()
        }
        return "Unknown";
    }

    public void sendAvailableBiomes(Player player) {
        List<String> availableBiomes = Registry.BIOME.stream() // Use Registry.BIOME.stream()
                .filter(b -> !b.getKey().getNamespace().equals(NamespacedKey.MINECRAFT_NAMESPACE) || !b.getKey().getKey().equals("custom")) // Filter out minecraft:custom
                .filter(b -> !b.getKey().getKey().startsWith("the_void")) // Filter out the_void variants
                .map(b -> b.getKey().getKey()) // Use getKey().getKey()
                .sorted()
                .collect(Collectors.toList());
        player.sendMessage(Component.text("--- Available Biomes ---", NamedTextColor.GOLD));

        // Joining with components for proper coloring
        List<Component> biomeComponents = new ArrayList<>();
        for (int i = 0; i < availableBiomes.size(); i++) {
            biomeComponents.add(Component.text(availableBiomes.get(i), NamedTextColor.YELLOW));
            if (i < availableBiomes.size() - 1) {
                biomeComponents.add(Component.text(", ", NamedTextColor.GRAY));
            }
        }
        player.sendMessage(Component.join(Component.empty(), biomeComponents));
        player.sendMessage(Component.text("Note: Some biomes may differ based on server configuration or game version.", NamedTextColor.GRAY));
    }
}