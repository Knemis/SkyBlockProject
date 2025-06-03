package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class HomeCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public HomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // Location home = team.getHome(); // TODO: Uncomment when Team is refactored
        // if (home == null) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().homeNotSet // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(home).map(T::getId).orElse(0) != team.getId()) { // TODO: Uncomment when TeamManager and Team are refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().homeNotInTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (SkyBlockProjectTeams.getTeamManager().teleport(player, home, team)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teleportingHome // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
        // }
        player.sendMessage("Home command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
