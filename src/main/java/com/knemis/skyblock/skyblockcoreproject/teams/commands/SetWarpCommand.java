package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.LocationUtils; // Assuming moved to core utils
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class SetWarpCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public SetWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        if (!LocationUtils.isSafe(player.getLocation(), skyblockTeams)) { // TODO: Ensure LocationUtils is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().notSafe
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (skyblockTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().notInTeamLand
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        WarpsEnhancementData data = skyblockTeams.getEnhancements().warpsEnhancement.levels.get(skyblockTeams.getTeamManager().getTeamEnhancement(team, "warps").getLevel()); // TODO: Ensure Enhancements and TeamManager are functional
        if (skyblockTeams.getTeamManager().getTeamWarps(team).size() >= (data == null ? 0 : data.warps)) { // TODO: Ensure TeamManager and data are available
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().warpLimitReached
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (skyblockTeams.getTeamManager().getTeamWarp(team, args[0]).isPresent()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().warpAlreadyExists
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        skyblockTeams.getTeamManager().createWarp(team, player.getUniqueId(), player.getLocation(), args[0], args.length == 2 ? args[1] : null); // TODO: Ensure TeamManager is functional
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().createdWarp
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%name%", args[0])
        ));
        // player.sendMessage("SetWarp command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
