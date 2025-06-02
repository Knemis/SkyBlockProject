package com.keviin.testplugin.commands;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.commands.Command;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class TestCommand extends Command<TestTeam, User> {

    public static boolean hasCalled;

    public TestCommand() {
        super(List.of("test"), "Description", "/test test", "keviinteams.test", 5);
        hasCalled = false;
    }

    @Override
    public boolean execute(User user, TestTeam team, String[] arguments, keviinTeams<TestTeam, User> keviinTeams) {
        hasCalled = true;
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<TestTeam, User> keviinTeams) {
        return Arrays.asList("c", "d", "A");
    }
}
