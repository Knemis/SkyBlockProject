package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.enhancements.Enhancement;
import com.keviin.keviinteams.enhancements.EnhancementType;
import com.keviin.keviinteams.gui.BoostersGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BoostersCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public BoostersCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new BoostersGUI<>(team, player, keviinTeams).getInventory());
            return false;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("buy")) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        String booster = args[1];
        Enhancement<?> enhancement = keviinTeams.getEnhancementList().get(booster);
        if (enhancement == null || enhancement.type != EnhancementType.BOOSTER) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noSuchBooster
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean success = keviinTeams.getTeamManager().UpdateEnhancement(team, booster, player);
        if (success) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().purchasedBooster
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%booster%", booster)
            ));
        }
        return success;
    }

}
