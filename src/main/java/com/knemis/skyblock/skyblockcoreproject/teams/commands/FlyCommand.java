package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class FlyCommand<T extends Team, U extends User<T>> extends Command<T, U> {

    @Getter
    String flyAnywherePermission;

    public FlyCommand(List<String> args, String description, String syntax, String permission, String flyAnywherePermission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.flyAnywherePermission = flyAnywherePermission;
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();

        boolean flight = !user.isFlying();
        if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable") && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                // player.sendMessage("Invalid syntax."); // Placeholder
                return false;
            }

            flight = args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("on");
        }

        if (!canFly(player, skyblockTeams)) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().flightNotActive.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        user.setFlying(flight);
        player.setAllowFlight(flight);
        player.setFlying(flight);

        if (flight) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().flightEnabled.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Flight enabled."); // Placeholder
        } else {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().flightDisabled.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Flight disabled."); // Placeholder
        }
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender, SkyBlockTeams<T, U> skyblockTeams) { // Changed IridiumTeams to SkyBlockTeams
        return true;
    }

    public boolean canFly(Player player, SkyBlockTeams<T, U> skyblockTeams) { // Changed IridiumTeams to SkyBlockTeams
        U user = skyblockTeams.getUserManager().getUser(player); // TODO: Ensure UserManager is functional
        return user.canFly(skyblockTeams);
        // return false; // Placeholder
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) { // Changed IridiumTeams to SkyBlockTeams
        return Arrays.asList("enable", "disable", "on", "off");
    }
}
