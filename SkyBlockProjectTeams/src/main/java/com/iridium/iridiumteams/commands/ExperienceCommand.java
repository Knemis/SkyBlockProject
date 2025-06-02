package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.BankGUI;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ExperienceCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public String adminPermission;

    public ExperienceCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, keviinTeams<T, U> keviinTeams) {
        if (arguments.length == 3) {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]);
            if (!team.isPresent()) {
                sender.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            int amount;
            try {
                amount = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(StringUtils.color(keviinTeams.getMessages().notANumber
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }

            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage(StringUtils.color(keviinTeams.getMessages().noPermission
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }

            switch (arguments[0].toLowerCase()) {
                case "give":
                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().gaveExperience
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                    ));

                    team.get().setExperience(team.get().getExperience() + amount);
                    return true;
                case "remove":
                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().removedExperience
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.min(amount, team.get().getExperience())))
                    ));

                    team.get().setExperience(team.get().getExperience() - amount);
                    return true;
                case "set":
                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().setExperience
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.max(amount, 0)))
                    ));

                    team.get().setExperience(amount);
                    return true;
                default:
                    sender.sendMessage(StringUtils.color(syntax
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    ));
                    return false;
            }
        }
        if (arguments.length != 0) {
            sender.sendMessage(StringUtils.color(syntax
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        return keviinTeams.getCommandManager().executeCommand(sender, keviinTeams.getCommands().infoCommand, arguments);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        player.openInventory(new BankGUI<>(team, player, keviinTeams).getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        if (!commandSender.hasPermission(adminPermission)) return Collections.emptyList();
        switch (args.length) {
            case 1:
                return Arrays.asList("give", "set", "remove");
            case 2:
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            case 3:
                return Arrays.asList("1", "10", "100");
            default:
                return Collections.emptyList();
        }
    }
}
