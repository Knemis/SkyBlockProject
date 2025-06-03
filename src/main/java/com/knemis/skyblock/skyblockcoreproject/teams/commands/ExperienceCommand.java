package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BankGUI; // Assuming BankGUI is for experience too, or needs specific GUI
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
public class ExperienceCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public String adminPermission;

    public ExperienceCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        if (arguments.length == 3) {
            Optional<T> team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]); // TODO: Ensure TeamManager is functional
            if (!team.isPresent()) {
                sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return false;
            }
            int amount;
            try {
                amount = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().notANumber
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                // sender.sendMessage("Invalid number format."); // Placeholder
                return false;
            }

            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().noPermission
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                // sender.sendMessage("You don't have permission."); // Placeholder
                return false;
            }

            switch (arguments[0].toLowerCase()) {
                case "give":
                    sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().gaveExperience
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                    ));

                    team.get().setExperience(team.get().getExperience() + amount); // TODO: Ensure Team set/save methods are functional
                    return true;
                case "remove":
                    sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().removedExperience
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.min(amount, team.get().getExperience())))
                    ));

                    team.get().setExperience(team.get().getExperience() - amount); // TODO: Ensure Team set/save methods are functional
                    return true;
                case "set":
                    sender.sendMessage(StringUtils.color(skyblockTeams.getMessages().setExperience
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.max(amount, 0)))
                    ));

                    team.get().setExperience(amount); // TODO: Ensure Team set/save methods are functional
                    return true;
                default:
                    sender.sendMessage(StringUtils.color(syntax
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    ));
                    return false;
            }
            // sender.sendMessage("Experience admin command needs to be reimplemented after refactoring."); // Placeholder
            // return true; // Handled by switch
        }
        if (arguments.length != 0) {
            sender.sendMessage(StringUtils.color(syntax
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            // sender.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }

        return skyblockTeams.getCommandManager().executeCommand(sender, skyblockTeams.getCommands().infoCommand, arguments); // TODO: Ensure CommandManager and getCommands are functional
        // return false; // Placeholder
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        // This command might not be intended for players to open a GUI directly,
        // but rather for admins to modify team experience or for info display.
        // The execute for CommandSender above handles the admin part.
        // If players should see their team's experience, it's usually part of team info.
        // For now, let's assume this specific overload means showing info or similar.
        // Or if BankGUI is indeed for experience, it would be:
        // player.openInventory(new BankGUI<>(team, player, skyblockTeams).getInventory());
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().experienceMessage // Placeholder for a specific message
            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            .replace("%experience%", String.valueOf(team.getExperience())) // TODO: Ensure Team has getExperience
        ));
        // player.sendMessage("Bank GUI needs to be reimplemented."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
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
