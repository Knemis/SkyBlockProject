package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class RecalculateCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public RecalculateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        // if (iridiumTeams.isRecalculating()) { // TODO: Uncomment when isRecalculating is available
            // sender.sendMessage(StringUtils.color(iridiumTeams.getMessages().calculationAlreadyInProcess // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
            // );
            // return false;
        // }

        // int interval = iridiumTeams.getConfiguration().forceRecalculateInterval; // TODO: Uncomment when Configuration is refactored
        // List<T> teams = iridiumTeams.getTeamManager().getTeams(); // TODO: Uncomment when TeamManager is refactored
        // int seconds = (teams.size() * interval / 20) % 60;
        // int minutes = (teams.size() * interval / 20) / 60;
        // for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            // if (!player.hasPermission(permission)) continue;
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().calculatingTeams // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                    // .replace("%player%", sender.getName())
                    // .replace("%minutes%", String.valueOf(minutes))
                    // .replace("%seconds%", String.valueOf(seconds))
                    // .replace("%amount%", String.valueOf(teams.size()))
            // ));
        // }
        // iridiumTeams.setRecalculating(true); // TODO: Uncomment when setRecalculating is available
        sender.sendMessage("Recalculate command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
