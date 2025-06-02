package com.keviin.keviinteams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.managers.TeamManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetHomeCommandTest {
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
    public void executeSetHomeCommand__NoTeam() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test sethome");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().dontHaveTeam
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetHomeCommand__NotInLand() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.SETHOME, true).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();

        serverMock.dispatchCommand(playerMock, "test sethome");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().notInTeamLand
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetHomeCommand__NoPermission() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.SETHOME, false).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();
        TeamManager.teamViaLocation = Optional.of(team);

        serverMock.dispatchCommand(playerMock, "test sethome");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().cannotSetHome
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetHomeCommand__Successful() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.SETHOME, true).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).build();
        TeamManager.teamViaLocation = Optional.of(team);

        serverMock.dispatchCommand(playerMock, "test sethome");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().homeSet
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%player%", playerMock.getName())
        ));
        playerMock.assertNoMoreSaid();
        assertEquals(team.getHome(), playerMock.getLocation());
    }
}