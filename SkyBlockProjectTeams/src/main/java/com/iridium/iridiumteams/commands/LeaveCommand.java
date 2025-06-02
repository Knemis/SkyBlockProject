package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class LeaveCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public LeaveCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();

        if (user.getUserRank() == Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().ownerCannotLeave
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.sendMessage(StringUtils.color(keviinTeams.getMessages().leftTeam
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%name%", team.getName())
        ));

        keviinTeams.getTeamManager().getTeamMembers(team).forEach(teamUser -> {
            Player teamPlayer = Bukkit.getPlayer(teamUser.getUuid());
            if (teamPlayer != null && teamPlayer != player) {
                teamPlayer.sendMessage(StringUtils.color(keviinTeams.getMessages().userLeftTeam
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%name%", team.getName())
                        .replace("%player%", player.getName())
                ));
            }
        });

        user.setTeam(null);
        return true;
    }

}
