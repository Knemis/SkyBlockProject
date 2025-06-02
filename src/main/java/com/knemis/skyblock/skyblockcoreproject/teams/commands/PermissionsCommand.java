package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.UserRank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.PermissionsGUI;
import com.keviin.keviinteams.gui.RanksGUI;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PermissionsCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public PermissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new RanksGUI<>(team, player, keviinTeams).getInventory());
            return false;
        }
        String rank = args[0];
        for (Map.Entry<Integer, UserRank> userRank : keviinTeams.getUserRanks().entrySet()) {
            if (!userRank.getValue().name.equalsIgnoreCase(rank)) continue;
            player.openInventory(new PermissionsGUI<>(team, userRank.getKey(), player, keviinTeams).getInventory());
            return true;
        }
        player.sendMessage(StringUtils.color(keviinTeams.getMessages().invalidUserRank.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return keviinTeams.getUserRanks().values().stream().map(userRank -> userRank.name).collect(Collectors.toList());
    }

}
