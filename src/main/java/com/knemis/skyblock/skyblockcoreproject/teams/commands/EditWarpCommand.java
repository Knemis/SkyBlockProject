package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class EditWarpCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public EditWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length < 2) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotManageWarps
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<TeamWarp> teamWarp = skyblockTeams.getTeamManager().getTeamWarp(team, args[0]); // TODO: Ensure TeamManager and TeamWarp are functional
        if (!teamWarp.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().unknownWarp
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        switch (args[1].toLowerCase()) { // Ensure case-insensitivity for subcommand
            case "icon":
                if (args.length != 3) {
                    player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                    return false;
                }

                Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(args[2]);
                if (!xMaterial.isPresent()) {
                    player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noSuchMaterial
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    ));
                    return false;
                }
                teamWarp.get().setIcon(xMaterial.get()); // TODO: Ensure TeamWarp set/save methods are functional
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().warpIconSet
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return true;
            case "description":
                if (args.length < 3) {
                    player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                    return false;
                }

                String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                teamWarp.get().setDescription(description); // TODO: Ensure TeamWarp set/save methods are functional
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().warpDescriptionSet
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return true;
            default:
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                return false;
        }
        // player.sendMessage("EditWarp command needs to be reimplemented after refactoring."); // Placeholder
        // return true; // Should be handled by switch cases
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        List<TeamWarp> teamWarps = skyblockTeams.getTeamManager().getTeamWarps(team); // TODO: Ensure TeamManager and TeamWarp are functional
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
        // return Collections.emptyList(); // Placeholder
    }
}
