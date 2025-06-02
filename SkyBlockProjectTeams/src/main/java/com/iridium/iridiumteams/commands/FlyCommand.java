package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class FlyCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    @Getter
    String flyAnywherePermission;

    public FlyCommand(List<String> args, String description, String syntax, String permission, String flyAnywherePermission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.flyAnywherePermission = flyAnywherePermission;
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();

        boolean flight = !user.isFlying();
        if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable") && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
                return false;
            }

            flight = args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("on");
        }

        if (!canFly(player, keviinTeams)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().flightNotActive.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        user.setFlying(flight);
        player.setAllowFlight(flight);
        player.setFlying(flight);

        if (flight) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().flightEnabled.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
        } else {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().flightDisabled.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
        }
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender, keviinTeams<T, U> keviinTeams) {
        return true;
    }

    public boolean canFly(Player player, keviinTeams<T, U> keviinTeams) {
        U user = keviinTeams.getUserManager().getUser(player);
        return user.canFly(keviinTeams);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Arrays.asList("enable", "disable", "on", "off");
    }
}
