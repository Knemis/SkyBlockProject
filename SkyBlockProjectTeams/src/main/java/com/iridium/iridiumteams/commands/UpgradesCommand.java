package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamEnhancement;
import com.keviin.keviinteams.enhancements.Enhancement;
import com.keviin.keviinteams.enhancements.EnhancementType;
import com.keviin.keviinteams.gui.UpgradesGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class UpgradesCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public UpgradesCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new UpgradesGUI<>(team, player, keviinTeams).getInventory());
            return true;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("buy")) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        String booster = args[1];
        Enhancement<?> enhancement = keviinTeams.getEnhancementList().get(booster);
        if (enhancement == null || enhancement.type != EnhancementType.UPGRADE) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noSuchUpgrade
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        TeamEnhancement teamEnhancement = keviinTeams.getTeamManager().getTeamEnhancement(team, booster);
        if(enhancement.levels.get(teamEnhancement.getLevel() + 1) == null){
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().maxUpgradeLevelReached
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean success = keviinTeams.getTeamManager().UpdateEnhancement(team, booster, player);
        if (success) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().purchasedUpgrade
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%upgrade%", booster)
            ));
        }
        return success;
    }
}
