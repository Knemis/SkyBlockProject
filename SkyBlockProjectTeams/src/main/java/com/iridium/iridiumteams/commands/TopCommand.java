package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.TopGUI;
import com.keviin.keviinteams.sorting.TeamSorting;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TopCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    public String adminPermission;

    public TopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, keviinTeams<T, U> keviinTeams) {
        int listLength = 10;
        TeamSorting<T> sortingType = keviinTeams.getSortingTypes().get(0);
        boolean excludePrivate = !sender.hasPermission(adminPermission);

        if (sender instanceof Player && arguments.length == 0) return sendGUI((Player) sender, keviinTeams);

        switch (arguments.length) {
            case 3: {
                try {
                    listLength = Math.min(Integer.parseInt(arguments[2]), 100);
                } catch (NumberFormatException ignored) {}
            }
            case 2: {
                for(TeamSorting<T> pluginSortingType : keviinTeams.getSortingTypes()) {
                    if (arguments[1].equalsIgnoreCase(pluginSortingType.getName())) sortingType = pluginSortingType;
                }
            }
            case 1: {
                if (!arguments[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(StringUtils.color(syntax.replace("prefix", keviinTeams.getConfiguration().prefix)));
                    return false;
                }
            }
            default: {
                sendList(sender, keviinTeams, sortingType, listLength, excludePrivate);
                return true;
            }
        }
    }

     public boolean sendGUI(Player player, keviinTeams<T, U> keviinTeams) {
         player.openInventory(new TopGUI<>(keviinTeams.getTop().valueTeamSort, player, keviinTeams).getInventory());
         return true;
    }

    public void sendList(CommandSender sender, keviinTeams<T, U> keviinTeams, TeamSorting<T> sortingType, int listLength, boolean excludePrivate) {
        List<T> teamList = keviinTeams.getTeamManager().getTeams(sortingType, excludePrivate);

        sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(keviinTeams.getMessages().topCommandHeader.replace("%sort_type%", sortingType.getName()), keviinTeams.getMessages().topCommandFiller)));

        for (int i = 0; i < listLength;  i++) {
            if(i == sortingType.getSortedTeams(keviinTeams).size()) break;
            T team = teamList.get(i);
            List<Placeholder> placeholders = keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(team);
            placeholders.add(new Placeholder("value", keviinTeams.getConfiguration().numberFormatter.format(sortingType.getValue(team))));
            placeholders.add(new Placeholder("rank", String.valueOf(i+1)));

            String color = "&7";
            switch(i) {
                case 0: {
                    color = keviinTeams.getMessages().topFirstColor;
                    break;
                }
                case 1: {
                    color = keviinTeams.getMessages().topSecondColor;
                    break;
                }
                case 2: {
                    color = keviinTeams.getMessages().topThirdColor;
                    break;
                }
            }
            placeholders.add(new Placeholder("color", color));


            sender.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(keviinTeams.getMessages().topCommandMessage, placeholders)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        if (!commandSender.hasPermission(adminPermission)) return Collections.singletonList("");
        switch(args.length) {
            case 1: {
                return Collections.singletonList("list");
            }
            case 2: {
                return keviinTeams.getSortingTypes().stream().map(TeamSorting::getName).collect(Collectors.toList());
            }
            case 3: {
                return Collections.singletonList("10");
            }
            default: {
                return null;
            }
        }
    }
}
