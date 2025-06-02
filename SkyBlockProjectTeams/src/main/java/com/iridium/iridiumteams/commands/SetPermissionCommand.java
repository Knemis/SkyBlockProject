package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor

public class SetPermissionCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public SetPermissionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 2 && (args.length != 3 || !args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<String> permission = keviinTeams.getPermissionList().keySet().stream()
                .filter(s -> s.equalsIgnoreCase(args[0]))
                .findFirst();
        if (!permission.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().invalidPermission
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<Integer> rank = keviinTeams.getUserRanks().entrySet().stream()
                .filter(r -> r.getValue().name.equalsIgnoreCase(args[1]))
                .findAny()
                .map(Map.Entry::getKey);
        if (!rank.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().invalidUserRank
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean allowed = args.length == 2 ? !keviinTeams.getTeamManager().getTeamPermission(team, rank.get(), permission.get()) : args[2].equalsIgnoreCase("true");
        if ((user.getUserRank() <= rank.get() && user.getUserRank() != Rank.OWNER.getId()) || !keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.CHANGE_PERMISSIONS) || rank.get() == Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotChangePermissions
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        keviinTeams.getTeamManager().setTeamPermission(team, rank.get(), permission.get(), allowed);
        player.sendMessage(StringUtils.color(keviinTeams.getMessages().permissionSet
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%permission%", permission.get())
                .replace("%rank%", WordUtils.capitalizeFully(args[1]))
                .replace("%allowed%", String.valueOf(allowed))
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        switch (args.length) {
            case 1:
                return new ArrayList<>(keviinTeams.getPermissionList().keySet());
            case 2:
                return keviinTeams.getUserRanks().values().stream().map(userRank -> userRank.name).collect(Collectors.toList());
            case 3:
                return Arrays.asList("true", "false");
            default:
                return Collections.emptyList();
        }
    }

}
