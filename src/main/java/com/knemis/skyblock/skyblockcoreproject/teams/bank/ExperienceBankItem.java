package com.knemis.skyblock.skyblockcoreproject.teams.bank;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // Added import
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams; // Changed IridiumTeams to SkyBlockTeams
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.PlayerUtils; // Updated to core.keviincore path
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class ExperienceBankItem extends BankItem {

    public ExperienceBankItem(double defaultAmount, Item item) {
        super("experience", item, defaultAmount, true);
    }

    @Override
    public BankResponse withdraw(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockTeams<?, ?> skyblockTeams) { // TODO: Update TeamBank to actual class
        int experience = Math.min(amount.intValue(), (int) teamBank.getNumber());
        if (experience > 0) {
            PlayerUtils.setTotalExperience(player, PlayerUtils.getTotalExperience(player) + experience);
            teamBank.setNumber(teamBank.getNumber() - experience);
            return new BankResponse(experience, true);
        }
        return new BankResponse(experience, false);
    }

    @Override
    public BankResponse deposit(Player player, Number amount, com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank, SkyBlockTeams<?, ?> skyblockTeams) { // TODO: Update TeamBank to actual class
        int experience = Math.min(amount.intValue(), PlayerUtils.getTotalExperience(player));
        // int experience = amount.intValue(); // Temporary placeholder
        if (experience > 0) {
            PlayerUtils.setTotalExperience(player, PlayerUtils.getTotalExperience(player) - experience);
            teamBank.setNumber(teamBank.getNumber() + experience);
            return new BankResponse(experience, true);
        }
        return new BankResponse(experience, false);
    }

}
