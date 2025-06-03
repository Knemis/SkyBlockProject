package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class InviteCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public InviteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.INVITE)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotInvite
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Player invitee = Bukkit.getServer().getPlayer(args[0]);
        if (invitee == null) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().notAPlayer
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            // player.sendMessage("Player not found."); // Placeholder
            return false;
        }
        U offlinePlayerUser = skyblockTeams.getUserManager().getUser(invitee); // TODO: Ensure UserManager is functional
        if (offlinePlayerUser.getTeamID() == team.getId()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().userAlreadyInTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (skyblockTeams.getTeamManager().getTeamInvite(team, offlinePlayerUser).isPresent()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().inviteAlreadyPresent
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        skyblockTeams.getTeamManager().createTeamInvite(team, offlinePlayerUser, user); // TODO: Ensure TeamManager is functional
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamInviteSent
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%player%", offlinePlayerUser.getName())
        ));
        invitee.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamInviteReceived
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%player%", player.getName())
        ));
        // player.sendMessage("Invite command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
