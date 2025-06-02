package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class RecalculateCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public RecalculateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, keviinTeams<T, U> keviinTeams) {
        if (keviinTeams.isRecalculating()) {
            sender.sendMessage(StringUtils.color(keviinTeams.getMessages().calculationAlreadyInProcess
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix))
            );
            return false;
        }

        int interval = keviinTeams.getConfiguration().forceRecalculateInterval;
        List<T> teams = keviinTeams.getTeamManager().getTeams();
        int seconds = (teams.size() * interval / 20) % 60;
        int minutes = (teams.size() * interval / 20) / 60;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!player.hasPermission(permission)) continue;
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().calculatingTeams
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%player%", sender.getName())
                    .replace("%minutes%", String.valueOf(minutes))
                    .replace("%seconds%", String.valueOf(seconds))
                    .replace("%amount%", String.valueOf(teams.size()))
            ));
        }
        keviinTeams.setRecalculating(true);
        return true;
    }

}
