package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.BankGUI; // TODO: Update to actual BankGUI class
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
public class ExperienceCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public String adminPermission;

    public ExperienceCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 3) {
            // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]); // TODO: Uncomment when TeamManager is refactored
            // if (!team.isPresent()) {
                // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            int amount;
            try {
                amount = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException exception) {
                // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notANumber // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                sender.sendMessage("Invalid number format."); // Placeholder
                return false;
            }

            if (!sender.hasPermission(adminPermission)) {
                // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noPermission // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                sender.sendMessage("You don't have permission."); // Placeholder
                return false;
            }

            // switch (arguments[0].toLowerCase()) { // TODO: Uncomment when team is available
                // case "give":
                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().gaveExperience // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(amount))
                    // ));

                    // team.get().setExperience(team.get().getExperience() + amount);
                    // return true;
                // case "remove":
                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().removedExperience // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(Math.min(amount, team.get().getExperience())))
                    // ));

                    // team.get().setExperience(team.get().getExperience() - amount);
                    // return true;
                // case "set":
                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setExperience // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(Math.max(amount, 0)))
                    // ));

                    // team.get().setExperience(amount);
                    // return true;
                // default:
                    // sender.sendMessage(StringUtils.color(syntax // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // ));
                    // return false;
            // }
            sender.sendMessage("Experience admin command needs to be reimplemented after refactoring."); // Placeholder
            return true;
        }
        if (arguments.length != 0) {
            // sender.sendMessage(StringUtils.color(syntax // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            sender.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }

        // return SkyBlockProjectTeams.getCommandManager().executeCommand(sender, SkyBlockProjectTeams.getCommands().infoCommand, arguments); // TODO: Uncomment when CommandManager and getCommands are refactored
        return false; // Placeholder
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // player.openInventory(new BankGUI<>(team, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BankGUI is refactored
        player.sendMessage("Bank GUI needs to be reimplemented."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
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
