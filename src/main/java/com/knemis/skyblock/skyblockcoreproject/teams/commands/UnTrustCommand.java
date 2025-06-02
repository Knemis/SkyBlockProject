package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamTrust;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class UnTrustCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public UnTrustCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        U offlinePlayer = keviinTeams.getUserManager().getUser(Bukkit.getServer().getOfflinePlayer(args[0]));
        Optional<TeamTrust> teamTrust = keviinTeams.getTeamManager().getTeamTrust(team, offlinePlayer);
        if (!teamTrust.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noActiveTrust.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        keviinTeams.getTeamManager().deleteTeamTrust(teamTrust.get());
        player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamTrustRevoked
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%player%", offlinePlayer.getName())
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
