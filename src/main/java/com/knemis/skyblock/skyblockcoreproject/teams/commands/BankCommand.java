package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
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
public class BankCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public String adminPermission;

    public BankCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 4) {
            // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]); // TODO: Uncomment when TeamManager is refactored
            // if (!team.isPresent()) {
                // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // Optional<BankItem> bankItem = SkyBlockProjectTeams.getBankItemList().stream() // TODO: Uncomment when getBankItemList is available
                    // .filter(item -> item.getName().equalsIgnoreCase(arguments[2]))
                    // .findAny();
            double amount;
            try {
                amount = Double.parseDouble(arguments[3]);
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

            // if (!bankItem.isPresent()) { // TODO: Uncomment when bankItem is available
                // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchBankItem // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // TeamBank teamBank = SkyBlockProjectTeams.getTeamManager().getTeamBank(team.get(), bankItem.get().getName()); // TODO: Uncomment when TeamManager and bankItem are available
            // switch (arguments[0].toLowerCase()) {
                // case "give":
                    // teamBank.setNumber(teamBank.getNumber() + amount);

                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().gaveBank // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(amount))
                            // .replace("%item%", bankItem.get().getName())
                    // ));
                    // break;
                // case "remove":
                    // teamBank.setNumber(teamBank.getNumber() - amount);

                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().removedBank // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(amount))
                            // .replace("%item%", bankItem.get().getName())
                    // ));
                    // break;
                // case "set":
                    // teamBank.setNumber(amount);

                    // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setBank // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            // .replace("%player%", arguments[1])
                            // .replace("%amount%", String.valueOf(amount))
                            // .replace("%item%", bankItem.get().getName())
                    // ));
                    // break;
                // default:
                    // sender.sendMessage(StringUtils.color(syntax // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // ));
            // }
            sender.sendMessage("Bank admin commands need reimplementation after refactoring."); // Placeholder
            return true;
        }
        if (arguments.length != 0) {
            // sender.sendMessage(StringUtils.color(syntax // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            sender.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        return super.execute(sender, arguments, SkyBlockProjectTeams);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // player.openInventory(new BankGUI<>(team, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BankGUI is refactored
        player.sendMessage("Bank GUI needs to be reimplemented."); // Placeholder
        return false;
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
                // return SkyBlockProjectTeams.getBankItemList().stream().map(BankItem::getName).collect(Collectors.toList()); // TODO: Uncomment when getBankItemList is available
                return Collections.emptyList(); // Placeholder
            case 4:
                return Arrays.asList("1", "10", "100");
            default:
                return Collections.emptyList();
        }
    }
}
