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
public class PromoteCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public PromoteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
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

        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        // U targetUser = iridiumTeams.getUserManager().getUser(targetPlayer); // TODO: Uncomment when UserManager is refactored

        // if (targetUser.getTeamID() != team.getId()) { // TODO: Uncomment when targetUser and Team are refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().userNotInYourTeam.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        // int nextRank = targetUser.getUserRank() + 1; // TODO: Uncomment when targetUser is refactored

        // if (!DoesRankExist(nextRank, iridiumTeams) || IsHigherRank(targetUser, user) || !iridiumTeams.getTeamManager().getTeamPermission(team, user, PermissionType.PROMOTE)) { // TODO: Uncomment when dependencies are refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotPromoteUser.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        // targetUser.setUserRank(nextRank); // TODO: Uncomment when targetUser and nextRank are available

        // for (U member : iridiumTeams.getTeamManager().getTeamMembers(team)) { // TODO: Uncomment when TeamManager is refactored
            // Player teamMember = Bukkit.getPlayer(member.getUuid());
            // if (teamMember != null) {
                // if (teamMember.equals(player)) {
                    // teamMember.sendMessage(StringUtils.color(iridiumTeams.getMessages().promotedPlayer // TODO: Replace StringUtils.color
                            // .replace("%player%", targetUser.getName())
                            // .replace("%rank%", iridiumTeams.getUserRanks().get(nextRank).name) // TODO: Uncomment when getUserRanks is available
                            // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                    // ));
                // } else {
                    // teamMember.sendMessage(StringUtils.color(iridiumTeams.getMessages().userPromotedPlayer // TODO: Replace StringUtils.color
                            // .replace("%promoter%", player.getName())
                            // .replace("%player%", targetUser.getName())
                            // .replace("%rank%", iridiumTeams.getUserRanks().get(nextRank).name) // TODO: Uncomment when getUserRanks is available
                            // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                    // ));
                // }
            // }
        // }
        player.sendMessage("Promote command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private boolean DoesRankExist(int rank, IridiumTeams<T, U> iridiumTeams) {
        if (rank < 1) return false;
        // return iridiumTeams.getUserRanks().containsKey(rank); // TODO: Uncomment when getUserRanks is available
        return false; // Placeholder
    }

    private boolean IsHigherRank(U target, U user) {
        // if (target.getUserRank() == Rank.OWNER.getId()) return true; // TODO: Uncomment when target is available
        if (user.getUserRank() == Rank.OWNER.getId()) return false;
        if (user.isBypassing()) return false;
        return target.getUserRank() + 1 >= user.getUserRank();
    }

}
