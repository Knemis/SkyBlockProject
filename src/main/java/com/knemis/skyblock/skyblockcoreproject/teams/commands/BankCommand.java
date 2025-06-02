package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank;
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
public class BankCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public String adminPermission;

    public BankCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (arguments.length == 4) {
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[1]);
            if (!team.isPresent()) {
                sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            Optional<BankItem> bankItem = SkyBlockProjectTeams.getBankItemList().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(arguments[2]))
                    .findAny();
            double amount;
            try {
                amount = Double.parseDouble(arguments[3]);
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

            if (!bankItem.isPresent()) {
                sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchBankItem
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            TeamBank teamBank = SkyBlockProjectTeams.getTeamManager().getTeamBank(team.get(), bankItem.get().getName());
            switch (arguments[0].toLowerCase()) {
                case "give":
                    teamBank.setNumber(teamBank.getNumber() + amount);

                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().gaveBank
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                case "remove":
                    teamBank.setNumber(teamBank.getNumber() - amount);

                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().removedBank
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                case "set":
                    teamBank.setNumber(amount);

                    sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setBank
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%player%", arguments[1])
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%item%", bankItem.get().getName())
                    ));
                    break;
                default:
                    sender.sendMessage(StringUtils.color(syntax
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
            }
            return true;
        }
        if (arguments.length != 0) {
            sender.sendMessage(StringUtils.color(syntax
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        return super.execute(sender, arguments, SkyBlockProjectTeams);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        player.openInventory(new BankGUI<>(team, player, SkyBlockProjectTeams).getInventory());
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
                return SkyBlockProjectTeams.getBankItemList().stream().map(BankItem::getName).collect(Collectors.toList());
            case 4:
                return Arrays.asList("1", "10", "100");
            default:
                return Collections.emptyList();
        }
    }
}
