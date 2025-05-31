package com.knemis.skyblock.skyblockcoreproject.placeholders;

// Simple record to hold rank display information.
// The original UserRank might have more fields (e.g., icon), add them if needed by placeholders.
public record UserRankDisplay(String name, String icon) {
    public UserRankDisplay(String name) {
        this(name, ""); // Default empty icon
    }
}
