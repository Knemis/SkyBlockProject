package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TrustCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public TrustCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.TRUST)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotTrust
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Player invitee = Bukkit.getServer().getPlayer(args[0]);
        if (invitee == null) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notAPlayer
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        U offlinePlayerUser = keviinTeams.getUserManager().getUser(invitee);
        if (offlinePlayerUser.getTeamID() == team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().userAlreadyInTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (keviinTeams.getTeamManager().getTeamTrust(team, offlinePlayerUser).isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().trustAlreadyPresent
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        keviinTeams.getTeamManager().createTeamTrust(team, offlinePlayerUser, user);
        player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamTrustSent
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%player%", offlinePlayerUser.getName())
        ));
        invitee.sendMessage(StringUtils.color(keviinTeams.getMessages().teamTrustReceived
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%player%", player.getName())
        ));
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
