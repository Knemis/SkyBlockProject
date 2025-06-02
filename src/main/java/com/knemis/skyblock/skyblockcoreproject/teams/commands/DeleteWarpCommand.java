package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;


import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DeleteWarpCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public DeleteWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        Optional<TeamWarp> teamWarp = keviinTeams.getTeamManager().getTeamWarp(team, args[0]);
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().unknownWarp
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        keviinTeams.getTeamManager().deleteWarp(teamWarp.get());
        keviinTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member ->
                member.sendMessage(StringUtils.color(keviinTeams.getMessages().deletedWarp
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                        .replace("%name%", teamWarp.get().getName())
                ))
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        List<TeamWarp> teamWarps = keviinTeams.getTeamManager().getTeamWarps(team);
        return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
    }
}
