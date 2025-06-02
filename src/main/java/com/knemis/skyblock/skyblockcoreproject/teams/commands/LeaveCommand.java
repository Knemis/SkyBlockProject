package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;


import java.util.List;

@NoArgsConstructor
public class LeaveCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public LeaveCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();

        if (user.getUserRank() == Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().ownerCannotLeave
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().leftTeam
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%name%", team.getName())
        ));

        SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).forEach(teamUser -> {
            Player teamPlayer = Bukkit.getPlayer(teamUser.getUuid());
            if (teamPlayer != null && teamPlayer != player) {
                teamPlayer.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userLeftTeam
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%name%", team.getName())
                        .replace("%player%", player.getName())
                ));
            }
        });

        user.setTeam(null);
        return true;
    }

}
