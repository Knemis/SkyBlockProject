package com.keviin.keviinteams.bank;

import com.keviin.keviincore.Item;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.TeamBank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class BankItem {

    private String name;
    private Item item;
    private double defaultAmount;
    private boolean enabled;

    public abstract BankResponse withdraw(Player player, Number amount, TeamBank teamBank, keviinTeams<?, ?> teams);

    public abstract BankResponse deposit(Player player, Number amount, TeamBank teamBank, keviinTeams<?, ?> teams);

}
