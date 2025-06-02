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
import java.util.stream.Collectors;

@NoArgsConstructor
public class PromoteCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public PromoteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        U targetUser = keviinTeams.getUserManager().getUser(targetPlayer);

        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().userNotInYourTeam.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        int nextRank = targetUser.getUserRank() + 1;

        if (!DoesRankExist(nextRank, keviinTeams) || IsHigherRank(targetUser, user) || !keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.PROMOTE)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotPromoteUser.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        targetUser.setUserRank(nextRank);

        for (U member : keviinTeams.getTeamManager().getTeamMembers(team)) {
            Player teamMember = Bukkit.getPlayer(member.getUuid());
            if (teamMember != null) {
                if (teamMember.equals(player)) {
                    teamMember.sendMessage(StringUtils.color(keviinTeams.getMessages().promotedPlayer
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", keviinTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    ));
                } else {
                    teamMember.sendMessage(StringUtils.color(keviinTeams.getMessages().userPromotedPlayer
                            .replace("%promoter%", player.getName())
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", keviinTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    ));
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private boolean DoesRankExist(int rank, keviinTeams<T, U> keviinTeams) {
        if (rank < 1) return false;
        return keviinTeams.getUserRanks().containsKey(rank);
    }

    private boolean IsHigherRank(U target, U user) {
        if (target.getUserRank() == Rank.OWNER.getId()) return true;
        if (user.getUserRank() == Rank.OWNER.getId()) return false;
        if (user.isBypassing()) return false;
        return target.getUserRank() + 1 >= user.getUserRank();
    }

}
