package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
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
public class KickCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public KickCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.KICK)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKick // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        // U kickedPlayer = SkyBlockProjectTeams.getUserManager().getUser(offlinePlayer); // TODO: Uncomment when UserManager is refactored
        // if (team.getId() != kickedPlayer.getTeamID()) { // TODO: Uncomment when Team and kickedPlayer are refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userNotInYourTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        if (offlinePlayer.getUniqueId() == player.getUniqueId()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKickYourself // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            player.sendMessage("You cannot kick yourself."); // Placeholder
            return false;
        }
        // if ((kickedPlayer.getUserRank() >= user.getUserRank() || kickedPlayer.getUserRank() == Rank.OWNER.getId()) && !user.isBypassing() && user.getUserRank() != Rank.OWNER.getId()) { // TODO: Uncomment when kickedPlayer is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKickHigherRank // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // kickedPlayer.setTeam(null); // TODO: Uncomment when kickedPlayer is refactored
        // Optional.ofNullable(kickedPlayer.getPlayer()).ifPresent(player1 -> player1.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().youHaveBeenKicked // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // .replace("%player%", player.getName())
        // )));
        // SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(player1 -> // TODO: Uncomment when TeamManager is refactored
                // player1.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().playerKicked // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%player%", kickedPlayer.getName())
                        // .replace("%kicker%", player.getName())
                // ))
        // );
        player.sendMessage("Kick command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
