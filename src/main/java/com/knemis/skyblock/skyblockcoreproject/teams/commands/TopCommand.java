package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils methods
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.Rank; // Rank is not directly used here, but good to keep if Command base class needs it.
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.TopGUI; // TODO: Update to actual TopGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting; // TODO: Update to actual TeamSorting class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TopCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes

    public String adminPermission;

    public TopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // int listLength = 10; // TODO: Uncomment when sortingType is available
        // com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType = SkyBlockProjectTeams.getSortingTypes().get(0); // TODO: Uncomment when getSortingTypes is available
        // boolean excludePrivate = !sender.hasPermission(adminPermission);

        // if (sender instanceof Player && arguments.length == 0) return sendGUI((Player) sender, SkyBlockProjectTeams);

        // switch (arguments.length) { // TODO: Uncomment when sortingType is available
            // case 3: {
                // try {
                    // listLength = Math.min(Integer.parseInt(arguments[2]), 100);
                // } catch (NumberFormatException ignored) {}
            // }
            // case 2: {
                // for(com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> pluginSortingType : SkyBlockProjectTeams.getSortingTypes()) { // TODO: Uncomment when getSortingTypes is available
                    // if (arguments[1].equalsIgnoreCase(pluginSortingType.getName())) sortingType = pluginSortingType;
                // }
            // }
            // case 1: {
                // if (!arguments[0].equalsIgnoreCase("list")) {
                    // sender.sendMessage(StringUtils.color(syntax.replace("prefix", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                    // return false;
                // }
            // }
            // default: {
                // sendList(sender, SkyBlockProjectTeams, sortingType, listLength, excludePrivate);
                // return true;
            // }
        // }
        sender.sendMessage("Top command (list) needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

     public boolean sendGUI(Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
         // player.openInventory(new TopGUI<>(SkyBlockProjectTeams.getTop().valueTeamSort, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when TopGUI and getTop are refactored
         player.sendMessage("Top GUI needs to be reimplemented."); // Placeholder
         return true;
    }

    public void sendList(CommandSender sender, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams, com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType, int listLength, boolean excludePrivate) { // TODO: Update TeamSorting type
        // List<T> teamList = SkyBlockProjectTeams.getTeamManager().getTeams(sortingType, excludePrivate); // TODO: Uncomment when TeamManager and sortingType are refactored

        // sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(SkyBlockProjectTeams.getMessages().topCommandHeader.replace("%sort_type%", sortingType.getName()), SkyBlockProjectTeams.getMessages().topCommandFiller))); // TODO: Replace StringUtils methods

        // for (int i = 0; i < listLength;  i++) { // TODO: Uncomment when teamList and sortingType are available
            // if(i == sortingType.getSortedTeams(SkyBlockProjectTeams).size()) break;
            // T team = teamList.get(i);
            // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team); // TODO: Replace Placeholder, uncomment when getTeamsPlaceholderBuilder is available
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("value", SkyBlockProjectTeams.getConfiguration().numberFormatter.format(sortingType.getValue(team)))); // TODO: Replace Placeholder
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("rank", String.valueOf(i+1))); // TODO: Replace Placeholder

            // String color = "&7";
            // switch(i) {
                // case 0: {
                    // color = SkyBlockProjectTeams.getMessages().topFirstColor;
                    // break;
                // }
                // case 1: {
                    // color = SkyBlockProjectTeams.getMessages().topSecondColor;
                    // break;
                // }
                // case 2: {
                    // color = SkyBlockProjectTeams.getMessages().topThirdColor;
                    // break;
                // }
            // }
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("color", color)); // TODO: Replace Placeholder


            // sender.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(SkyBlockProjectTeams.getMessages().topCommandMessage, placeholders))); // TODO: Replace StringUtils methods
        // }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!commandSender.hasPermission(adminPermission)) return Collections.singletonList("");
        // switch(args.length) { // TODO: Uncomment when getSortingTypes is available
            // case 1: {
                // return Collections.singletonList("list");
            // }
            // case 2: {
                // return SkyBlockProjectTeams.getSortingTypes().stream().map(com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting::getName).collect(Collectors.toList());
            // }
            // case 3: {
                // return Collections.singletonList("10");
            // }
            // default: {
                // return null;
            // }
        // }
        return Collections.emptyList(); // Placeholder
    }
}
