package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class KickCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public KickCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.KICK)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotKick
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        U kickedPlayer = keviinTeams.getUserManager().getUser(offlinePlayer);
        if (team.getId() != kickedPlayer.getTeamID()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (offlinePlayer.getUniqueId() == player.getUniqueId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotKickYourself
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if ((kickedPlayer.getUserRank() >= user.getUserRank() || kickedPlayer.getUserRank() == Rank.OWNER.getId()) && !user.isBypassing() && user.getUserRank() != Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotKickHigherRank
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        kickedPlayer.setTeam(null);
        Optional.ofNullable(kickedPlayer.getPlayer()).ifPresent(player1 -> player1.sendMessage(StringUtils.color(keviinTeams.getMessages().youHaveBeenKicked
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%player%", player.getName())
        )));
        keviinTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(player1 ->
                player1.sendMessage(StringUtils.color(keviinTeams.getMessages().playerKicked
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", kickedPlayer.getName())
                        .replace("%kicker%", player.getName())
                ))
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
