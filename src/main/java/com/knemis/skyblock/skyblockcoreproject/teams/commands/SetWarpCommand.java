package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.enhancements.WarpsEnhancementData;
import com.keviin.keviinteams.utils.LocationUtils;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class SetWarpCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public SetWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (!LocationUtils.isSafe(player.getLocation(), keviinTeams)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notSafe
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (keviinTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notInTeamLand
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        WarpsEnhancementData data = keviinTeams.getEnhancements().warpsEnhancement.levels.get(keviinTeams.getTeamManager().getTeamEnhancement(team, "warps").getLevel());
        if (keviinTeams.getTeamManager().getTeamWarps(team).size() >= (data == null ? 0 : data.warps)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().warpLimitReached
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (keviinTeams.getTeamManager().getTeamWarp(team, args[0]).isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().warpAlreadyExists
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        keviinTeams.getTeamManager().createWarp(team, player.getUniqueId(), player.getLocation(), args[0], args.length == 2 ? args[1] : null);
        player.sendMessage(StringUtils.color(keviinTeams.getMessages().createdWarp
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%name%", args[0])
        ));

        return true;
    }

}
