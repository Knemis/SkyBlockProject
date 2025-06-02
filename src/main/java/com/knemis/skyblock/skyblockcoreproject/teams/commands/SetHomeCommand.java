package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor

public class SetHomeCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public SetHomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notInTeamLand
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETHOME)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotSetHome
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        team.setHome(player.getLocation());
        SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member ->
                member.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().homeSet
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                ))
        );
        return true;
    }

}
