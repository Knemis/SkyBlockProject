package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class CreateCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public CreateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (args.length < 1) {
            if (keviinTeams.getConfiguration().createRequiresName) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
                return false;
            }
            keviinTeams.getTeamManager().createTeam(player, null).thenAccept(team -> {
                if (team == null) return;
                user.setUserRank(Rank.OWNER.getId());
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamCreated
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                getCooldownProvider().applyCooldown(player);
            });
            return false;
        }

        String teamName = String.join(" ", args);
        if (teamName.length() < keviinTeams.getConfiguration().minTeamNameLength) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameTooShort
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%min_length%", String.valueOf(keviinTeams.getConfiguration().minTeamNameLength))
            ));
            return false;
        }
        if (teamName.length() > keviinTeams.getConfiguration().maxTeamNameLength) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameTooLong
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%max_length%", String.valueOf(keviinTeams.getConfiguration().maxTeamNameLength))
            ));
            return false;
        }
        if (keviinTeams.getTeamManager().getTeamViaName(teamName).isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameAlreadyExists
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        keviinTeams.getTeamManager().createTeam(player, teamName).thenAccept(team -> {
            if (team == null) return;
            user.setUserRank(Rank.OWNER.getId());
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamCreated
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            getCooldownProvider().applyCooldown(player);
        });
        return false;
    }

}
