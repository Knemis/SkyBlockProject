package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.UserRank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.PermissionsGUI; // TODO: Update to actual PermissionsGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.RanksGUI; // TODO: Update to actual RanksGUI class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PermissionsCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public PermissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (args.length == 0) { // TODO: Uncomment when RanksGUI is refactored
            // player.openInventory(new RanksGUI<>(team, player, SkyBlockProjectTeams).getInventory());
            // return false;
        // }
        // String rank = args[0];
        // for (Map.Entry<Integer, UserRank> userRank : SkyBlockProjectTeams.getUserRanks().entrySet()) { // TODO: Uncomment when getUserRanks is available
            // if (!userRank.getValue().name.equalsIgnoreCase(rank)) continue;
            // player.openInventory(new PermissionsGUI<>(team, userRank.getKey(), player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when PermissionsGUI is refactored
            // return true;
        // }
        // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidUserRank.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
        player.sendMessage("Permissions command needs to be reimplemented after refactoring."); // Placeholder
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // return SkyBlockProjectTeams.getUserRanks().values().stream().map(userRank -> userRank.name).collect(Collectors.toList()); // TODO: Uncomment when getUserRanks is available
        return Collections.emptyList(); // Placeholder
    }

}
