package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankResponse;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DepositCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public DepositCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        Optional<BankItem> bankItem = skyblockTeams.getBankItemList().stream().filter(item -> item.getName().equalsIgnoreCase(args[0])).findFirst();
        if (!bankItem.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noSuchBankItem.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        try {
            TeamBank teamBank = skyblockTeams.getTeamManager().getTeamBank(team, bankItem.get().getName()); // TODO: Ensure TeamManager and TeamBank are functional
            BankResponse bankResponse = bankItem.get().deposit(player, Double.parseDouble(args[1]), teamBank, skyblockTeams);

            player.sendMessage(StringUtils.color((bankResponse.isSuccess() ? skyblockTeams.getMessages().bankDeposited : skyblockTeams.getMessages().insufficientFundsToDeposit)
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%amount%", String.valueOf(bankResponse.getAmount()))
                    .replace("%type%", bankItem.get().getName())
            ));
            // player.sendMessage("Deposit command needs to be reimplemented after refactoring."); // Placeholder
            return true;
        } catch (NumberFormatException exception) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().notANumber.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid number format."); // Placeholder
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        if (args.length == 1) {
            return skyblockTeams.getBankItemList().stream()
                    .map(BankItem::getName)
                    .collect(Collectors.toList());
            // return Collections.emptyList(); // Placeholder
        }
        return Arrays.asList("100", "1000", "10000", "100000");
    }

}
