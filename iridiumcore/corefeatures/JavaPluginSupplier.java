package com.knemis.skyblock.skyblockcoreproject.iridiumcore.corefeatures;

import org.bukkit.plugin.java.JavaPlugin;

public interface JavaPluginSupplier<T> {
    T get(JavaPlugin javaPlugin);
}