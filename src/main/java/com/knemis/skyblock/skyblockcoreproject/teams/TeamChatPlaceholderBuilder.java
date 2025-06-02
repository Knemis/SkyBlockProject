package com.knemis.skyblock.skyblockcoreproject.teams;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public interface TeamChatPlaceholderBuilder {
    List<Placeholder> getPlaceholders(AsyncPlayerChatEvent event, Player player);
}
