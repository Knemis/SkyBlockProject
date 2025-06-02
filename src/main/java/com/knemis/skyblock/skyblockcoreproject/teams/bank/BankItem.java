package com.knemis.skyblock.skyblockcoreproject.teams.bank;

// import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;

import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
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

    public abstract BankResponse withdraw(Player player, Number amount, TeamBank teamBank, SkyBlockProjectTeams<?, ?> teams);

    public abstract BankResponse deposit(Player player, Number amount, TeamBank teamBank, SkyBlockProjectTeams<?, ?> teams);

}
