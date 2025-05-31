package com.knemis.skyblock.skyblockcoreproject.schematics; // Adjusted package

// WorldEdit API imports
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit; // Main WorldEdit class
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter; // Adapter
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
// FAWE specific capabilities might require FAWE API if used, snippet uses standard WE
// import com.sk89q.worldedit.extension.platform.Capability;
// import com.sk89q.worldedit.extension.platform.Platform;


// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin; // For scheduler

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level; // For Logger

// Attempt to get plugin instance for scheduler
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;


public class FastAsyncWorldEditPaster implements SchematicPaster { // Renamed and implements interface

    private static final HashMap<File, ClipboardFormat> cachedClipboardFormat = new HashMap<>();
    // The original snippet had a static Object mutex = new Object();
    // This implies that FAWE operations might need external synchronization if run in parallel threads.
    // For now, retaining it as it was in the snippet.
    private static final Object faweMutex = new Object();

    // Original isWorking() method. Adapted to use Bukkit.getLogger().
    // Note: FAWE compatibility checks can be more complex. This is a basic check.
    public static boolean isFaweWorking() {
        try {
            // Check if WorldEdit (which FAWE typically shades or provides) is available
            if (WorldEdit.getInstance() != null && WorldEdit.getInstance().getPlatformManager() != null) {
                // FAWE often identifies itself in the version or through specific classes.
                // A simple check: Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")
                // However, relying on WorldEdit API calls to succeed is often the practical test.
                // The original snippet used Capability.WORLD_EDITING, which might be FAWE-specific or newer WE.
                // For now, a basic WE check + assuming FAWE is the WorldEdit provider if this paster is chosen.
                // Bukkit.getLogger().info("[FastAsyncWorldEditPaster] WorldEdit (assumed FAWE) instance found.");
                return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
            }
            Bukkit.getLogger().warning("[FastAsyncWorldEditPaster] WorldEdit instance or PlatformManager is null (FAWE check).");
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[FastAsyncWorldEditPaster] Error during FAWE check. Ensure FAWE is installed and compatible.", t);
        }
        return false;
    }

    @Override
    public void paste(File file, Location location, Boolean ignoreAirBlock, CompletableFuture<Void> completableFuture) {
        try {
            ClipboardFormat format = cachedClipboardFormat.get(file);
            if (format == null) {
                format = ClipboardFormats.findByFile(file);
                if (format != null) {
                    cachedClipboardFormat.put(file, format);
                } else {
                     completableFuture.completeExceptionally(new IOException("Could not determine clipboard format for file: " + file.getName()));
                    return;
                }
            }

            Clipboard clipboard;
            try (FileInputStream fis = new FileInputStream(file);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }

            // Centering logic
            int width = clipboard.getDimensions().getBlockX();
            int height = clipboard.getDimensions().getBlockY();
            int length = clipboard.getDimensions().getBlockZ();

            Location pasteLocation = location.clone(); // Mutate a clone
            pasteLocation.subtract(width / 2.0, height / 2.0, length / 2.0);

            clipboard.setOrigin(clipboard.getRegion().getMinimumPoint());

            // The original snippet created a new Thread for FAWE operations.
            // This is common with FAWE to offload to its async mechanisms.
            final Clipboard finalClipboard = clipboard; // Effectively final for thread
            // final ClipboardFormat finalFormat = format; // Not used in thread in this version

            Thread faweThread = new Thread(() -> {
                synchronized (faweMutex) { // Synchronize based on original snippet's mutex
                    Plugin pluginInstance = null;
                    try {
                         // Attempt to get plugin instance for scheduler task
                        pluginInstance = SkyBlockProject.getPlugin();
                        if (pluginInstance == null && SkyBlockProject.getInstance() != null) { // Fallback if getPlugin() is not the static accessor
                            pluginInstance = SkyBlockProject.getInstance();
                        }

                        if (pluginInstance == null) {
                            Bukkit.getLogger().severe("[FastAsyncWorldEditPaster] SkyBlockProject plugin instance is null, cannot complete future for " + file.getName());
                            completableFuture.completeExceptionally(new IllegalStateException("Plugin instance not available for task scheduling."));
                            return;
                        }

                        final Plugin finalPluginInstance = pluginInstance; // effectively final for lambda

                        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pasteLocation.getWorld()))) {
                            Operation operation = new ClipboardHolder(finalClipboard)
                                    .createPaste(editSession)
                                    .to(BlockVector3.at(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ()))
                                    .copyEntities(true) // Assuming true
                                    .ignoreAirBlocks(ignoreAirBlock)
                                    .build();
                            Operations.complete(operation);
                            // editSession.commit() or relying on try-with-resources close behavior
                            // FAWE often handles commits efficiently.

                            // Schedule CompletableFuture completion back on main thread via Bukkit scheduler
                            Bukkit.getScheduler().runTask(finalPluginInstance, () -> {
                                completableFuture.complete(null);
                            });

                        } catch (WorldEditException e) {
                            Bukkit.getLogger().log(Level.SEVERE, "[FastAsyncWorldEditPaster] WorldEditException during FAWE paste " + file.getName(), e);
                            // Schedule exceptional completion back to main thread if possible, or complete directly
                             Bukkit.getScheduler().runTask(finalPluginInstance, () -> {
                                completableFuture.completeExceptionally(e);
                            });
                        } catch (Exception e) { // Catch any other unexpected errors in the thread
                            Bukkit.getLogger().log(Level.SEVERE, "[FastAsyncWorldEditPaster] Unexpected error in FAWE paste thread " + file.getName(), e);
                            Bukkit.getScheduler().runTask(finalPluginInstance, () -> {
                                completableFuture.completeExceptionally(e);
                            });
                        }
                    } catch (Exception e) { // Catch errors like plugin instance being null before scheduling
                        Bukkit.getLogger().log(Level.SEVERE, "[FastAsyncWorldEditPaster] Error in FAWE thread setup for " + file.getName(), e);
                        completableFuture.completeExceptionally(e); // Complete exceptionally from this thread if scheduler can't be reached
                    }
                }
            });
            faweThread.setName("FAWE-Paster-Thread-" + file.getName());
            faweThread.start();

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[FastAsyncWorldEditPaster] IOException preparing FAWE paste " + file.getName(), e);
            completableFuture.completeExceptionally(e);
        }
    }

    @Override
    public void clearCache() {
        cachedClipboardFormat.clear();
        Bukkit.getLogger().info("[FastAsyncWorldEditPaster] Clipboard format cache cleared.");
    }
}
