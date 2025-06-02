package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.List;

@NoArgsConstructor
public class ReloadCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public ReloadCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, keviinTeams<T, U> keviinTeams) {
        keviinTeams.loadConfigs();
        sender.sendMessage(StringUtils.color(keviinTeams.getMessages().reloaded.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
        return true;
    }

}
