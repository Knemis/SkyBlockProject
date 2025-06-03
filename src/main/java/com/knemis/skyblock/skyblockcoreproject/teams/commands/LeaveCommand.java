package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class LeaveCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public LeaveCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();

        if (user.getUserRank() == Rank.OWNER.getId()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().ownerCannotLeave // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            player.sendMessage("Owners cannot leave the team. Use /team delete or transfer ownership."); // Placeholder
            return false;
        }

        // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().leftTeam // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // .replace("%name%", team.getName())
        // ));
        player.sendMessage("You have left the team " + team.getName()); // Placeholder

        // SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).forEach(teamUser -> { // TODO: Uncomment when TeamManager is refactored
            // Player teamPlayer = Bukkit.getPlayer(teamUser.getUuid());
            // if (teamPlayer != null && teamPlayer != player) {
                // teamPlayer.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userLeftTeam // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%name%", team.getName())
                        // .replace("%player%", player.getName())
                // ));
            // }
        // });

        user.setTeam(null);
        return true;
    }

}
