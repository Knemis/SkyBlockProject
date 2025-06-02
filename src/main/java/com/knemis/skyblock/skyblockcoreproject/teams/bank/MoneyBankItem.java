package com.knemis.skyblock.skyblockcoreproject.teams.bank;

// import com.keviin.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import lombok.NoArgsConstructor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class MoneyBankItem extends BankItem {

    public MoneyBankItem(double defaultAmount, com.knemis.skyblock.skyblockcoreproject.teams.Item item) { // TODO: Replace with actual Item class
        super("money", item, defaultAmount, true);
    }

    @Override
    public BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, IridiumTeams<?, ?> iridiumTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), teamBank.getNumber());
        if (money > 0) {
            EconomyResponse economyResponse = iridiumTeams.getEconomy().depositPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() - money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, IridiumTeams<?, ?> iridiumTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), iridiumTeams.getEconomy().getBalance(player));
        if (money > 0) {
            EconomyResponse economyResponse = iridiumTeams.getEconomy().withdrawPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() + money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

}
