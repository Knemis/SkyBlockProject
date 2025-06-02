package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends Team, U extends keviinUser<T>> extends ConfirmableCommand<T, U> {
    public String adminPermission;

    public DeleteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission, boolean requiresConfirmation) {
        super(args, description, syntax, permission, cooldownInSeconds, requiresConfirmation);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            if (!player.hasPermission(adminPermission)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().noPermission
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }

            Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]);
            if (!team.isPresent()) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            return execute(user, team.get(), arguments, keviinTeams);
        }
        return super.execute(user, arguments, keviinTeams);
    }

    @Override
    protected boolean isCommandValid(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            return true;
        }

        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotDeleteTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        return true;
    }

    @Override
    protected void executeAfterConfirmation(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        if (arguments.length == 1) {
            deleteTeam(user, team, keviinTeams, true);
        }

        deleteTeam(user, team, keviinTeams, false);
    }

    private void deleteTeam(U user, T team, keviinTeams<T, U> keviinTeams, boolean admin) {
        Player player = user.getPlayer();
        if (!keviinTeams.getTeamManager().deleteTeam(team, user)) return;

        for (U member : keviinTeams.getTeamManager().getTeamMembers(team)) {
            member.setTeamID(0);
            Player teamMember = member.getPlayer();
            if (teamMember != null) {
                teamMember.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDeleted
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                ));
            }
        }
        if (admin) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().deletedPlayerTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%name%", team.getName())
            ));
        }
    }

}
