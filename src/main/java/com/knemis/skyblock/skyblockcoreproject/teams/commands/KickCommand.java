package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

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
public class KickCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public KickCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.KICK)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKick
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        U kickedPlayer = SkyBlockProjectTeams.getUserManager().getUser(offlinePlayer);
        if (team.getId() != kickedPlayer.getTeamID()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (offlinePlayer.getUniqueId() == player.getUniqueId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKickYourself
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if ((kickedPlayer.getUserRank() >= user.getUserRank() || kickedPlayer.getUserRank() == Rank.OWNER.getId()) && !user.isBypassing() && user.getUserRank() != Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKickHigherRank
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        kickedPlayer.setTeam(null);
        Optional.ofNullable(kickedPlayer.getPlayer()).ifPresent(player1 -> player1.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().youHaveBeenKicked
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%player%", player.getName())
        )));
        SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(player1 ->
                player1.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().playerKicked
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%player%", kickedPlayer.getName())
                        .replace("%kicker%", player.getName())
                ))
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
