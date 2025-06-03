package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.TopGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TopCommand<T extends Team, U extends User<T>> extends Command<T, U> {

    public String adminPermission;

    public TopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        int listLength = 10;
        TeamSorting<T> sortingType = skyblockTeams.getSortingTypes().get(0); // TODO: Ensure getSortingTypes is functional & not empty
        boolean excludePrivate = !sender.hasPermission(adminPermission);

        if (sender instanceof Player && arguments.length == 0) return sendGUI((Player) sender, skyblockTeams);

        switch (arguments.length) {
            case 3: {
                try {
                    listLength = Math.min(Integer.parseInt(arguments[2]), 100);
                } catch (NumberFormatException ignored) {}
            }
            // Fallthrough intended
            case 2: {
                for(TeamSorting<T> pluginSortingType : skyblockTeams.getSortingTypes()) { // TODO: Ensure getSortingTypes is functional
                    if (arguments[1].equalsIgnoreCase(pluginSortingType.getName())) sortingType = pluginSortingType;
                }
            }
            // Fallthrough intended
            case 1: {
                if (!arguments[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                    return false;
                }
            }
            // Fallthrough intended
            default: {
                sendList(sender, skyblockTeams, sortingType, listLength, excludePrivate);
                return true;
            }
        }
        // sender.sendMessage("Top command (list) needs to be reimplemented after refactoring."); // Placeholder
        // return true; // Handled by switch
    }

     public boolean sendGUI(Player player, SkyBlockTeams<T, U> skyblockTeams) {
         player.openInventory(new TopGUI<>(skyblockTeams.getTop().valueTeamSort, player, skyblockTeams).getInventory()); // TODO: Ensure getTop is functional
         // player.sendMessage("Top GUI needs to be reimplemented."); // Placeholder
         return true;
    }

    public void sendList(CommandSender sender, SkyBlockTeams<T, U> skyblockTeams, TeamSorting<T> sortingType, int listLength, boolean excludePrivate) {
        List<T> teamList = skyblockTeams.getTeamManager().getTeams(sortingType, excludePrivate); // TODO: Ensure TeamManager and sortingType are functional

        sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(skyblockTeams.getMessages().topCommandHeader.replace("%sort_type%", sortingType.getName()), skyblockTeams.getMessages().topCommandFiller)));

        for (int i = 0; i < listLength;  i++) {
            if(i >= teamList.size()) break; // Corrected condition: use teamList.size()
            T team = teamList.get(i);
            List<Placeholder> placeholders = skyblockTeams.getTeamsPlaceholderBuilder().getPlaceholders(team); // TODO: Ensure TeamsPlaceholderBuilder is functional
            placeholders.add(new Placeholder("value", skyblockTeams.getConfiguration().numberFormatter.format(sortingType.getValue(team))));
            placeholders.add(new Placeholder("rank", String.valueOf(i+1)));

            String color = "&7";
            switch(i) {
                case 0: {
                    color = skyblockTeams.getMessages().topFirstColor;
                    break;
                }
                case 1: {
                    color = skyblockTeams.getMessages().topSecondColor;
                    break;
                }
                case 2: {
                    color = skyblockTeams.getMessages().topThirdColor;
                    break;
                }
            }
            placeholders.add(new Placeholder("color", color));


            sender.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(skyblockTeams.getMessages().topCommandMessage, placeholders)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        if (!commandSender.hasPermission(adminPermission)) return Collections.singletonList("");
        switch(args.length) {
            case 1: {
                return Collections.singletonList("list");
            }
            case 2: {
                return skyblockTeams.getSortingTypes().stream().map(TeamSorting::getName).collect(Collectors.toList()); // TODO: Ensure getSortingTypes is functional
            }
            case 3: {
                return Collections.singletonList("10");
            }
            default: {
                return Collections.emptyList(); // Return empty list for more args, not null
            }
        }
        // return Collections.emptyList(); // Placeholder
    }
}
