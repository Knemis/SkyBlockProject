package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
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
public class HelpCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public HelpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args, keviinTeams<T, U> keviinTeams) {
        List<Command<T, U>> availableCommands = keviinTeams.getCommandManager().getCommands().stream()
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
        TextComponent footerText = new TextComponent(StringUtils.color(keviinTeams.getMessages().helpCommandFooter
                .replace("%page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage))
        ));
        TextComponent previousButton = new TextComponent(StringUtils.color(keviinTeams.getMessages().helpCommandPreviousPage));
        TextComponent nextButton = new TextComponent(StringUtils.color(keviinTeams.getMessages().helpCommandNextPage));
        if (page != 1) {
            previousButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + keviinTeams.getCommandManager().getCommand() + " help " + (page - 1)));
            previousButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(keviinTeams.getMessages().helpCommandPreviousPageHover)).create()));
        }
        if (page != maxPage) {
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + keviinTeams.getCommandManager().getCommand() + " help " + (page + 1)));
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(keviinTeams.getMessages().helpCommandNextPageHover)).create()));
        }

        // Send all messages
        sender.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(keviinTeams.getMessages().helpCommandHeader, keviinTeams.getMessages().helpCommandFiller)));
        availableCommands.stream()
                .skip((page - 1) * 8L)
                .limit(8)
                .map(command -> StringUtils.color(keviinTeams.getMessages().helpCommandMessage
                        .replace("%command%", command.aliases.get(0))
                        .replace("%description%", command.description)))
                .forEach(sender::sendMessage);

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(previousButton, footerText, nextButton);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        int availableCommandAmount = (int) keviinTeams.getCommandManager().getCommands().stream()
                .filter(command -> commandSender.hasPermission(command.permission) || command.permission.isEmpty())
                .count();

        // Return all numbers from 1 to the max page
        return IntStream.rangeClosed(1, (int) Math.ceil(availableCommandAmount / 8.0))
                .boxed()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

}
