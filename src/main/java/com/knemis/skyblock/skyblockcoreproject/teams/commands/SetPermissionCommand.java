package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Permission; // Added for Permission class
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.UserRank; // Added for UserRank class
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public class SetPermissionCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public SetPermissionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 2 && (args.length != 3 || !args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        Optional<String> permissionKey = skyblockTeams.getPermissionList().entrySet().stream() // TODO: Ensure getPermissionList is functional
                .filter(entry -> entry.getKey().equalsIgnoreCase(args[0]) || entry.getValue().getDisplayName().equalsIgnoreCase(args[0]))
                .map(Map.Entry::getKey)
                .findFirst();

        if (!permissionKey.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().invalidPermission
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<Integer> rankId = skyblockTeams.getUserRanks().entrySet().stream() // TODO: Ensure getUserRanks is functional
                .filter(r -> r.getValue().name.equalsIgnoreCase(args[1]))
                .findAny()
                .map(Map.Entry::getKey);

        if (!rankId.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().invalidUserRank
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean allowed = args.length == 2 ? !skyblockTeams.getTeamManager().getTeamPermission(team, rankId.get(), permissionKey.get()) : args[2].equalsIgnoreCase("true"); // TODO: Ensure TeamManager is functional
        if ((user.getUserRank() <= rankId.get() && user.getUserRank() != Rank.OWNER.getId()) || !skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.CHANGE_PERMISSIONS) || rankId.get() == Rank.OWNER.getId()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotChangePermissions
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        skyblockTeams.getTeamManager().setTeamPermission(team, rankId.get(), permissionKey.get(), allowed); // TODO: Ensure TeamManager is functional
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().permissionSet
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%permission%", WordUtils.capitalizeFully(permissionKey.get().toLowerCase().replace("_", " ")))
                .replace("%rank%", WordUtils.capitalizeFully(args[1]))
                .replace("%allowed%", String.valueOf(allowed))
        ));
        // player.sendMessage("SetPermission command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        switch (args.length) {
            case 1:
                return new ArrayList<>(skyblockTeams.getPermissionList().keySet()); // TODO: Ensure getPermissionList is functional
            case 2:
                return skyblockTeams.getUserRanks().values().stream().map(UserRank::getName).collect(Collectors.toList()); // TODO: Ensure getUserRanks is functional
            case 3:
                return Arrays.asList("true", "false");
            default:
                return Collections.emptyList();
        }
        // return Collections.emptyList(); // Placeholder
    }

}
