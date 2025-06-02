package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class FlyCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {

    @Getter
    String flyAnywherePermission;

    public FlyCommand(List<String> args, String description, String syntax, String permission, String flyAnywherePermission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.flyAnywherePermission = flyAnywherePermission;
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();

        boolean flight = !user.isFlying();
        if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable") && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                return false;
            }

            flight = args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("on");
        }

        if (!canFly(player, SkyBlockProjectTeams)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().flightNotActive.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        user.setFlying(flight);
        player.setAllowFlight(flight);
        player.setFlying(flight);

        if (flight) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().flightEnabled.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
        } else {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().flightDisabled.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
        }
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return true;
    }

    public boolean canFly(Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        return user.canFly(SkyBlockProjectTeams);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Arrays.asList("enable", "disable", "on", "off");
    }
}
