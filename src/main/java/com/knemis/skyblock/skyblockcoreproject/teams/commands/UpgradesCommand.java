package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.UpgradesGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class UpgradesCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public UpgradesCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new UpgradesGUI<>(team, player, skyblockTeams).getInventory());
            return true;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("buy")) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }
        String booster = args[1]; // Variable name is booster, but it's for an upgrade
        Enhancement<?> enhancement = skyblockTeams.getEnhancementList().get(booster);
        if (enhancement == null || enhancement.type != EnhancementType.UPGRADE) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noSuchUpgrade
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        TeamEnhancement teamEnhancement = skyblockTeams.getTeamManager().getTeamEnhancement(team, booster);
        if(enhancement.levels.get(teamEnhancement.getLevel() + 1) == null){
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().maxUpgradeLevelReached
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean success = skyblockTeams.getTeamManager().UpdateEnhancement(team, booster, player);
        if (success) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().purchasedUpgrade
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%upgrade%", booster) // Should probably be %upgrade% if it's an upgrade
            ));
        }
        return success;
    }
}
