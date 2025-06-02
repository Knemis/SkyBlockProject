package com.keviin.testplugin.managers;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends com.keviin.keviinteams.managers.CommandManager<TestTeam, User> {
    public CommandManager(keviinTeams<TestTeam, User> keviinTeams, String color, String command) {
        super(keviinTeams, color, command);
    }

    @Override
    public void noArgsDefault(@NotNull CommandSender commandSender) {
        commandSender.sendMessage("No Argument Method hit");
    }
}
