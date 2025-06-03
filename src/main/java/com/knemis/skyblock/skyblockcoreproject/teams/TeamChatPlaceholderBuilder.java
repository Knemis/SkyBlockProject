package com.knemis.skyblock.skyblockcoreproject.teams;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public interface TeamChatPlaceholderBuilder {
    List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders(AsyncPlayerChatEvent event, Player player); // TODO: Replace with actual Placeholder class
}
