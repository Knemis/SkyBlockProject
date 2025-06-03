package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.knemis.skyblock.skyblockcoreproject.teams.UserBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.TopGUI;
import com.keviin.testplugin.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TopCommandTest {

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
    public void executeTopCommand__Success() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        serverMock.dispatchCommand(playerMock, "test top");
        assertTrue(playerMock.getOpenInventory().getTopInventory().getHolder() instanceof TopGUI<?,?>);
    }

}