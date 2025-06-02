package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends ConfirmableCommand<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public String adminPermission;

    public DeleteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission, boolean requiresConfirmation) {
        super(args, description, syntax, permission, cooldownInSeconds, requiresConfirmation);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            if (!player.hasPermission(adminPermission)) {
                // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().noPermission // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // ));
                player.sendMessage("You don't have permission."); // Placeholder
                return false;
            }

            // Optional<T> team = iridiumTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]); // TODO: Uncomment when TeamManager is refactored
            // if (!team.isPresent()) {
                // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // return execute(user, team.get(), arguments, iridiumTeams); // TODO: Uncomment when TeamManager is refactored
            player.sendMessage("Delete (admin) command needs to be reimplemented after refactoring."); // Placeholder
            return false; // Placeholder
        }
        return super.execute(user, arguments, iridiumTeams);
    }

    @Override
    protected boolean isCommandValid(U user, T team, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            return true;
        }

        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotDeleteTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            player.sendMessage("You cannot delete this team."); // Placeholder
            return false;
        }
        return true;
    }

    @Override
    protected void executeAfterConfirmation(U user, T team, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        if (arguments.length == 1) {
            deleteTeam(user, team, iridiumTeams, true);
        } else { // Added else to ensure deleteTeam is called in non-admin case too after confirmation
            deleteTeam(user, team, iridiumTeams, false);
        }
    }

    private void deleteTeam(U user, T team, IridiumTeams<T, U> iridiumTeams, boolean admin) {
        Player player = user.getPlayer();
        // if (!iridiumTeams.getTeamManager().deleteTeam(team, user)) return; // TODO: Uncomment when TeamManager is refactored

        // for (U member : iridiumTeams.getTeamManager().getTeamMembers(team)) { // TODO: Uncomment when TeamManager is refactored
            // member.setTeamID(0);
            // Player teamMember = member.getPlayer();
            // if (teamMember != null) {
                // teamMember.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDeleted // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                // ));
            // }
        // }
        if (admin) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().deletedPlayerTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                    // .replace("%name%", team.getName())
            // ));
            player.sendMessage("Admin deleted team " + team.getName()); // Placeholder
        } else {
            player.sendMessage("Team " + team.getName() + " deleted."); // Placeholder
        }
    }

}
