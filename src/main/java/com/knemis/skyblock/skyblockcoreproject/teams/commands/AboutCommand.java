package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;

@NoArgsConstructor
public class AboutCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes

    public AboutCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        // TODO: Re-implement message sending with proper color handling and plugin information
        // sender.sendMessage(StringUtils.color("&7Plugin Name: " + iridiumTeams.getCommandManager().getColor() + iridiumTeams.getDescription().getName()));
        // sender.sendMessage(StringUtils.color("&7Plugin Version: " + iridiumTeams.getCommandManager().getColor() + iridiumTeams.getDescription().getVersion()));
        // sender.sendMessage(StringUtils.color("&7Plugin Author: " + iridiumTeams.getCommandManager().getColor() + "Peaches_MLG"));
        // sender.sendMessage(StringUtils.color("&7Plugin Donations: " + iridiumTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"));

        // HashSet<String> providerList = iridiumTeams.getSupportManager().getProviderList();
        // if(!providerList.isEmpty())
            // sender.sendMessage(StringUtils.color("&7Detected Plugins Supported: " + iridiumTeams.getCommandManager().getColor() + String.join(", ", providerList)));

        sender.sendMessage("About command needs to be reimplemented after StringUtils and core functionalities are refactored.");
        return true;
    }

}
