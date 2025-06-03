package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends Team, U extends User<T>> extends ConfirmableCommand<T, U> {
    public String adminPermission;

    public DeleteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission, boolean requiresConfirmation) {
        super(args, description, syntax, permission, cooldownInSeconds, requiresConfirmation);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            if (!player.hasPermission(adminPermission)) {
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noPermission
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                // player.sendMessage("You don't have permission."); // Placeholder
                return false;
            }

            Optional<T> team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]); // TODO: Ensure TeamManager is functional
            if (!team.isPresent()) {
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return false;
            }
            return execute(user, team.get(), arguments, skyblockTeams); // TODO: Ensure TeamManager is functional
            // player.sendMessage("Delete (admin) command needs to be reimplemented after refactoring."); // Placeholder
            // return false; // Placeholder
        }
        return super.execute(user, arguments, skyblockTeams);
    }

    @Override
    protected boolean isCommandValid(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) { // Admin trying to delete other team
            return true;
        }

        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotDeleteTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            // player.sendMessage("You cannot delete this team."); // Placeholder
            return false;
        }
        return true;
    }

    @Override
    protected void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        if (arguments.length == 1) { // Admin deleting a specific team
            deleteTeam(user, team, skyblockTeams, true);
        } else { // User deleting their own team
            deleteTeam(user, team, skyblockTeams, false);
        }
    }

    private void deleteTeam(U user, T team, SkyBlockTeams<T, U> skyblockTeams, boolean admin) {
        Player player = user.getPlayer();
        if (!skyblockTeams.getTeamManager().deleteTeam(team, user)) return; // TODO: Ensure TeamManager is functional

        for (U member : skyblockTeams.getTeamManager().getTeamMembers(team)) { // TODO: Ensure TeamManager is functional
            member.setTeamID(0);
            Player teamMember = member.getPlayer();
            if (teamMember != null) {
                teamMember.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDeleted
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                ));
            }
        }
        if (admin) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().deletedPlayerTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%name%", team.getName())
            ));
            // player.sendMessage("Admin deleted team " + team.getName()); // Placeholder
        } else {
            // Message for self-deletion is typically handled by deleteTeam method or success message in Command.java
             player.sendMessage(StringUtils.color(skyblockTeams.getMessages().deletedTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
             ));
            // player.sendMessage("Team " + team.getName() + " deleted."); // Placeholder
        }
    }

}
