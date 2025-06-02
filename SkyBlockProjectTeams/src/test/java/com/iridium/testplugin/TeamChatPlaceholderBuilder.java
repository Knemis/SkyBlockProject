package com.keviin.testplugin;

import com.keviin.keviincore.utils.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;

public class TeamChatPlaceholderBuilder implements com.keviin.keviinteams.TeamChatPlaceholderBuilder {
    @Override
    public List<Placeholder> getPlaceholders(AsyncPlayerChatEvent event, Player player) {
        return Arrays.asList(
                new Placeholder("player", event.getPlayer().getName()),
                new Placeholder("message", event.getMessage())
        );
    }
}
