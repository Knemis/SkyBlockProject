package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor

public class SetPermissionCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public SetPermissionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 2 && (args.length != 3 || !args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<String> permission = SkyBlockProjectTeams.getPermissionList().keySet().stream()
                .filter(s -> s.equalsIgnoreCase(args[0]))
                .findFirst();
        if (!permission.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidPermission
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<Integer> rank = SkyBlockProjectTeams.getUserRanks().entrySet().stream()
                .filter(r -> r.getValue().name.equalsIgnoreCase(args[1]))
                .findAny()
                .map(Map.Entry::getKey);
        if (!rank.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidUserRank
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        boolean allowed = args.length == 2 ? !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, rank.get(), permission.get()) : args[2].equalsIgnoreCase("true");
        if ((user.getUserRank() <= rank.get() && user.getUserRank() != Rank.OWNER.getId()) || !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.CHANGE_PERMISSIONS) || rank.get() == Rank.OWNER.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangePermissions
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        SkyBlockProjectTeams.getTeamManager().setTeamPermission(team, rank.get(), permission.get(), allowed);
        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().permissionSet
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%permission%", permission.get())
                .replace("%rank%", WordUtils.capitalizeFully(args[1]))
                .replace("%allowed%", String.valueOf(allowed))
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        switch (args.length) {
            case 1:
                return new ArrayList<>(SkyBlockProjectTeams.getPermissionList().keySet());
            case 2:
                return SkyBlockProjectTeams.getUserRanks().values().stream().map(userRank -> userRank.name).collect(Collectors.toList());
            case 3:
                return Arrays.asList("true", "false");
            default:
                return Collections.emptyList();
        }
    }

}
