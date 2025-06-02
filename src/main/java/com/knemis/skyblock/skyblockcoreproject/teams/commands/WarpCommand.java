package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class WarpCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public WarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<TeamWarp> teamWarp = SkyBlockProjectTeams.getTeamManager().getTeamWarp(team, args[0]);
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownWarp
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (teamWarp.get().getPassword() != null) {
            if (args.length != 2 || !teamWarp.get().getPassword().equals(args[1])) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().incorrectPassword
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
        }
        
        if (SkyBlockProjectTeams.getTeamManager().teleport(player, teamWarp.get().getLocation(), team)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teleportingWarp
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%name%", teamWarp.get().getName())
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        List<TeamWarp> teamWarps = SkyBlockProjectTeams.getTeamManager().getTeamWarps(team);
        return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
    }
}
