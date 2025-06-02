package com.keviin.keviinteams.listeners;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerExpChangeListenerTest {

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
    public void onPlayerExpChange__BoosterNotActive() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(playerMock, 10);
        serverMock.getPluginManager().callEvent(playerExpChangeEvent);

        assertEquals(10, playerExpChangeEvent.getAmount());
    }

    @Test
    public void onPlayerExpChange__BoosterActive() {
        TestTeam team = new TeamBuilder().withEnhancement("experience", 1).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();

        PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(playerMock, 10);
        serverMock.getPluginManager().callEvent(playerExpChangeEvent);

        assertEquals(15, playerExpChangeEvent.getAmount());
    }
}