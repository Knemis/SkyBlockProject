package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.bank.BankItem;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamBank;
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
public class BankCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public String adminPermission;

    public BankCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, keviinTeams<T, U> keviinTeams) {
        if (arguments.length == 4) {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]);
            if (!team.isPresent()) {
                sender.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            Optional<BankItem> bankItem = keviinTeams.getBankItemList().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(arguments[2]))
                    .findAny();
            double amount;
            try {
                amount = Double.parseDouble(arguments[3]);
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

            if (!bankItem.isPresent()) {
                sender.sendMessage(StringUtils.color(keviinTeams.getMessages().noSuchBankItem
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            TeamBank teamBank = keviinTeams.getTeamManager().getTeamBank(team.get(), bankItem.get().getName());
            switch (arguments[0].toLowerCase()) {
                case "give":
                    teamBank.setNumber(teamBank.getNumber() + amount);

                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().gaveBank
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                case "remove":
                    teamBank.setNumber(teamBank.getNumber() - amount);

                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().removedBank
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                case "set":
                    teamBank.setNumber(amount);

                    sender.sendMessage(StringUtils.color(keviinTeams.getMessages().setBank
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                default:
                    sender.sendMessage(StringUtils.color(syntax
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    ));
            }
            return true;
        }
        if (arguments.length != 0) {
            sender.sendMessage(StringUtils.color(syntax
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        return super.execute(sender, arguments, keviinTeams);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        player.openInventory(new BankGUI<>(team, player, keviinTeams).getInventory());
        return false;
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
                return keviinTeams.getBankItemList().stream().map(BankItem::getName).collect(Collectors.toList());
            case 4:
                return Arrays.asList("1", "10", "100");
            default:
                return Collections.emptyList();
        }
    }
}
