package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.bank.BankItem;
import com.keviin.keviinteams.bank.BankResponse;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamBank;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DepositCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public DepositCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<BankItem> bankItem = keviinTeams.getBankItemList().stream().filter(item -> item.getName().equalsIgnoreCase(args[0])).findFirst();
        if (!bankItem.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noSuchBankItem.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        try {
            TeamBank teamBank = keviinTeams.getTeamManager().getTeamBank(team, bankItem.get().getName());
            BankResponse bankResponse = bankItem.get().deposit(player, Double.parseDouble(args[1]), teamBank, keviinTeams);

            player.sendMessage(StringUtils.color((bankResponse.isSuccess() ? keviinTeams.getMessages().bankDeposited : keviinTeams.getMessages().insufficientFundsToDeposit)
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%amount%", String.valueOf(bankResponse.getAmount()))
                    .replace("%type%", bankItem.get().getName())
            ));
            return true;
        } catch (NumberFormatException exception) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notANumber.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        if (args.length == 1) {
            return keviinTeams.getBankItemList().stream()
                    .map(BankItem::getName)
                    .collect(Collectors.toList());
        }
        return Arrays.asList("100", "1000", "10000", "100000");
    }

}
