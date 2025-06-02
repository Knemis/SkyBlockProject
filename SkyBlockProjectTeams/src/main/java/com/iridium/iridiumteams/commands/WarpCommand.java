package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamWarp;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class WarpCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public WarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<TeamWarp> teamWarp = keviinTeams.getTeamManager().getTeamWarp(team, args[0]);
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().unknownWarp
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (teamWarp.get().getPassword() != null) {
            if (args.length != 2 || !teamWarp.get().getPassword().equals(args[1])) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().incorrectPassword
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
        }
        
        if (keviinTeams.getTeamManager().teleport(player, teamWarp.get().getLocation(), team)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teleportingWarp
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%name%", teamWarp.get().getName())
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        List<TeamWarp> teamWarps = keviinTeams.getTeamManager().getTeamWarps(team);
        return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
    }
}
