package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp; // TODO: Update to actual TeamWarp class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DeleteWarpCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public DeleteWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (!iridiumTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotManageWarps // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // Optional<TeamWarp> teamWarp = iridiumTeams.getTeamManager().getTeamWarp(team, args[0]); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // if (!teamWarp.isPresent()) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().unknownWarp // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // iridiumTeams.getTeamManager().deleteWarp(teamWarp.get()); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // iridiumTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Uncomment when TeamManager is refactored
                // member.sendMessage(StringUtils.color(iridiumTeams.getMessages().deletedWarp // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                        // .replace("%name%", teamWarp.get().getName())
                // ))
        // );
        player.sendMessage("DeleteWarp command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        // List<TeamWarp> teamWarps = iridiumTeams.getTeamManager().getTeamWarps(team); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList()); // TODO: Uncomment when TeamWarp is refactored
        return Collections.emptyList(); // Placeholder
    }
}
