package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class WarpCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public WarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<TeamWarp> teamWarp = skyblockTeams.getTeamManager().getTeamWarp(team, args[0]); // TODO: Ensure TeamManager is functional
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().unknownWarp
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (teamWarp.get().getPassword() != null) {
            if (args.length != 2 || !teamWarp.get().getPassword().equals(args[1])) {
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().incorrectPassword
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return false;
            }
        }
        
        if (skyblockTeams.getTeamManager().teleport(player, teamWarp.get().getLocation(), team)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teleportingWarp
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%name%", teamWarp.get().getName())
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        List<TeamWarp> teamWarps = skyblockTeams.getTeamManager().getTeamWarps(team); // TODO: Ensure TeamManager is functional
        return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
    }
}
