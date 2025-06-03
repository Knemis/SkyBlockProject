package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.List;

@NoArgsConstructor
public class ReloadCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public ReloadCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(CommandSender sender, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        // iridiumTeams.loadConfigs(); // TODO: Uncomment when loadConfigs is available
        // sender.sendMessage(StringUtils.color(iridiumTeams.getMessages().reloaded.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
        sender.sendMessage("Reload command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
