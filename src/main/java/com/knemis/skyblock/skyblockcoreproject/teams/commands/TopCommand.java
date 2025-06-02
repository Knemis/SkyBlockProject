package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TopCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {

    public String adminPermission;

    public TopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        int listLength = 10;
        TeamSorting<T> sortingType = SkyBlockProjectTeams.getSortingTypes().get(0);
        boolean excludePrivate = !sender.hasPermission(adminPermission);

        if (sender instanceof Player && arguments.length == 0) return sendGUI((Player) sender, SkyBlockProjectTeams);

        switch (arguments.length) {
            case 3: {
                try {
                    listLength = Math.min(Integer.parseInt(arguments[2]), 100);
                } catch (NumberFormatException ignored) {}
            }
            case 2: {
                for(TeamSorting<T> pluginSortingType : SkyBlockProjectTeams.getSortingTypes()) {
                    if (arguments[1].equalsIgnoreCase(pluginSortingType.getName())) sortingType = pluginSortingType;
                }
            }
            case 1: {
                if (!arguments[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(StringUtils.color(syntax.replace("prefix", SkyBlockProjectTeams.getConfiguration().prefix)));
                    return false;
                }
            }
            default: {
                sendList(sender, SkyBlockProjectTeams, sortingType, listLength, excludePrivate);
                return true;
            }
        }
    }

     public boolean sendGUI(Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
         player.openInventory(new TopGUI<>(SkyBlockProjectTeams.getTop().valueTeamSort, player, SkyBlockProjectTeams).getInventory());
         return true;
    }

    public void sendList(CommandSender sender, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams, TeamSorting<T> sortingType, int listLength, boolean excludePrivate) {
        List<T> teamList = SkyBlockProjectTeams.getTeamManager().getTeams(sortingType, excludePrivate);

        sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(SkyBlockProjectTeams.getMessages().topCommandHeader.replace("%sort_type%", sortingType.getName()), SkyBlockProjectTeams.getMessages().topCommandFiller)));

        for (int i = 0; i < listLength;  i++) {
            if(i == sortingType.getSortedTeams(SkyBlockProjectTeams).size()) break;
            T team = teamList.get(i);
            List<Placeholder> placeholders = SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team);
            placeholders.add(new Placeholder("value", SkyBlockProjectTeams.getConfiguration().numberFormatter.format(sortingType.getValue(team))));
            placeholders.add(new Placeholder("rank", String.valueOf(i+1)));

            String color = "&7";
            switch(i) {
                case 0: {
                    color = SkyBlockProjectTeams.getMessages().topFirstColor;
                    break;
                }
                case 1: {
                    color = SkyBlockProjectTeams.getMessages().topSecondColor;
                    break;
                }
                case 2: {
                    color = SkyBlockProjectTeams.getMessages().topThirdColor;
                    break;
                }
            }
            placeholders.add(new Placeholder("color", color));


            sender.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(SkyBlockProjectTeams.getMessages().topCommandMessage, placeholders)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!commandSender.hasPermission(adminPermission)) return Collections.singletonList("");
        switch(args.length) {
            case 1: {
                return Collections.singletonList("list");
            }
            case 2: {
                return SkyBlockProjectTeams.getSortingTypes().stream().map(TeamSorting::getName).collect(Collectors.toList());
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
