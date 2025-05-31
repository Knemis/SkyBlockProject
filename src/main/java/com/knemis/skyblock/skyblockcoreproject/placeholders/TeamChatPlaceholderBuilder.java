package com.knemis.skyblock.skyblockcoreproject.placeholders; // Adjusted package

// Assuming com.iridium.iridiumcore.utils.Placeholder is a dependency.
// If not, this will need to be adapted or a placeholder class created.
import com.knemis.skyblock.skyblockcoreproject.utils.Placeholder;
// Assuming com.iridium.iridiumteams.TeamChatPlaceholderBuilder is an interface dependency.
// If not, this will need to be adapted or a placeholder interface created.
// For now, we will keep this, and if it causes issues, we can remove the 'implements' clause temporarily.

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;

// The 'implements com.iridium.iridiumteams.TeamChatPlaceholderBuilder' may cause issues
// if the IridiumTeams framework is not present. This will be addressed in compilation/dependency resolution step.
public class TeamChatPlaceholderBuilder implements com.iridium.iridiumteams.TeamChatPlaceholderBuilder {

    @Override
    public List<Placeholder> getPlaceholders(AsyncPlayerChatEvent event, Player player) {
        // Ensure event.getMessage() is not null if possible, though Placeholder constructor should handle it.
        String message = event.getMessage() != null ? event.getMessage() : "";
        return Arrays.asList(
                new Placeholder("player", event.getPlayer().getName()),
                new Placeholder("message", message)
        );
    }
}
