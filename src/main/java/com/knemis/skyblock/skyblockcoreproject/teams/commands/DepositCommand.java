package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankResponse;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DepositCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public DepositCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (args.length != 2) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // Optional<BankItem> bankItem = iridiumTeams.getBankItemList().stream().filter(item -> item.getName().equalsIgnoreCase(args[0])).findFirst(); // TODO: Uncomment when getBankItemList is available
        // if (!bankItem.isPresent()) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().noSuchBankItem.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        try {
            // TeamBank teamBank = iridiumTeams.getTeamManager().getTeamBank(team, bankItem.get().getName()); // TODO: Uncomment when TeamManager and bankItem are available
            // BankResponse bankResponse = bankItem.get().deposit(player, Double.parseDouble(args[1]), teamBank, iridiumTeams); // TODO: Uncomment when bankItem and teamBank are available

            // player.sendMessage(StringUtils.color((bankResponse.isSuccess() ? iridiumTeams.getMessages().bankDeposited : iridiumTeams.getMessages().insufficientFundsToDeposit) // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                    // .replace("%amount%", String.valueOf(bankResponse.getAmount()))
                    // .replace("%type%", bankItem.get().getName())
            // ));
            player.sendMessage("Deposit command needs to be reimplemented after refactoring."); // Placeholder
            return true;
        } catch (NumberFormatException exception) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().notANumber.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid number format."); // Placeholder
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        if (args.length == 1) {
            // return iridiumTeams.getBankItemList().stream() // TODO: Uncomment when getBankItemList is available
                    // .map(BankItem::getName)
                    // .collect(Collectors.toList());
            return Collections.emptyList(); // Placeholder
        }
        return Arrays.asList("100", "1000", "10000", "100000");
    }

}
