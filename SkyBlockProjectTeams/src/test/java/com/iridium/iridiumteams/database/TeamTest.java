package com.keviin.keviinteams.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamTest {

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
    public void setExperience_GivesReward_WhenLevelUp() {
        TestTeam testTeam = new TeamBuilder().build();
        testTeam.setExperience(10);
        assertEquals(1, TestPlugin.getInstance().getTeamManager().getTeamRewards(testTeam).size());
    }
    @Test
    public void setExperience_DoesntGiveSameRewardTwice() {
        TestTeam testTeam = new TeamBuilder().build();
        testTeam.setExperience(10);
        testTeam.setExperience(0);
        testTeam.setExperience(10);
        assertEquals(1, TestPlugin.getInstance().getTeamManager().getTeamRewards(testTeam).size());
    }
    @Test
    public void setExperience_DoesntGiveReward_OfSameLevel() {
        TestTeam testTeam = new TeamBuilder().build();
        testTeam.setExperience(10);
        testTeam.setExperience(11);
        assertEquals(1, TestPlugin.getInstance().getTeamManager().getTeamRewards(testTeam).size());
    }

}