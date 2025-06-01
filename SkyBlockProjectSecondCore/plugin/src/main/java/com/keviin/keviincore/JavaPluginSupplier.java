package com.keviin.keviincore;

import org.bukkit.plugin.java.JavaPlugin;

public interface JavaPluginSupplier<T> {
    T get(JavaPlugin javaPlugin);
}