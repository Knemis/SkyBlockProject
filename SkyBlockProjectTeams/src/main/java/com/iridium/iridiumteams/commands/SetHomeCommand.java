package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor

public class SetHomeCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public SetHomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (keviinTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notInTeamLand
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETHOME)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotSetHome
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        team.setHome(player.getLocation());
        keviinTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member ->
                member.sendMessage(StringUtils.color(keviinTeams.getMessages().homeSet
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                ))
        );
        return true;
    }

}
