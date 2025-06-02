package com.keviin.keviinteams.bank;

import com.keviin.keviincore.Item;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.TeamBank;
import lombok.NoArgsConstructor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class MoneyBankItem extends BankItem {

    public MoneyBankItem(double defaultAmount, Item item) {
        super("money", item, defaultAmount, true);
    }

    @Override
    public BankResponse withdraw(Player player, Number amount, TeamBank teamBank, keviinTeams<?, ?> keviinTeams) {
        double money = Math.min(amount.doubleValue(), teamBank.getNumber());
        if (money > 0) {
            EconomyResponse economyResponse = keviinTeams.getEconomy().depositPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() - money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, TeamBank teamBank, keviinTeams<?, ?> keviinTeams) {
        double money = Math.min(amount.doubleValue(), keviinTeams.getEconomy().getBalance(player));
        if (money > 0) {
            EconomyResponse economyResponse = keviinTeams.getEconomy().withdrawPlayer(player, money);
            if (economyResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                teamBank.setNumber(teamBank.getNumber() + money);
                return new BankResponse(money, true);
            }
        }
        return new BankResponse(money, false);
    }

}
