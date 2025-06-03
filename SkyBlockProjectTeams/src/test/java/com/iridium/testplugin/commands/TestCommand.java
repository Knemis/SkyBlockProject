package com.keviin.testplugin.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.commands.Command;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class TestCommand extends Command<TestTeam, User> {

    public static boolean hasCalled;

    public TestCommand() {
        super(List.of("test"), "Description", "/test test", "SkyBlockProjectTeams.test", 5);
        hasCalled = false;
    }

    @Override
    public boolean execute(User user, TestTeam team, String[] arguments, SkyBlockProjectTeams<TestTeam, User> SkyBlockProjectTeams) {
        hasCalled = true;
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<TestTeam, User> SkyBlockProjectTeams) {
        return Arrays.asList("c", "d", "A");
    }
}
