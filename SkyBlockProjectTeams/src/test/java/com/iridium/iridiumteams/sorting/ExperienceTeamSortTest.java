package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.knemis.skyblock.skyblockcoreproject.teams.TeamBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceTeamSortTest {

    private ServerMock serverMock;

    @BeforeEach
    public void setup() {
        this.serverMock = MockBukkit.mock();
        MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void getSortedTeams() {
        TestTeam teamA = new TeamBuilder().withExperience(100).build();
        TestTeam teamB = new TeamBuilder().withExperience(27).build();
        TestTeam teamC = new TeamBuilder().withExperience(300).build();
        assertEquals(Arrays.asList(teamC, teamA, teamB), new ExperienceTeamSort<TestTeam>().getSortedTeams(TestPlugin.getInstance()));
    }
}