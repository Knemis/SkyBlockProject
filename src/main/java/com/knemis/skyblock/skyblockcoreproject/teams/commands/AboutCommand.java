package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;

@NoArgsConstructor
public class AboutCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    public AboutCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> keviinTeams) {
        sender.sendMessage(StringUtils.color("&7Plugin Name: " + keviinTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getName()));
        sender.sendMessage(StringUtils.color("&7Plugin Version: " + keviinTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getVersion()));
        sender.sendMessage(StringUtils.color("&7Plugin Author: " + keviinTeams.getCommandManager().getColor() + "Peaches_MLG"));
        sender.sendMessage(StringUtils.color("&7Plugin Donations: " + keviinTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"));

        HashSet<String> providerList = keviinTeams.getSupportManager().getProviderList();
        if(!providerList.isEmpty())
            sender.sendMessage(StringUtils.color("&7Detected Plugins Supported: " + keviinTeams.getCommandManager().getColor() + String.join(", ", providerList)));

        return true;
    }

}
