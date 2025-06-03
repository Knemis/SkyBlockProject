package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankResponse;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DepositCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public DepositCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 2) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // Optional<BankItem> bankItem = SkyBlockProjectTeams.getBankItemList().stream().filter(item -> item.getName().equalsIgnoreCase(args[0])).findFirst(); // TODO: Uncomment when getBankItemList is available
        // if (!bankItem.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchBankItem.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        try {
            // TeamBank teamBank = SkyBlockProjectTeams.getTeamManager().getTeamBank(team, bankItem.get().getName()); // TODO: Uncomment when TeamManager and bankItem are available
            // BankResponse bankResponse = bankItem.get().deposit(player, Double.parseDouble(args[1]), teamBank, SkyBlockProjectTeams); // TODO: Uncomment when bankItem and teamBank are available

            // player.sendMessage(StringUtils.color((bankResponse.isSuccess() ? SkyBlockProjectTeams.getMessages().bankDeposited : SkyBlockProjectTeams.getMessages().insufficientFundsToDeposit) // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%amount%", String.valueOf(bankResponse.getAmount()))
                    // .replace("%type%", bankItem.get().getName())
            // ));
            player.sendMessage("Deposit command needs to be reimplemented after refactoring."); // Placeholder
            return true;
        } catch (NumberFormatException exception) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notANumber.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid number format."); // Placeholder
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (args.length == 1) {
            // return SkyBlockProjectTeams.getBankItemList().stream() // TODO: Uncomment when getBankItemList is available
                    // .map(BankItem::getName)
                    // .collect(Collectors.toList());
            return Collections.emptyList(); // Placeholder
        }
        return Arrays.asList("100", "1000", "10000", "100000");
    }

}
