package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData; // TODO: Update to actual WarpsEnhancementData class
// import com.knemis.skyblock.skyblockcoreproject.teams.utils.LocationUtils; // TODO: Update to actual LocationUtils class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class SetWarpCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public SetWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (!LocationUtils.isSafe(player.getLocation(), SkyBlockProjectTeams)) { // TODO: Uncomment when LocationUtils is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notSafe // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) { // TODO: Uncomment when TeamManager and Team are refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notInTeamLand // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotManageWarps // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData data = SkyBlockProjectTeams.getEnhancements().warpsEnhancement.levels.get(SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "warps").getLevel()); // TODO: Uncomment when Enhancements and TeamManager are refactored
        // if (SkyBlockProjectTeams.getTeamManager().getTeamWarps(team).size() >= (data == null ? 0 : data.warps)) { // TODO: Uncomment when TeamManager and data are available
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpLimitReached // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // if (SkyBlockProjectTeams.getTeamManager().getTeamWarp(team, args[0]).isPresent()) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpAlreadyExists // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // SkyBlockProjectTeams.getTeamManager().createWarp(team, player.getUniqueId(), player.getLocation(), args[0], args.length == 2 ? args[1] : null); // TODO: Uncomment when TeamManager is refactored
        // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().createdWarp // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // .replace("%name%", args[0])
        // ));
        player.sendMessage("SetWarp command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
