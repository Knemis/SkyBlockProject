package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
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
public class HelpCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public HelpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        List<Command<T, U>> availableCommands = skyblockTeams.getCommandManager().getCommands().stream() // TODO: Ensure CommandManager is functional
                .filter(command -> !command.isSuperSecretCommand())
                .filter(command -> sender.hasPermission(command.permission) || command.permission.isEmpty())
                .collect(Collectors.toList());

        int page = 1;
        int maxPage = (int) Math.ceil(availableCommands.size() / 8.0);

        // Read optional page argument
        if (args.length != 0) {
            String pageArgument = args[0];
            if (pageArgument.matches("[0-9]+")) {
                page = Integer.parseInt(pageArgument);
            }
        }

        // Correct requested page if it's out of bounds
        if (page > maxPage) {
            page = maxPage;
        } else if (page < 1) {
            page = 1;
        }

        // Prepare the footer
        TextComponent footerText = new TextComponent(StringUtils.color(skyblockTeams.getMessages().helpCommandFooter
                .replace("%page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage))
        ));
        TextComponent previousButton = new TextComponent(StringUtils.color(skyblockTeams.getMessages().helpCommandPreviousPage));
        TextComponent nextButton = new TextComponent(StringUtils.color(skyblockTeams.getMessages().helpCommandNextPage));
        if (page != 1) {
            previousButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + skyblockTeams.getCommandManager().getCommand() + " help " + (page - 1))); // TODO: Ensure CommandManager is functional
            previousButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(skyblockTeams.getMessages().helpCommandPreviousPageHover)).create()));
        }
        if (page != maxPage) {
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + skyblockTeams.getCommandManager().getCommand() + " help " + (page + 1))); // TODO: Ensure CommandManager is functional
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(skyblockTeams.getMessages().helpCommandNextPageHover)).create()));
        }

        // Send all messages
        sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(skyblockTeams.getMessages().helpCommandHeader, skyblockTeams.getMessages().helpCommandFiller)));
        availableCommands.stream()
                .skip((page - 1) * 8L)
                .limit(8)
                .map(command -> StringUtils.color(skyblockTeams.getMessages().helpCommandMessage
                        .replace("%command%", command.aliases.get(0))
                        .replace("%description%", command.description)))
                .forEach(sender::sendMessage);

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(previousButton, footerText, nextButton);
        }
        // sender.sendMessage("Help command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        int availableCommandAmount = (int) skyblockTeams.getCommandManager().getCommands().stream() // TODO: Ensure CommandManager is functional
                .filter(command -> commandSender.hasPermission(command.permission) || command.permission.isEmpty())
                .count();

        // Return all numbers from 1 to the max page
        return IntStream.rangeClosed(1, (int) Math.ceil(availableCommandAmount / 8.0))
                .boxed()
                .map(String::valueOf)
                .collect(Collectors.toList());
        // return Collections.emptyList(); // Placeholder
    }

}
