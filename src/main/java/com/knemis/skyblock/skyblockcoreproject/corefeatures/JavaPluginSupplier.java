package com.knemis.skyblock.skyblockcoreproject.corefeatures;

import org.bukkit.plugin.java.JavaPlugin;

public interface JavaPluginSupplier<T> {
    T get(JavaPlugin javaPlugin);
}