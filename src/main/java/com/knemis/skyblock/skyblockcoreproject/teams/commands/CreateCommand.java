package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class CreateCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public CreateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (skyblockTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (args.length < 1) {
            if (skyblockTeams.getConfiguration().createRequiresName) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                return false;
            }
            skyblockTeams.getTeamManager().createTeam(player, null).thenAccept(team -> { // TODO: Ensure TeamManager is functional
                if (team == null) return;
                user.setUserRank(Rank.OWNER.getId());
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamCreated
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                getCooldownProvider().applyCooldown(player); // TODO: Ensure CooldownProvider is functional
            });
            // player.sendMessage("Create command (no name) needs to be reimplemented after refactoring."); // Placeholder
            return true; // Assuming async operation, command itself succeeded in starting
        }

        String teamName = String.join(" ", args);
        if (teamName.length() < skyblockTeams.getConfiguration().minTeamNameLength) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamNameTooShort
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%min_length%", String.valueOf(skyblockTeams.getConfiguration().minTeamNameLength))
            ));
            return false;
        }
        if (teamName.length() > skyblockTeams.getConfiguration().maxTeamNameLength) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamNameTooLong
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%max_length%", String.valueOf(skyblockTeams.getConfiguration().maxTeamNameLength))
            ));
            return false;
        }
        if (skyblockTeams.getTeamManager().getTeamViaName(teamName).isPresent()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamNameAlreadyExists
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        skyblockTeams.getTeamManager().createTeam(player, teamName).thenAccept(team -> { // TODO: Ensure TeamManager is functional
            if (team == null) return;
            user.setUserRank(Rank.OWNER.getId());
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamCreated
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            getCooldownProvider().applyCooldown(player); // TODO: Ensure CooldownProvider is functional
        });
        // player.sendMessage("Create command (with name) needs to be reimplemented after refactoring."); // Placeholder
        return true; // Assuming async operation, command itself succeeded in starting
    }

}
