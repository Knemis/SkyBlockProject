package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color and StringUtils.getCenteredMessage
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor
public class HelpCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public HelpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // List<Command<T, U>> availableCommands = SkyBlockProjectTeams.getCommandManager().getCommands().stream() // TODO: Uncomment when CommandManager is refactored
                // .filter(command -> !command.isSuperSecretCommand())
                // .filter(command -> sender.hasPermission(command.permission) || command.permission.isEmpty())
                // .collect(Collectors.toList());

        // int page = 1;
        // int maxPage = (int) Math.ceil(availableCommands.size() / 8.0); // TODO: Uncomment when availableCommands is available

        // // Read optional page argument
        // if (args.length != 0) {
            // String pageArgument = args[0];
            // if (pageArgument.matches("[0-9]+")) {
                // page = Integer.parseInt(pageArgument);
            // }
        // }

        // // Correct requested page if it's out of bounds
        // if (page > maxPage) { // TODO: Uncomment when maxPage is available
            // page = maxPage;
        // } else if (page < 1) {
            // page = 1;
        // }

        // // Prepare the footer
        // TextComponent footerText = new TextComponent(StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandFooter // TODO: Replace StringUtils.color
                // .replace("%page%", String.valueOf(page))
                // .replace("%max_page%", String.valueOf(maxPage)) // TODO: Uncomment when maxPage is available
        // ));
        // TextComponent previousButton = new TextComponent(StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandPreviousPage)); // TODO: Replace StringUtils.color
        // TextComponent nextButton = new TextComponent(StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandNextPage)); // TODO: Replace StringUtils.color
        // if (page != 1) {
            // previousButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + SkyBlockProjectTeams.getCommandManager().getCommand() + " help " + (page - 1))); // TODO: Uncomment when CommandManager is refactored
            // previousButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandPreviousPageHover)).create())); // TODO: Replace StringUtils.color
        // }
        // if (page != maxPage) { // TODO: Uncomment when maxPage is available
            // nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + SkyBlockProjectTeams.getCommandManager().getCommand() + " help " + (page + 1))); // TODO: Uncomment when CommandManager is refactored
            // nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandNextPageHover)).create())); // TODO: Replace StringUtils.color
        // }

        // // Send all messages
        // sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(SkyBlockProjectTeams.getMessages().helpCommandHeader, SkyBlockProjectTeams.getMessages().helpCommandFiller))); // TODO: Replace StringUtils.color and StringUtils.getCenteredMessage
        // availableCommands.stream() // TODO: Uncomment when availableCommands is available
                // .skip((page - 1) * 8L)
                // .limit(8)
                // .map(command -> StringUtils.color(SkyBlockProjectTeams.getMessages().helpCommandMessage // TODO: Replace StringUtils.color
                        // .replace("%command%", command.aliases.get(0))
                        // .replace("%description%", command.description)))
                // .forEach(sender::sendMessage);

        // if (sender instanceof Player) {
            // ((Player) sender).spigot().sendMessage(previousButton, footerText, nextButton); // TODO: Uncomment when buttons and footerText are available
        // }
        sender.sendMessage("Help command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // int availableCommandAmount = (int) SkyBlockProjectTeams.getCommandManager().getCommands().stream() // TODO: Uncomment when CommandManager is refactored
                // .filter(command -> commandSender.hasPermission(command.permission) || command.permission.isEmpty())
                // .count();

        // // Return all numbers from 1 to the max page
        // return IntStream.rangeClosed(1, (int) Math.ceil(availableCommandAmount / 8.0))
                // .boxed()
                // .map(String::valueOf)
                // .collect(Collectors.toList());
        return Collections.emptyList(); // Placeholder
    }

}
