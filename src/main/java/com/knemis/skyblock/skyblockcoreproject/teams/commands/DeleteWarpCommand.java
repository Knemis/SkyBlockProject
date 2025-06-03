package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DeleteWarpCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public DeleteWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) { // args.length != 2 seems to be for an optional password which is not used here
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        Optional<TeamWarp> teamWarp = skyblockTeams.getTeamManager().getTeamWarp(team, args[0]); // TODO: Ensure TeamManager and TeamWarp are functional
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().unknownWarp
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        skyblockTeams.getTeamManager().deleteWarp(teamWarp.get()); // TODO: Ensure TeamManager and TeamWarp are functional
        skyblockTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Ensure TeamManager is functional
                member.sendMessage(StringUtils.color(skyblockTeams.getMessages().deletedWarp
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                        .replace("%name%", teamWarp.get().getName())
                ))
        );
        // player.sendMessage("DeleteWarp command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        List<TeamWarp> teamWarps = skyblockTeams.getTeamManager().getTeamWarps(team); // TODO: Ensure TeamManager and TeamWarp are functional
        return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
        // return Collections.emptyList(); // Placeholder
    }
}
