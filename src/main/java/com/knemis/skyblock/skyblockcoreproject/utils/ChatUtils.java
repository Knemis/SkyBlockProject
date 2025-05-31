package com.knemis.skyblock.skyblockcoreproject.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public final class ChatUtils {

    private ChatUtils() {
        // Utility class
    }

    /**
     * Deserializes a string containing legacy color codes (using '&') into a Component.
     *
     * @param textToTranslate The string to translate.
     * @return A Component with colors applied. Returns an empty component if input is null.
     */
    public static Component deserializeLegacyColorCodes(String textToTranslate) {
        if (textToTranslate == null || textToTranslate.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(textToTranslate);
    }

    /**
     * Strips all legacy color codes (using '&') from a string.
     *
     * @param textToStrip The string to strip colors from.
     * @return A plain string with all color codes removed. Returns an empty string if input is null.
     */
    public static String stripColor(String textToStrip) {
        if (textToStrip == null || textToStrip.isEmpty()) {
            return "";
        }
        // First, deserialize to a component, then serialize to plain text
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(textToStrip);
        return PlainComponentSerializer.plain().serialize(component);
    }
}
