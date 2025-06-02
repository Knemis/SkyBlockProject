package com.keviin.keviinteams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.ChatType;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PlayerChatListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        U user = keviinTeams.getUserManager().getUser(event.getPlayer());
        Optional<T> yourTeam = keviinTeams.getTeamManager().getTeamViaID(user.getTeamID());
        Optional<ChatType> chatType = keviinTeams.getChatTypes().stream()
                .filter(type -> type.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(user.getChatType())))
                .findFirst();
        if (!yourTeam.isPresent() || !chatType.isPresent()) return;
        List<Player> players = chatType.get().getPlayerChat().getPlayers(event.getPlayer().getPlayer());
        if (players == null) return;
        for (Player player : players) {
            if(player == null) return;
            player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(keviinTeams.getMessages().chatFormat, keviinTeams.getTeamChatPlaceholderBuilder().getPlaceholders(event, player))));
        }
        event.getRecipients().clear();
    }

}
