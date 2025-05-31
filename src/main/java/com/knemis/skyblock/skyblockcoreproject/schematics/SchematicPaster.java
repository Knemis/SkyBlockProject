package com.knemis.skyblock.skyblockcoreproject.schematics; // Adjusted package

import org.bukkit.Location; // Bukkit Location

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface SchematicPaster {
    void paste(File file, Location location, Boolean ignoreAirBlock, CompletableFuture<Void> completableFuture);

    void clearCache();
}
