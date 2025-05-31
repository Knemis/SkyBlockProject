package com.knemis.skyblock.skyblockcoreproject.utils;

import java.util.function.Supplier;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholder {
    private final String key;
    private final Supplier<String> valueSupplier;

    public Placeholder(String key, String value) {
        this.key = key;
        this.valueSupplier = () -> value; // Store direct value as a supplier
    }

    public Placeholder(String key, Supplier<String> valueSupplier) {
        this.key = key;
        this.valueSupplier = valueSupplier;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        // Ensure supplier is not null before calling get(), though constructor should prevent this.
        return valueSupplier != null ? valueSupplier.get() : "";
    }

    /**
     * A utility method to replace placeholders in a string.
     * Placeholders are typically formatted as %key%.
     * @param text The text containing placeholders.
     * @param placeholders A list of Placeholder objects.
     * @return The text with placeholders replaced.
     */
    public static String process(String text, List<Placeholder> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }
        String result = text;
        for (Placeholder placeholder : placeholders) {
            if (placeholder != null && placeholder.getKey() != null) {
                // Simple replacement, assuming %key% format
                // More sophisticated pattern matching could be used if needed for complex keys
                String placeholderKey = "%" + placeholder.getKey() + "%";
                String value = placeholder.getValue();
                if (value == null) value = ""; // Avoid "null" string literals
                result = result.replace(placeholderKey, value);
            }
        }
        return result;
    }

    /**
     * A utility method to replace placeholders in a list of strings.
     * @param lines The list of strings containing placeholders.
     * @param placeholders A list of Placeholder objects.
     * @return A new list of strings with placeholders replaced.
     */
    public static List<String> process(List<String> lines, List<Placeholder> placeholders) {
        if (lines == null || placeholders == null || placeholders.isEmpty()) {
            return lines;
        }
        List<String> result = new java.util.ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(process(line, placeholders));
        }
        return result;
    }
}
