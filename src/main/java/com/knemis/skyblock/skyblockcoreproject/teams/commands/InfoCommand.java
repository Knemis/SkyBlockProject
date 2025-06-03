package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color and StringUtils.getCenteredMessage and StringUtils.processMultiplePlaceholders
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class InfoCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public InfoCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (args.length == 0) { // TODO: Uncomment when TeamManager is refactored
            // Optional<T> userTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
            // if (!userTeam.isPresent()) {
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // sendTeamInfo(player, userTeam.get(), SkyBlockProjectTeams);
            // return true;
        // }

        // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(String.join(" ", args)); // TODO: Uncomment when TeamManager is refactored
        // if(args[0].equals("location")) {
            // team = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player); // TODO: Uncomment when TeamManager is refactored
        // }

        // if (!team.isPresent()) { // TODO: Uncomment when team is available
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // sendTeamInfo(player, team.get(), SkyBlockProjectTeams); // TODO: Uncomment when team is available
        player.sendMessage("Info command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    public void sendTeamInfo(Player player, T team, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholderList = SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team); // TODO: Replace Placeholder, uncomment when getTeamsPlaceholderBuilder is available
        // player.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(StringUtils.processMultiplePlaceholders(SkyBlockProjectTeams.getConfiguration().teamInfoTitle, placeholderList), SkyBlockProjectTeams.getConfiguration().teamInfoTitleFiller))); // TODO: Replace StringUtils methods
        // for (String line : SkyBlockProjectTeams.getConfiguration().teamInfo) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(line, placeholderList))); // TODO: Replace StringUtils methods
        // }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
