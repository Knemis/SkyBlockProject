package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class EditWarpCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public EditWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length < 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<TeamWarp> teamWarp = SkyBlockProjectTeams.getTeamManager().getTeamWarp(team, args[0]);
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownWarp
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        switch (args[1]) {
            case "icon":
                if (args.length != 3) {
                    player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                    return false;
                }

                Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(args[2]);
                if (!xMaterial.isPresent()) {
                    player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchMaterial
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
                    return false;
                }
                teamWarp.get().setIcon(xMaterial.get());
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpIconSet
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return true;
            case "description":
                if (args.length < 3) {
                    player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                    return false;
                }

                String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                teamWarp.get().setDescription(description);
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpDescriptionSet
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return true;
            default:
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        List<TeamWarp> teamWarps = SkyBlockProjectTeams.getTeamManager().getTeamWarps(team);
        switch (args.length) {
            case 1:
                return teamWarps.stream().map(TeamWarp::getName).collect(Collectors.toList());
            case 2:
                return Arrays.asList("icon", "description");
            case 3:
                if (args[1].equalsIgnoreCase("icon")) {
                    return Arrays.stream(XMaterial.values()).map(XMaterial::name).collect(Collectors.toList());
                }
            default:
                return Collections.emptyList();
        }
    }
}
