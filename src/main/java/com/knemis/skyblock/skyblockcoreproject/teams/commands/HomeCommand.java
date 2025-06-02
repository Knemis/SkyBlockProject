package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class HomeCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public HomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        Location home = team.getHome();
        if (home == null) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().homeNotSet
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (keviinTeams.getTeamManager().getTeamViaLocation(home).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().homeNotInTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (keviinTeams.getTeamManager().teleport(player, home, team)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teleportingHome
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
        }
        return true;
    }

}
