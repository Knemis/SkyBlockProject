package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends ConfirmableCommand<T, U> {
    public String adminPermission;

    public DeleteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission, boolean requiresConfirmation) {
        super(args, description, syntax, permission, cooldownInSeconds, requiresConfirmation);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            if (!player.hasPermission(adminPermission)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noPermission
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }

            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]);
            if (!team.isPresent()) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            return execute(user, team.get(), arguments, SkyBlockProjectTeams);
        }
        return super.execute(user, arguments, SkyBlockProjectTeams);
    }

    @Override
    protected boolean isCommandValid(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            return true;
        }

        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotDeleteTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        return true;
    }

    @Override
    protected void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 1) {
            deleteTeam(user, team, SkyBlockProjectTeams, true);
        }

        deleteTeam(user, team, SkyBlockProjectTeams, false);
    }

    private void deleteTeam(U user, T team, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams, boolean admin) {
        Player player = user.getPlayer();
        if (!SkyBlockProjectTeams.getTeamManager().deleteTeam(team, user)) return;

        for (U member : SkyBlockProjectTeams.getTeamManager().getTeamMembers(team)) {
            member.setTeamID(0);
            Player teamMember = member.getPlayer();
            if (teamMember != null) {
                teamMember.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDeleted
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                ));
            }
        }
        if (admin) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().deletedPlayerTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%name%", team.getName())
            ));
        }
    }

}
