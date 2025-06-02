package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BankGUI;
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
public class ExperienceCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public String adminPermission;

    public ExperienceCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 3) {
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]);
            if (!team.isPresent()) {
                sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            int amount;
            try {
                amount = Integer.parseInt(arguments[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notANumber
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }

            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noPermission
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }

            switch (arguments[0].toLowerCase()) {
                case "give":
                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().gaveExperience
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                    ));

                    team.get().setExperience(team.get().getExperience() + amount);
                    return true;
                case "remove":
                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().removedExperience
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.min(amount, team.get().getExperience())))
                    ));

                    team.get().setExperience(team.get().getExperience() - amount);
                    return true;
                case "set":
                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setExperience
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(Math.max(amount, 0)))
                    ));

                    team.get().setExperience(amount);
                    return true;
                default:
                    sender.sendMessage(StringUtils.color(syntax
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
                    return false;
            }
        }
        if (arguments.length != 0) {
            sender.sendMessage(StringUtils.color(syntax
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        return SkyBlockProjectTeams.getCommandManager().executeCommand(sender, SkyBlockProjectTeams.getCommands().infoCommand, arguments);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        player.openInventory(new BankGUI<>(team, player, SkyBlockProjectTeams).getInventory());
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
