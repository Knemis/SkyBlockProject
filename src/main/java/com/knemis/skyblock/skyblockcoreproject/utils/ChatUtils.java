package com.knemis.skyblock.skyblockcoreproject.utils;

import org.bukkit.ChatColor;

public final class ChatUtils {

    private ChatUtils() {
        // Utility class
    }

    public static String translateAlternateColorCodes(String textToTranslate) {
        if (textToTranslate == null) {
            return ""; // Return empty string for null input
        }
        return ChatColor.translateAlternateColorCodes('&', textToTranslate);
    }

    public static String stripColor(String textToStrip) {
        if (textToStrip == null) {
            return ""; // Return empty string for null input
        }
        return ChatColor.stripColor(textToStrip);
    }
}
