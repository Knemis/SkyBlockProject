package com.knemis.skyblock.skyblockcoreproject.teams.bank;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
// import com.knemis.skyblock.skyblockcoreproject.teams.utils.PlayerUtils; // TODO: Update to actual PlayerUtils class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class ExperienceBankItem extends BankItem {

    public ExperienceBankItem(double defaultAmount, com.knemis.skyblock.skyblockcoreproject.teams.Item item) { // TODO: Replace with actual Item class
        super("experience", item, defaultAmount, true);
    }

    @Override
    public BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> SkyBlockProjectTeams) { // TODO: Update TeamBank to actual class
        int experience = Math.min(amount.intValue(), (int) teamBank.getNumber());
        if (experience > 0) {
            // PlayerUtils.setTotalExperience(player, PlayerUtils.getTotalExperience(player) + experience); // TODO: Uncomment when PlayerUtils is available
            teamBank.setNumber(teamBank.getNumber() - experience);
            return new BankResponse(experience, true);
        }
        return new BankResponse(experience, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockProjectTeams<?, ?> SkyBlockProjectTeams) { // TODO: Update TeamBank to actual class
        // int experience = Math.min(amount.intValue(), PlayerUtils.getTotalExperience(player)); // TODO: Uncomment when PlayerUtils is available
        int experience = amount.intValue(); // Temporary placeholder
        if (experience > 0) {
            // PlayerUtils.setTotalExperience(player, PlayerUtils.getTotalExperience(player) - experience); // TODO: Uncomment when PlayerUtils is available
            teamBank.setNumber(teamBank.getNumber() + experience);
            return new BankResponse(experience, true);
        }
        return new BankResponse(experience, false);
    }

}
