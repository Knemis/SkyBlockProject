package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.UserRank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.gui.PermissionsGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.RanksGUI;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PermissionsCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public PermissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        // if (args.length == 0) { // TODO: Uncomment when RanksGUI is refactored
            // player.openInventory(new RanksGUI<>(team, player, iridiumTeams).getInventory());
            // return false;
        // }
        // String rank = args[0];
        // for (Map.Entry<Integer, UserRank> userRank : iridiumTeams.getUserRanks().entrySet()) { // TODO: Uncomment when getUserRanks is available
            // if (!userRank.getValue().name.equalsIgnoreCase(rank)) continue;
            // player.openInventory(new PermissionsGUI<>(team, userRank.getKey(), player, iridiumTeams).getInventory()); // TODO: Uncomment when PermissionsGUI is refactored
            // return true;
        // }
        // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().invalidUserRank.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
        player.sendMessage("Permissions command needs to be reimplemented after refactoring."); // Placeholder
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        // return iridiumTeams.getUserRanks().values().stream().map(userRank -> userRank.name).collect(Collectors.toList()); // TODO: Uncomment when getUserRanks is available
        return Collections.emptyList(); // Placeholder
    }

}
