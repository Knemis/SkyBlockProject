package com.knemis.skyblock.skyblockcoreproject.teams.bank;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class BankItem {

    private String name;
    private com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
    private double defaultAmount;
    private boolean enabled;

    public abstract BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> teams); // TODO: Update TeamBank to actual class

    public abstract BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> teams); // TODO: Update TeamBank to actual class

}
