package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class RecalculateCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public RecalculateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (SkyBlockProjectTeams.isRecalculating()) {
            sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().calculationAlreadyInProcess
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
            return false;
        }

        int interval = SkyBlockProjectTeams.getConfiguration().forceRecalculateInterval;
        List<T> teams = SkyBlockProjectTeams.getTeamManager().getTeams();
        int seconds = (teams.size() * interval / 20) % 60;
        int minutes = (teams.size() * interval / 20) / 60;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!player.hasPermission(permission)) continue;
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().calculatingTeams
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%player%", sender.getName())
                    .replace("%minutes%", String.valueOf(minutes))
                    .replace("%seconds%", String.valueOf(seconds))
                    .replace("%amount%", String.valueOf(teams.size()))
            ));
        }
        SkyBlockProjectTeams.setRecalculating(true);
        return true;
    }

}
