package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class SetWarpCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public SetWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        if (!LocationUtils.isSafe(player.getLocation(), SkyBlockProjectTeams)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notSafe
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notInTeamLand
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        WarpsEnhancementData data = SkyBlockProjectTeams.getEnhancements().warpsEnhancement.levels.get(SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "warps").getLevel());
        if (SkyBlockProjectTeams.getTeamManager().getTeamWarps(team).size() >= (data == null ? 0 : data.warps)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpLimitReached
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (SkyBlockProjectTeams.getTeamManager().getTeamWarp(team, args[0]).isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpAlreadyExists
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        SkyBlockProjectTeams.getTeamManager().createWarp(team, player.getUniqueId(), player.getLocation(), args[0], args.length == 2 ? args[1] : null);
        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().createdWarp
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%name%", args[0])
        ));

        return true;
    }

}
