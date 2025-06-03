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
public class InfoCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public InfoCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            Optional<T> userTeam = skyblockTeams.getTeamManager().getTeamViaID(user.getTeamID()); // TODO: Ensure TeamManager is functional
            if (!userTeam.isPresent()) {
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().dontHaveTeam
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return false;
            }
            sendTeamInfo(player, userTeam.get(), skyblockTeams);
            return true;
        }

        Optional<T> team;
        if(args[0].equalsIgnoreCase("location")) { // Make case-insensitive
            team = skyblockTeams.getTeamManager().getTeamViaPlayerLocation(player); // TODO: Ensure TeamManager is functional
        } else {
            team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(String.join(" ", args)); // TODO: Ensure TeamManager is functional
        }


        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        sendTeamInfo(player, team.get(), skyblockTeams);
        // player.sendMessage("Info command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    public void sendTeamInfo(Player player, T team, SkyBlockTeams<T, U> skyblockTeams) {
        List<Placeholder> placeholderList = skyblockTeams.getTeamsPlaceholderBuilder().getPlaceholders(team); // Using core Placeholder
        player.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(StringUtils.processMultiplePlaceholders(skyblockTeams.getConfiguration().teamInfoTitle, placeholderList), skyblockTeams.getConfiguration().teamInfoTitleFiller)));
        for (String line : skyblockTeams.getConfiguration().teamInfo) {
            player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(line, placeholderList)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
