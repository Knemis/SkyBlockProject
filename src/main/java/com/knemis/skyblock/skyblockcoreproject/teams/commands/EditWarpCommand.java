package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.cryptomorin.xseries.XMaterial;
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp; // TODO: Update to actual TeamWarp class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class EditWarpCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public EditWarpCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length < 2) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.MANAGE_WARPS)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotManageWarps // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // Optional<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp> teamWarp = SkyBlockProjectTeams.getTeamManager().getTeamWarp(team, args[0]); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // if (!teamWarp.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownWarp // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // switch (args[1]) { // TODO: Uncomment when teamWarp is available
            // case "icon":
                // if (args.length != 3) {
                    // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                    // return false;
                // }

                // Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(args[2]);
                // if (!xMaterial.isPresent()) {
                    // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchMaterial // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // ));
                    // return false;
                // }
                // teamWarp.get().setIcon(xMaterial.get());
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpIconSet // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return true;
            // case "description":
                // if (args.length < 3) {
                    // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                    // return false;
                // }

                // String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                // teamWarp.get().setDescription(description);
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().warpDescriptionSet // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return true;
            // default:
                // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                // return false;
        // }
        player.sendMessage("EditWarp command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // List<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp> teamWarps = SkyBlockProjectTeams.getTeamManager().getTeamWarps(team); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // switch (args.length) { // TODO: Uncomment when teamWarps is available
            // case 1:
                // return teamWarps.stream().map(com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp::getName).collect(Collectors.toList());
            // case 2:
                // return Arrays.asList("icon", "description");
            // case 3:
                // if (args[1].equalsIgnoreCase("icon")) {
                    // return Arrays.stream(XMaterial.values()).map(XMaterial::name).collect(Collectors.toList());
                // }
            // default:
                // return Collections.emptyList();
        // }
        return Collections.emptyList(); // Placeholder
    }
}
