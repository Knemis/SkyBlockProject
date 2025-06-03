package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class LevelCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes

    public LevelCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        // if (args.length == 0) { // TODO: Uncomment when TeamManager is refactored
            // Optional<T> userTeam = iridiumTeams.getTeamManager().getTeamViaID(user.getTeamID());
            // if (!userTeam.isPresent()) {
                // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().dontHaveTeam // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // sendTeamLevel(player, userTeam.get(), iridiumTeams);
            // return true;
        // }

        // Optional<T> team = iridiumTeams.getTeamManager().getTeamViaNameOrPlayer(String.join(" ", args)); // TODO: Uncomment when TeamManager is refactored
        // if(args[0].equals("location")) {
            // team = iridiumTeams.getTeamManager().getTeamViaPlayerLocation(player); // TODO: Uncomment when TeamManager is refactored
        // }

        // if (!team.isPresent()) { // TODO: Uncomment when team is available
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // sendTeamLevel(player, team.get(), iridiumTeams); // TODO: Uncomment when team is available
        player.sendMessage("Level command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    public void sendTeamLevel(Player player, T team, IridiumTeams<T, U> iridiumTeams) {
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholderList = iridiumTeams.getTeamsPlaceholderBuilder().getPlaceholders(team); // TODO: Replace Placeholder, uncomment when getTeamsPlaceholderBuilder is available
        // player.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(StringUtils.processMultiplePlaceholders(iridiumTeams.getConfiguration().teamInfoTitle, placeholderList), iridiumTeams.getConfiguration().teamInfoTitleFiller))); // TODO: Replace StringUtils methods
        // for (String line : iridiumTeams.getConfiguration().levelInfo) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(line, placeholderList))); // TODO: Replace StringUtils methods
        // }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}