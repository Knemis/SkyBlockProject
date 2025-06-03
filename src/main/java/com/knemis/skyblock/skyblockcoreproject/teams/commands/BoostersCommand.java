package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BoostersGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BoostersCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public BoostersCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new BoostersGUI<>(team, player, skyblockTeams).getInventory());
            // player.sendMessage("Boosters GUI needs to be reimplemented."); // Placeholder
            return false; // Typically GUIs opened return false or true based on if further command processing is stopped
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("buy")) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        String booster = args[1];
        Enhancement<?> enhancement = skyblockTeams.getEnhancementList().get(booster); // TODO: Ensure getEnhancementList is functional
        if (enhancement == null || enhancement.type != EnhancementType.BOOSTER) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noSuchBooster
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean success = skyblockTeams.getTeamManager().UpdateEnhancement(team, booster, player); // TODO: Ensure TeamManager & UpdateEnhancement are functional
        // boolean success = false; // Placeholder
        if (success) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().purchasedBooster
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%booster%", booster)
            ));
            // player.sendMessage("Purchased booster " + booster); // Placeholder
        } else {
            // This message might be redundant if UpdateEnhancement handles its own messaging on failure
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotPurchaseUpgrade.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Could not purchase booster."); // Placeholder
        }
        return success;
    }

}
