package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class HomeCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public HomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        Location home = team.getHome(); // TODO: Ensure Team has getHome and it's functional
        if (home == null) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().homeNotSet
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (skyblockTeams.getTeamManager().getTeamViaLocation(home).map(T::getId).orElse(0) != team.getId()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().homeNotInTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (skyblockTeams.getTeamManager().teleport(player, home, team)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teleportingHome
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
        }
        // player.sendMessage("Home command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
