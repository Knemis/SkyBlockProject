package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends ConfirmableCommand<T, U> { // TODO: Update Team and IridiumUser to actual classes
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
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noPermission // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                player.sendMessage("You don't have permission."); // Placeholder
                return false;
            }

            // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]); // TODO: Uncomment when TeamManager is refactored
            // if (!team.isPresent()) {
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // return execute(user, team.get(), arguments, SkyBlockProjectTeams); // TODO: Uncomment when TeamManager is refactored
            player.sendMessage("Delete (admin) command needs to be reimplemented after refactoring."); // Placeholder
            return false; // Placeholder
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
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotDeleteTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            player.sendMessage("You cannot delete this team."); // Placeholder
            return false;
        }
        return true;
    }

    @Override
    protected void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 1) {
            deleteTeam(user, team, SkyBlockProjectTeams, true);
        } else { // Added else to ensure deleteTeam is called in non-admin case too after confirmation
            deleteTeam(user, team, SkyBlockProjectTeams, false);
        }
    }

    private void deleteTeam(U user, T team, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams, boolean admin) {
        Player player = user.getPlayer();
        // if (!SkyBlockProjectTeams.getTeamManager().deleteTeam(team, user)) return; // TODO: Uncomment when TeamManager is refactored

        // for (U member : SkyBlockProjectTeams.getTeamManager().getTeamMembers(team)) { // TODO: Uncomment when TeamManager is refactored
            // member.setTeamID(0);
            // Player teamMember = member.getPlayer();
            // if (teamMember != null) {
                // teamMember.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDeleted // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                // ));
            // }
        // }
        if (admin) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().deletedPlayerTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%name%", team.getName())
            // ));
            player.sendMessage("Admin deleted team " + team.getName()); // Placeholder
        } else {
            player.sendMessage("Team " + team.getName() + " deleted."); // Placeholder
        }
    }

}
