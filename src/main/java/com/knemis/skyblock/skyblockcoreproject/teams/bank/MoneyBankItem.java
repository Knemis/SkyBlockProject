package com.knemis.skyblock.skyblockcoreproject.teams.bank;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
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
    public BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> SkyBlockProjectTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), teamBank.getNumber());
        if (money > 0) {
            EconomyResponse economyResponse = SkyBlockProjectTeams.getEconomy().depositPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() - money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> SkyBlockProjectTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), SkyBlockProjectTeams.getEconomy().getBalance(player));
        if (money > 0) {
            EconomyResponse economyResponse = SkyBlockProjectTeams.getEconomy().withdrawPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() + money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

}
