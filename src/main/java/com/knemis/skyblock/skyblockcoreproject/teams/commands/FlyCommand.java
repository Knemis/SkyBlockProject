package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class FlyCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes

    @Getter
    String flyAnywherePermission;

    public FlyCommand(List<String> args, String description, String syntax, String permission, String flyAnywherePermission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.flyAnywherePermission = flyAnywherePermission;
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();

        boolean flight = !user.isFlying();
        if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable") && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
                // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                player.sendMessage("Invalid syntax."); // Placeholder
                return false;
            }

            flight = args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("on");
        }

        // if (!canFly(player, iridiumTeams)) { // TODO: Uncomment when canFly is refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().flightNotActive.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        user.setFlying(flight);
        player.setAllowFlight(flight);
        player.setFlying(flight);

        if (flight) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().flightEnabled.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Flight enabled."); // Placeholder
        } else {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().flightDisabled.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Flight disabled."); // Placeholder
        }
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender, IridiumTeams<T, U> iridiumTeams) {
        return true;
    }

    public boolean canFly(Player player, IridiumTeams<T, U> iridiumTeams) {
        // U user = iridiumTeams.getUserManager().getUser(player); // TODO: Uncomment when UserManager is refactored
        // return user.canFly(iridiumTeams); // TODO: Uncomment when user is available
        return false; // Placeholder
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Arrays.asList("enable", "disable", "on", "off");
    }
}
