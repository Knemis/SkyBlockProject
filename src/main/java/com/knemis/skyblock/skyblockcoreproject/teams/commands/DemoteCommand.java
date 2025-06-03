package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DemoteCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public DemoteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
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

        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]); // TODO: Deprecated, consider UUID approach if possible
        U targetUser = skyblockTeams.getUserManager().getUser(targetPlayer); // TODO: Ensure UserManager is functional

        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        int nextRank = targetUser.getUserRank() - 1;

        if (!DoesRankExist(nextRank, skyblockTeams) || IsHigherRank(targetUser, user) || !skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.DEMOTE)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotDemoteUser
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        targetUser.setUserRank(nextRank);

        for (U member : skyblockTeams.getTeamManager().getTeamMembers(team)) { // TODO: Ensure TeamManager is functional
            Player teamMember = Bukkit.getPlayer(member.getUuid());
            if (teamMember != null) {
                if (teamMember.equals(player)) {
                    teamMember.sendMessage(StringUtils.color(skyblockTeams.getMessages().demotedPlayer
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", skyblockTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    ));
                } else {
                    teamMember.sendMessage(StringUtils.color(skyblockTeams.getMessages().userDemotedPlayer
                            .replace("%demoter%", player.getName())
                            .replace("%player%", targetUser.getName())
                            .replace("%rank%", skyblockTeams.getUserRanks().get(nextRank).name)
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    ));
                }
            }
        }
        // player.sendMessage("Demote command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private boolean DoesRankExist(int rank, SkyBlockTeams<T, U> skyblockTeams) {
        if (rank < 1) return false;
        return skyblockTeams.getUserRanks().containsKey(rank);
        // return false; // Placeholder
    }

    private boolean IsHigherRank(U target, U user) {
        if (target.getUserRank() == Rank.OWNER.getId()) return true;
        if (user.getUserRank() == Rank.OWNER.getId()) return false;
        if (user.isBypassing()) return false;
        return target.getUserRank() >= user.getUserRank();
    }

}
