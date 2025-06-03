package com.knemis.skyblock.skyblockcoreproject.core.keviincore;

import org.bukkit.plugin.java.JavaPlugin;

public interface JavaPluginSupplier<T> {
    T get(JavaPlugin javaPlugin);
}