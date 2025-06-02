package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;

@NoArgsConstructor
public class AboutCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {

    public AboutCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        sender.sendMessage(StringUtils.color("&7Plugin Name: " + SkyBlockProjectTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getName()));
        sender.sendMessage(StringUtils.color("&7Plugin Version: " + SkyBlockProjectTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getVersion()));
        sender.sendMessage(StringUtils.color("&7Plugin Author: " + SkyBlockProjectTeams.getCommandManager().getColor() + "Peaches_MLG"));
        sender.sendMessage(StringUtils.color("&7Plugin Donations: " + SkyBlockProjectTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"));

        HashSet<String> providerList = SkyBlockProjectTeams.getSupportManager().getProviderList();
        if (!providerList.isEmpty())
            sender.sendMessage(StringUtils.color("&7Detected Plugins Supported: " + SkyBlockProjectTeams.getCommandManager().getColor() + String.join(", ", providerList)));

        return true;
    }

}
