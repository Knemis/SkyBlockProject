package com.keviin.testplugin.managers;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends com.knemis.skyblock.skyblockcoreproject.teams.managers.CommandManager<TestTeam, User> {
    public CommandManager(SkyBlockProjectTeams<TestTeam, User> SkyBlockProjectTeams, String color, String command) {
        super(SkyBlockProjectTeams, color, command);
    }

    @Override
    public void noArgsDefault(@NotNull CommandSender commandSender) {
        commandSender.sendMessage("No Argument Method hit");
    }
}
