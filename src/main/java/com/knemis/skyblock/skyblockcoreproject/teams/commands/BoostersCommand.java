package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement; // TODO: Update to actual Enhancement class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType; // TODO: Update to actual EnhancementType class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.BoostersGUI; // TODO: Update to actual BoostersGUI class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BoostersCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public BoostersCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            // player.openInventory(new BoostersGUI<>(team, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BoostersGUI is refactored
            player.sendMessage("Boosters GUI needs to be reimplemented."); // Placeholder
            return false;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("buy")) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        String booster = args[1];
        // Enhancement<?> enhancement = SkyBlockProjectTeams.getEnhancementList().get(booster); // TODO: Uncomment when getEnhancementList is available
        // if (enhancement == null || enhancement.type != EnhancementType.BOOSTER) { // TODO: Uncomment when Enhancement and EnhancementType are refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchBooster // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // boolean success = SkyBlockProjectTeams.getTeamManager().UpdateEnhancement(team, booster, player); // TODO: Uncomment when TeamManager is refactored
        boolean success = false; // Placeholder
        if (success) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().purchasedBooster // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%booster%", booster)
            // ));
            player.sendMessage("Purchased booster " + booster); // Placeholder
        } else {
            player.sendMessage("Could not purchase booster."); // Placeholder
        }
        return success;
    }

}
