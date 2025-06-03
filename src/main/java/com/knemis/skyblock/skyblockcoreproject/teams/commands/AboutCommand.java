package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;

@NoArgsConstructor
public class AboutCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes

    public AboutCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // TODO: Re-implement message sending with proper color handling and plugin information
        // sender.sendMessage(StringUtils.color("&7Plugin Name: " + SkyBlockProjectTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getName()));
        // sender.sendMessage(StringUtils.color("&7Plugin Version: " + SkyBlockProjectTeams.getCommandManager().getColor() + SkyBlockProjectTeams.getDescription().getVersion()));
        // sender.sendMessage(StringUtils.color("&7Plugin Author: " + SkyBlockProjectTeams.getCommandManager().getColor() + "Peaches_MLG"));
        // sender.sendMessage(StringUtils.color("&7Plugin Donations: " + SkyBlockProjectTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"));

        // HashSet<String> providerList = SkyBlockProjectTeams.getSupportManager().getProviderList();
        // if(!providerList.isEmpty())
            // sender.sendMessage(StringUtils.color("&7Detected Plugins Supported: " + SkyBlockProjectTeams.getCommandManager().getColor() + String.join(", ", providerList)));

        sender.sendMessage("About command needs to be reimplemented after StringUtils and core functionalities are refactored.");
        return true;
    }

}
