package com.knemis.skyblock.skyblockcoreproject.teams.bank;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // Added import
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams; // Changed IridiumTeams to SkyBlockTeams
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import lombok.NoArgsConstructor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class MoneyBankItem extends BankItem {

    public MoneyBankItem(double defaultAmount, Item item) {
        super("money", item, defaultAmount, true);
    }

    @Override
    public BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockTeams<?, ?> skyblockTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), teamBank.getNumber());
        if (money > 0) {
            EconomyResponse economyResponse = skyblockTeams.getEconomy().depositPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() - money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockTeams<?, ?> skyblockTeams) { // TODO: Update TeamBank to actual class
        double money = Math.min(amount.doubleValue(), skyblockTeams.getEconomy().getBalance(player));
        if (money > 0) {
            EconomyResponse economyResponse = skyblockTeams.getEconomy().withdrawPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() + money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

}
