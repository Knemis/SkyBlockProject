package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct for now
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;

@NoArgsConstructor
public class AboutCommand<T extends Team, U extends User<T>> extends Command<T, U> {

    public AboutCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        // TODO: Re-implement message sending with proper color handling and plugin information
        sender.sendMessage(StringUtils.color("&7Plugin Name: " + skyblockTeams.getCommandManager().getColor() + skyblockTeams.getDescription().getName()));
        sender.sendMessage(StringUtils.color("&7Plugin Version: " + skyblockTeams.getCommandManager().getColor() + skyblockTeams.getDescription().getVersion()));
        sender.sendMessage(StringUtils.color("&7Plugin Author: " + skyblockTeams.getCommandManager().getColor() + "Peaches_MLG")); // Assuming Peaches_MLG is a placeholder or previous author
        sender.sendMessage(StringUtils.color("&7Plugin Donations: " + skyblockTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG")); // Assuming Peaches_MLG is a placeholder or previous author

        HashSet<String> providerList = skyblockTeams.getSupportManager().getProviderList(); //TODO: Ensure getSupportManager and getProviderList are functional
        if(!providerList.isEmpty())
            sender.sendMessage(StringUtils.color("&7Detected Plugins Supported: " + skyblockTeams.getCommandManager().getColor() + String.join(", ", providerList)));

        // sender.sendMessage("About command needs to be reimplemented after StringUtils and core functionalities are refactored.");
        return true;
    }

}
