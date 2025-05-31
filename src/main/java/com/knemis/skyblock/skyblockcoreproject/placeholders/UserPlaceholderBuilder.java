package com.knemis.skyblock.skyblockcoreproject.placeholders;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.utils.TemporaryCache;
// Assuming IslandMemberManager might provide rank info, or another system.
// For now, this import might not be used if rank logic is simplified.
// import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager;


import org.bukkit.entity.Player; // Using Bukkit Player

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserPlaceholderBuilder implements PlaceholderBuilder<Player> { // Changed to Player
    private final TemporaryCache<UUID, List<Placeholder>> cache = new TemporaryCache<>(); // Key by UUID for player
    private final List<Placeholder> defaultPlaceholders;

    private SkyBlockProject plugin;

    public UserPlaceholderBuilder(SkyBlockProject plugin) {
        this.plugin = plugin;
        // Define default placeholders using plugin's messages if available, or defaults
        String nullPlaceholderMsg = plugin.getConfig().getString("messages.null-placeholder", "N/A");
        this.defaultPlaceholders = Arrays.asList(
                new Placeholder("player_rank", nullPlaceholderMsg),
                new Placeholder("player_name", nullPlaceholderMsg),
                new Placeholder("player_join", nullPlaceholderMsg)
        );
    }

    private UserRankDisplay getPlayerRank(Player player) {
        // This is a placeholder for rank logic.
        // TODO: Integrate with skyblockcoreproject's actual rank system if it exists.
        // For now, let's check if the player is an island owner using IslandMemberManager if accessible,
        // or just return a default.
        // This part is highly dependent on how ranks are managed in skyblockcoreproject.
        // Example: if (plugin.getIslandMemberManager().isOwner(player)) return new UserRankDisplay("Owner");
        return new UserRankDisplay(plugin.getConfig().getString("placeholders.default-rank-name", "Member"));
    }

    private String getDateTimeFormat() {
         return plugin.getConfig().getString("datetime-format", "yyyy-MM-dd HH:mm:ss");
    }


    @Override
    public List<Placeholder> getPlaceholders(Player player) { // Changed to Player
        if (player == null) return defaultPlaceholders;

        return cache.get(player.getUniqueId(), Duration.ofSeconds(1), () -> {
            UserRankDisplay rankDisplay = getPlayerRank(player);
            String rankName = rankDisplay != null ? rankDisplay.name() : plugin.getConfig().getString("messages.null-placeholder", "N/A");

            LocalDateTime joinDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(player.getFirstPlayed()), ZoneId.systemDefault()
            );

            return Arrays.asList(
                    new Placeholder("player_rank", rankName),
                    new Placeholder("player_name", player.getName()),
                    new Placeholder("player_join", joinDateTime.format(DateTimeFormatter.ofPattern(getDateTimeFormat())))
            );
        });
    }

    @Override
    public List<Placeholder> getPlaceholders(Optional<Player> optionalPlayer) { // Changed to Player
        return optionalPlayer.map(this::getPlaceholders).orElse(defaultPlaceholders);
    }
}
