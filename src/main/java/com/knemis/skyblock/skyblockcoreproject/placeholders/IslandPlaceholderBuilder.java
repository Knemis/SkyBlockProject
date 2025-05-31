package com.knemis.skyblock.skyblockcoreproject.placeholders;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island; // Project's Island class
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // To get island owner if needed
import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager; // For members
import com.knemis.skyblock.skyblockcoreproject.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.utils.TemporaryCache;

import com.cryptomorin.xseries.XMaterial; // Keep for block/spawner count placeholders, though they might be stubbed

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType; // Keep for spawner count placeholders
import org.bukkit.entity.Player;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class IslandPlaceholderBuilder implements PlaceholderBuilder<Island> {

    private final TemporaryCache<UUID, List<Placeholder>> cache = new TemporaryCache<>(); // Key by Island's Owner UUID
    private final List<Placeholder> defaultPlaceholders;
    private final SkyBlockProject plugin;
    private final String nullPlaceholderMsg;
    private final DateTimeFormatter dateTimeFormatter;

    public IslandPlaceholderBuilder(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.nullPlaceholderMsg = plugin.getConfig().getString("messages.null-placeholder", "N/A");
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(plugin.getConfig().getString("datetime-format", "yyyy-MM-dd HH:mm:ss"));
        this.defaultPlaceholders = initializeDefaultPlaceholders();
    }

    private String formatDouble(double value) {
        // Assuming SkyBlockProject might have a number formatter or use a default one.
        // For now, simple string formatting.
        return String.format("%.2f", value);
    }

    @Override
    public List<Placeholder> getPlaceholders(Island island) {
        if (island == null || island.getOwnerUUID() == null) {
            return defaultPlaceholders;
        }

        return cache.get(island.getOwnerUUID(), Duration.ofSeconds(1), () -> {
            List<Placeholder> placeholderList = new ArrayList<>();

            // Basic Island Info
            placeholderList.add(new Placeholder("island_name", island.getIslandName() != null ? island.getIslandName() : nullPlaceholderMsg));

            OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(island.getOwnerUUID());
            placeholderList.add(new Placeholder("island_owner", ownerPlayer.getName() != null ? ownerPlayer.getName() : nullPlaceholderMsg));

            // Ensure creationTimestamp is not null or default to a very old date or nullPlaceholderMsg
            long creationTimestamp = island.getCreationTimestamp(); // Assuming this method exists and returns long
            LocalDateTime createDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(creationTimestamp), ZoneId.systemDefault()
            );
            placeholderList.add(new Placeholder("island_create", createDateTime.format(dateTimeFormatter)));
            placeholderList.add(new Placeholder("island_description", island.getWelcomeMessage() != null ? island.getWelcomeMessage() : nullPlaceholderMsg)); // Assuming welcome message can be description

            // Value, Level, Experience - Adapt to SkyBlockProject's Island methods
            // Assuming getIslandWorth and getIslandLevel methods exist in SkyBlockProject.Island
            placeholderList.add(new Placeholder("island_value", formatDouble(island.getIslandWorth())));
            placeholderList.add(new Placeholder("island_level", String.valueOf(island.getIslandLevel())));
            placeholderList.add(new Placeholder("island_experience", nullPlaceholderMsg)); // Stubbed - SkyBlockProject.Island doesn't have direct experience field
            placeholderList.add(new Placeholder("island_experienceToLevelUp", nullPlaceholderMsg)); // Stubbed
            placeholderList.add(new Placeholder("island_experienceForNextLevel", nullPlaceholderMsg)); // Stubbed
            placeholderList.add(new Placeholder("island_value_rank", nullPlaceholderMsg)); // Stubbed - Rank system specific to Iridium
            placeholderList.add(new Placeholder("island_experience_rank", nullPlaceholderMsg)); // Stubbed

            // Members
            // IslandMemberManager memberManager = plugin.getIslandMemberManager(); // Not directly used here if Island object has members
            List<UUID> memberUUIDs = new ArrayList<>(island.getMembers());
            if (!memberUUIDs.contains(island.getOwnerUUID())) {
                memberUUIDs.add(island.getOwnerUUID());
            }

            List<String> onlineUsers = new ArrayList<>();
            List<String> offlineUsers = new ArrayList<>();

            for (UUID memberUUID : memberUUIDs) {
                if (memberUUID == null) continue; // Safety check
                OfflinePlayer memberOfflinePlayer = Bukkit.getOfflinePlayer(memberUUID);
                if (memberOfflinePlayer.isOnline()) {
                    onlineUsers.add(memberOfflinePlayer.getName() != null ? memberOfflinePlayer.getName() : memberUUID.toString());
                } else {
                    offlineUsers.add(memberOfflinePlayer.getName() != null ? memberOfflinePlayer.getName() : memberUUID.toString());
                }
            }
            placeholderList.add(new Placeholder("island_members_online", () -> String.join(", ", onlineUsers)));
            placeholderList.add(new Placeholder("island_members_online_count", () -> String.valueOf(onlineUsers.size())));
            placeholderList.add(new Placeholder("island_members_offline", () -> String.join(", ", offlineUsers)));
            placeholderList.add(new Placeholder("island_members_offline_count", () -> String.valueOf(offlineUsers.size())));
            placeholderList.add(new Placeholder("island_members_count", () -> String.valueOf(memberUUIDs.size())));

            placeholderList.add(new Placeholder("island_visitors", nullPlaceholderMsg));
            placeholderList.add(new Placeholder("island_visitors_amount", "0"));

            placeholderList.add(new Placeholder("island_enhancement_size_active", nullPlaceholderMsg));
            placeholderList.add(new Placeholder("island_enhancement_size_level", nullPlaceholderMsg));

            placeholderList.add(new Placeholder("island_bank_crystals", nullPlaceholderMsg));

            for (XMaterial xMaterial : XMaterial.values()) {
                if (xMaterial.isSupported()) {
                    placeholderList.add(new Placeholder("island_" + xMaterial.name().toLowerCase() + "_amount", nullPlaceholderMsg));
                }
            }
            for (EntityType entityType : EntityType.values()) {
                if (entityType.isAlive() || entityType.name().equals("ITEM_FRAME") || entityType.name().equals("PAINTING")) {
                     placeholderList.add(new Placeholder("island_" + entityType.name().toLowerCase() + "_amount", nullPlaceholderMsg));
                }
            }
            return placeholderList;
        });
    }

    private List<Placeholder> initializeDefaultPlaceholders() {
        List<Placeholder> phList = new ArrayList<>(Arrays.asList(
                new Placeholder("island_name", nullPlaceholderMsg),
                new Placeholder("island_owner", nullPlaceholderMsg),
                new Placeholder("island_description", nullPlaceholderMsg),
                new Placeholder("island_create", nullPlaceholderMsg),
                new Placeholder("island_value", nullPlaceholderMsg),
                new Placeholder("island_level", nullPlaceholderMsg),
                new Placeholder("island_experience", nullPlaceholderMsg),
                new Placeholder("island_value_rank", nullPlaceholderMsg),
                new Placeholder("island_experience_rank", nullPlaceholderMsg),
                new Placeholder("island_members_online", nullPlaceholderMsg),
                new Placeholder("island_members_online_count", "0"),
                new Placeholder("island_members_offline", nullPlaceholderMsg),
                new Placeholder("island_members_offline_count", "0"),
                new Placeholder("island_members_count", "0"),
                new Placeholder("island_visitors", nullPlaceholderMsg),
                new Placeholder("island_visitors_amount", "0")
        ));
        phList.add(new Placeholder("island_enhancement_size_active", nullPlaceholderMsg));
        phList.add(new Placeholder("island_enhancement_size_level", nullPlaceholderMsg));
        phList.add(new Placeholder("island_bank_crystals", nullPlaceholderMsg));
        phList.add(new Placeholder("island_" + XMaterial.STONE.name().toLowerCase() + "_amount", nullPlaceholderMsg));
        phList.add(new Placeholder("island_" + EntityType.PIG.name().toLowerCase() + "_amount", nullPlaceholderMsg));
        return Collections.unmodifiableList(phList); // Make default list unmodifiable
    }

    @Override
    public List<Placeholder> getPlaceholders(Optional<Island> optionalIsland) {
        return optionalIsland.map(this::getPlaceholders).orElse(defaultPlaceholders);
    }
}
