package com.knemis.skyblock.skyblockcoreproject.schematics; // Adjusted package

// Assuming XMaterial is available (e.g. from a library like XSeries)
import com.cryptomorin.xseries.XMaterial;
// Assuming Coordinate is the one we added to utils
import com.knemis.skyblock.skyblockcoreproject.utils.Coordinate;
// JNBT dependency
import org.jnbt.*;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin; // For scheduler

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

// Attempt to get plugin instance for scheduler and config
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;

public class SchematicAsyncPaster implements SchematicPaster { // Renamed and implements interface

    private static final Map<File, SchematicData> schematicCache = new HashMap<>();

    // Method to get plugin instance, assuming SkyBlockProject has a static getter.
    // Returns null if not available, callers must handle.
    private Plugin getPluginInstance() {
        try {
            // Try common static accessor names
            java.lang.reflect.Method getPluginMethod = SkyBlockProject.class.getMethod("getPlugin");
            return (Plugin) getPluginMethod.invoke(null);
        } catch (Exception e1) {
            try {
                java.lang.reflect.Method getInstanceMethod = SkyBlockProject.class.getMethod("getInstance");
                return (Plugin) getInstanceMethod.invoke(null);
            } catch (Exception e2) {
                // Log only once or use a flag to prevent spam if called frequently and fails
                // For now, simple log:
                // Bukkit.getLogger().log(Level.WARNING, "[SchematicAsyncPaster] Could not get SkyBlockProject plugin instance via reflection. Scheduler and config access might fail.", e2);
                return null; // Return null if both attempts fail
            }
        }
    }

    // Helper to get config values safely
    private int getConfigInt(String path, int defaultValue) {
        Plugin plugin = getPluginInstance();
        if (plugin != null && plugin.getConfig().isSet(path)) { // Use isSet for robustness
            return plugin.getConfig().getInt(path);
        }
        return defaultValue;
    }


    @Override
    public void paste(File file, Location location, Boolean ignoreAirBlock, CompletableFuture<Void> completableFuture) {
        SchematicData schematicData = getSchematicData(file, completableFuture);
        if (schematicData == null) {
            // getSchematicData already completes future exceptionally if error occurs
            return;
        }

        ListIterator<Coordinate> coordinates = getCoordinates(schematicData);

        // Adapt config access
        int delay = getConfigInt("schematic-paster.async.delay-in-ticks", 1);
        int limitPerTick = getConfigInt("schematic-paster.async.limit-per-tick", 100);


        // Centering logic
        short length = schematicData.length;
        short width = schematicData.width;
        short height = schematicData.height;

        Location pasteLocation = location.clone(); // Mutate a clone
        pasteLocation.subtract(width / 2.0, height / 2.0, length / 2.0);


        Plugin pluginInstance = getPluginInstance();
        if (pluginInstance == null) {
            Bukkit.getLogger().severe("[SchematicAsyncPaster] Cannot schedule async paste task: SkyBlockProject plugin instance is null.");
            completableFuture.completeExceptionally(new IllegalStateException("Plugin instance not available for async schematic pasting."));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    int remaining = limitPerTick;

                    while (remaining > 0 && coordinates.hasNext()) {
                        Coordinate coordinate = coordinates.next();
                        int x = coordinate.x;
                        int y = coordinate.y;
                        int z = coordinate.z;

                        int index = y * width * length + z * width + x;
                        if (index < 0 || index >= schematicData.blockdata.length) {
                            // This can happen if schematic dimensions are inconsistent with blockdata length
                            // Bukkit.getLogger().warning("[SchematicAsyncPaster] Calculated index " + index + " is out of bounds for blockdata array (length " + schematicData.blockdata.length + "). Skipping block at " + x + "," + y + "," + z + " for schematic " + file.getName());
                            continue;
                        }

                        // Palette processing - original snippet's direct mapping approach is used.
                        // A typical Sponge schematic would use blockdata[index] as an ID into a sequential palette array/list.
                        // The snippet implies blockdata[index] is a value to be found in the palette map's *values*.
                        // This is unusual but maintained as per snippet.
                        String blockStateString = null;
                        byte currentBlockDataVal = schematicData.blockdata[index];

                        for (Map.Entry<String, Tag> entry : schematicData.palette.entrySet()) {
                            if (entry.getValue() instanceof IntTag) {
                                if (((IntTag) entry.getValue()).getValue() == currentBlockDataVal) {
                                    blockStateString = entry.getKey();
                                    break;
                                }
                            }
                        }

                        if (blockStateString == null) {
                           // Bukkit.getLogger().warning("[SchematicAsyncPaster] No block state found in palette for data value: " + currentBlockDataVal + " at index " + index);
                            continue;
                        }

                        BlockData data = Bukkit.createBlockData(blockStateString);
                        if (data.getMaterial() == Material.AIR && ignoreAirBlock) {
                            // We don't decrement 'remaining' for ignored air blocks to ensure we process 'limitPerTick' non-air blocks.
                            // However, if we want to count air blocks towards the limit, then decrement here.
                            // For now, assume limit is for actual block placements.
                            continue;
                        }

                        Block block = new Location(pasteLocation.getWorld(), x + pasteLocation.getX(), y + pasteLocation.getY(), z + pasteLocation.getZ()).getBlock();
                        block.setBlockData(data, false); // false for no physics updates during paste
                        remaining--;
                    }

                    if (!coordinates.hasNext()) { // All blocks placed
                        if (schematicData.tileEntities != null) {
                            for (Tag tag : schematicData.tileEntities) {
                                if (!(tag instanceof CompoundTag)) continue;
                                CompoundTag t = (CompoundTag) tag;
                                Map<String, Tag> tags = t.getValue();

                                if (!tags.containsKey("Pos") || !tags.containsKey("Id")) continue;

                                int[] pos = SchematicData.getChildTag(tags, "Pos", IntArrayTag.class).getValue();
                                int teX = pos[0];
                                int teY = pos[1];
                                int teZ = pos[2];

                                Block block = new Location(pasteLocation.getWorld(), teX + pasteLocation.getX(), teY + pasteLocation.getY(), teZ + pasteLocation.getZ()).getBlock();
                                String id = SchematicData.getChildTag(tags, "Id", StringTag.class).getValue().toLowerCase().replace("minecraft:", "");

                                if (id.equalsIgnoreCase("chest") && block.getState() instanceof Chest) {
                                    Chest chest = (Chest) block.getState();
                                    if (tags.containsKey("Items") && tags.get("Items") instanceof ListTag) { // Check type
                                        List<Tag> items = SchematicData.getChildTag(tags, "Items", ListTag.class).getValue();
                                        chest.getBlockInventory().clear(); // Clear existing items before adding new ones
                                        for (Tag item : items) {
                                            if (!(item instanceof CompoundTag)) continue;
                                            Map<String, Tag> itemtag = ((CompoundTag) item).getValue();
                                            if (!itemtag.containsKey("Slot") || !itemtag.containsKey("id") || !itemtag.containsKey("Count")) continue;

                                            byte slot = SchematicData.getChildTag(itemtag, "Slot", ByteTag.class).getValue();
                                            String itemName = (SchematicData.getChildTag(itemtag, "id", StringTag.class).getValue()).toLowerCase().replace("minecraft:", "").replace("reeds", "sugar_cane");
                                            byte amount = SchematicData.getChildTag(itemtag, "Count", ByteTag.class).getValue();

                                            Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(itemName.toUpperCase());
                                            if (optionalXMaterial.isPresent() && optionalXMaterial.get().parseMaterial() != null) {
                                                ItemStack itemStack = optionalXMaterial.get().parseItem();
                                                if (itemStack != null) {
                                                    itemStack.setAmount(amount);
                                                    if (slot >= 0 && slot < chest.getBlockInventory().getSize()) {
                                                        chest.getBlockInventory().setItem(slot, itemStack);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        completableFuture.complete(null);
                        this.cancel();
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[SchematicAsyncPaster] Error during async paste task for " + file.getName(), e);
                    completableFuture.completeExceptionally(e);
                    this.cancel();
                }
            }
        }.runTaskTimer(pluginInstance, delay, delay);
    }

    private SchematicData getSchematicData(File file, CompletableFuture<Void> futureForException) {
        try {
            SchematicData schematicData = schematicCache.get(file);
            if (schematicData == null) {
                schematicData = SchematicData.loadSchematic(file);
                schematicCache.put(file, schematicData);
            }
            return schematicData;
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[SchematicAsyncPaster] IOException loading schematic " + file.getName(), e);
            futureForException.completeExceptionally(e);
            return null;
        } catch (IllegalArgumentException e) { // Catch errors from getChildTag in SchematicData.loadSchematic
            Bukkit.getLogger().log(Level.SEVERE, "[SchematicAsyncPaster] Error parsing NBT data for schematic " + file.getName() + ": " + e.getMessage(), e);
            futureForException.completeExceptionally(e);
            return null;
        }
    }

    private ListIterator<Coordinate> getCoordinates(SchematicData schematicData) {
        short length = schematicData.length;
        short width = schematicData.width;
        short height = schematicData.height;

        List<Coordinate> coordinates = new ArrayList<>(width * height * length);
        for (short y = 0; y < height; ++y) {
            for (short x = 0; x < width; ++x) {
                for (short z = 0; z < length; ++z) {
                    coordinates.add(new Coordinate(x, y, z));
                }
            }
        }
        return coordinates.listIterator();
    }

    @Override
    public void clearCache() {
        schematicCache.clear();
        Bukkit.getLogger().info("[SchematicAsyncPaster] Schematic data cache cleared.");
    }
}
