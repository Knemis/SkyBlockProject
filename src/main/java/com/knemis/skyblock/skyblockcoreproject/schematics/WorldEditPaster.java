package com.knemis.skyblock.skyblockcoreproject.schematics; // Adjusted package

// Imports for WorldEdit API
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter; // Adapter for Bukkit <-> WorldEdit
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.WorldEdit; // Main WorldEdit class access

// Bukkit imports
import org.bukkit.Bukkit; // For Bukkit.getLogger()
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level; // For Logger

// Assuming SkyBlockProject class exists for plugin instance, though not used directly in this adapted version.
// import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;

public class WorldEditPaster implements SchematicPaster { // Renamed and implements interface

    private static final HashMap<File, ClipboardFormat> cachedClipboardFormat = new HashMap<>();

    // The original isWorking() method relied on IridiumSkyblock.getInstance().getLogger().
    // This static utility method might be better placed in a WorldEdit utility class for the project,
    // or called by the plugin's main class. For now, adapting it to use Bukkit.getLogger().
    public static boolean isWorldEditWorking() {
        try {
            // The original code used:
            // com.sk89q.worldedit.WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
            // This Capability enum is not directly available in all WE versions or might be part of an extension.
            // A simpler check is to see if WorldEdit can be accessed.
            if (WorldEdit.getInstance() != null && WorldEdit.getInstance().getPlatformManager() != null) {
                // Further checks could be added here if a specific capability is required.
                // For now, if WE instance is available, assume it's 'working' enough for basic operations.
                // Bukkit.getLogger().info("[WorldEditPaster] WorldEdit instance found.");
                return true;
            }
             Bukkit.getLogger().warning("[WorldEditPaster] WorldEdit instance or PlatformManager is null.");
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[WorldEditPaster] WorldEdit threw an error during basic check. Ensure it's properly installed and compatible.", t);
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
                    throw new IOException("Could not determine clipboard format for file: " + file.getName());
                }
            }

            Clipboard clipboard;
            try (FileInputStream fis = new FileInputStream(file);
                 ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }

            // Centering logic from original snippet
            int width = clipboard.getDimensions().getBlockX();
            int height = clipboard.getDimensions().getBlockY();
            int length = clipboard.getDimensions().getBlockZ();

            // Ensure location is mutable for subtraction
            Location pasteLocation = location.clone();
            pasteLocation.subtract(width / 2.0, height / 2.0, length / 2.0);

            // Adjust clipboard origin
            clipboard.setOrigin(clipboard.getRegion().getMinimumPoint());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pasteLocation.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ()))
                        .copyEntities(true) // Assuming true is a sensible default
                        .ignoreAirBlocks(ignoreAirBlock)
                        .build();
                Operations.complete(operation);
                // The original had Operations.complete(editSession.commit());
                // Depending on WorldEdit version and practices, commit might be handled by complete(operation)
                // or might need to be explicit. For modern WE, editSession.close() handles commit.
                // Let's stick to what the existing IslandLifecycleManager uses or what's common for the WE version.
                // The existing IslandLifecycleManager in skyblockcoreproject doesn't show an explicit commit after Operations.complete.
                // It just uses the try-with-resources on EditSession.
            }
            completableFuture.complete(null);
        } catch (IOException | WorldEditException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[WorldEditPaster] Error pasting schematic " + file.getName(), e);
            completableFuture.completeExceptionally(e); // Signal failure
        }
    }

    @Override
    public void clearCache() {
        cachedClipboardFormat.clear();
        Bukkit.getLogger().info("[WorldEditPaster] Clipboard format cache cleared.");
    }
}
