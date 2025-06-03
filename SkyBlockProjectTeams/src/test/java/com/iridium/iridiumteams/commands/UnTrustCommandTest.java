package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.TeamBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnTrustCommandTest {

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
    public void executeUnTrustCommand__BadSyntax() {
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(new TeamBuilder().build()).build();

        serverMock.dispatchCommand(playerMock, "test untrust");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getCommands().unTrustCommand.syntax
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUnTrustCommand__NoTeam() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test untrust Player");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().dontHaveTeam.replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUnTrustCommand__PlayerNoTrust() {
        TestTeam team = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();
        PlayerMock otherPlayer = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test untrust " + otherPlayer.getName());
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().noActiveTrust.replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUnTrustCommand__Successful() {
        TestTeam team = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();
        PlayerMock otherPlayer = new UserBuilder(serverMock).withTrust(team).build();

        serverMock.dispatchCommand(playerMock, "test untrust " + otherPlayer.getName());
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().teamTrustRevoked
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%player%", otherPlayer.getName())
        ));
        playerMock.assertNoMoreSaid();
        assertFalse(TestPlugin.getInstance().getTeamManager().getTeamInvite(team, TestPlugin.getInstance().getUserManager().getUser(otherPlayer)).isPresent());
    }

    @Test
    public void tabCompleteUnTrustCommand(){
        PlayerMock playerMock1 = new UserBuilder(serverMock).build();
        PlayerMock playerMock2 = new UserBuilder(serverMock).build();

        assertEquals(Arrays.asList(playerMock1.getName(), playerMock2.getName()), serverMock.getCommandTabComplete(playerMock1, "test untrust "));
    }

}