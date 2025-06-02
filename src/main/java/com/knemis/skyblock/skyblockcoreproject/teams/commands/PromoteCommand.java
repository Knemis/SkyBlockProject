package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PromoteCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public PromoteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        U targetUser = SkyBlockProjectTeams.getUserManager().getUser(targetPlayer);

        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userNotInYourTeam.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        int nextRank = targetUser.getUserRank() + 1;

        if (!DoesRankExist(nextRank, SkyBlockProjectTeams) || IsHigherRank(targetUser, user) || !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.PROMOTE)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotPromoteUser.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        targetUser.setUserRank(nextRank);

        for (U member : SkyBlockProjectTeams.getTeamManager().getTeamMembers(team)) {
            Player teamMember = Bukkit.getPlayer(member.getUuid());
            if (teamMember != null) {
                if (teamMember.equals(player)) {
                    teamMember.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().promotedPlayer
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", SkyBlockProjectTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
                } else {
                    teamMember.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userPromotedPlayer
                            .replace("%promoter%", player.getName())
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", SkyBlockProjectTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private boolean DoesRankExist(int rank, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (rank < 1) return false;
        return SkyBlockProjectTeams.getUserRanks().containsKey(rank);
    }

    private boolean IsHigherRank(U target, U user) {
        if (target.getUserRank() == Rank.OWNER.getId()) return true;
        if (user.getUserRank() == Rank.OWNER.getId()) return false;
        if (user.isBypassing()) return false;
        return target.getUserRank() + 1 >= user.getUserRank();
    }

}
