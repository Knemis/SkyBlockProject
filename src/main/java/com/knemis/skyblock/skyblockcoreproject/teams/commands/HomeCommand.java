package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class HomeCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public HomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        Location home = team.getHome();
        if (home == null) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().homeNotSet
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(home).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().homeNotInTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (SkyBlockProjectTeams.getTeamManager().teleport(player, home, team)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teleportingHome
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
        }
        return true;
    }

}
