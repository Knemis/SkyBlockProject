package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor

public class SetPermissionCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public SetPermissionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 2 && (args.length != 3 || !args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // Optional<String> permission = SkyBlockProjectTeams.getPermissionList().keySet().stream() // TODO: Uncomment when getPermissionList is available
                // .filter(s -> s.equalsIgnoreCase(args[0]))
                // .findFirst();
        // if (!permission.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidPermission // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // Optional<Integer> rank = SkyBlockProjectTeams.getUserRanks().entrySet().stream() // TODO: Uncomment when getUserRanks is available
                // .filter(r -> r.getValue().name.equalsIgnoreCase(args[1]))
                // .findAny()
                // .map(Map.Entry::getKey);
        // if (!rank.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidUserRank // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // boolean allowed = args.length == 2 ? !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, rank.get(), permission.get()) : args[2].equalsIgnoreCase("true"); // TODO: Uncomment when TeamManager, rank and permission are available
        // if ((user.getUserRank() <= rank.get() && user.getUserRank() != Rank.OWNER.getId()) || !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.CHANGE_PERMISSIONS) || rank.get() == Rank.OWNER.getId()) { // TODO: Uncomment when TeamManager and rank are available
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangePermissions // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // SkyBlockProjectTeams.getTeamManager().setTeamPermission(team, rank.get(), permission.get(), allowed); // TODO: Uncomment when TeamManager, rank, permission and allowed are available
        // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().permissionSet // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // .replace("%permission%", permission.get())
                // .replace("%rank%", WordUtils.capitalizeFully(args[1]))
                // .replace("%allowed%", String.valueOf(allowed))
        // ));
        player.sendMessage("SetPermission command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // switch (args.length) { // TODO: Uncomment when getPermissionList and getUserRanks are available
            // case 1:
                // return new ArrayList<>(SkyBlockProjectTeams.getPermissionList().keySet());
            // case 2:
                // return SkyBlockProjectTeams.getUserRanks().values().stream().map(com.knemis.skyblock.skyblockcoreproject.teams.UserRank::name).collect(Collectors.toList()); // Adjusted to new UserRank path
            // case 3:
                // return Arrays.asList("true", "false");
            // default:
                // return Collections.emptyList();
        // }
        return Collections.emptyList(); // Placeholder
    }

}
