package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class InviteCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public InviteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (!iridiumTeams.getTeamManager().getTeamPermission(team, user, PermissionType.INVITE)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotInvite // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        Player invitee = Bukkit.getServer().getPlayer(args[0]);
        if (invitee == null) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().notAPlayer // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            player.sendMessage("Player not found."); // Placeholder
            return false;
        }
        // U offlinePlayerUser = iridiumTeams.getUserManager().getUser(invitee); // TODO: Uncomment when UserManager is refactored
        // if (offlinePlayerUser.getTeamID() == team.getId()) { // TODO: Uncomment when offlinePlayerUser and Team are refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().userAlreadyInTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (iridiumTeams.getTeamManager().getTeamInvite(team, offlinePlayerUser).isPresent()) { // TODO: Uncomment when TeamManager and offlinePlayerUser are refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().inviteAlreadyPresent // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // iridiumTeams.getTeamManager().createTeamInvite(team, offlinePlayerUser, user); // TODO: Uncomment when TeamManager and offlinePlayerUser are refactored
        // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamInviteSent // TODO: Replace StringUtils.color
                // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // .replace("%player%", offlinePlayerUser.getName())
        // ));
        // invitee.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamInviteReceived // TODO: Replace StringUtils.color
                // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // .replace("%player%", player.getName())
        // ));
        player.sendMessage("Invite command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
