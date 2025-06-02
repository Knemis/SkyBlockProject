package com.keviin.keviinteams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetPermissionCommandTest {

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
    public void executeSetPermissionCommand__NoTeam() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        serverMock.dispatchCommand(playerMock, "test setpermission");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().dontHaveTeam
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetPermissionCommand__InvalidSyntax() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();

        serverMock.dispatchCommand(playerMock, "test setpermission permission role invalid");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getCommands().setPermissionCommand.syntax
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetPermissionCommand__InvalidPermission() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test setpermission invalid member true");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().invalidPermission
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetPermissionCommand__InvalidRank() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test setpermission blockBreak invalid true");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().invalidUserRank
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetPermissionCommand__CannotChangePermissions() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test setpermission blockBreak member true");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().cannotChangePermissions
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeSetPermissionCommand__SuccessOwner() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.BLOCK_BREAK, false).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(team).withRank(Rank.OWNER.getId()).build();

        serverMock.dispatchCommand(playerMock, "test setpermission blockBreak member true");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().permissionSet
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%permission%", "blockBreak")
                .replace("%rank%", "Member")
                .replace("%allowed%", "true")
        ));
        playerMock.assertNoMoreSaid();
        assertTrue(TestPlugin.getInstance().getTeamManager().getTeamPermission(team, 1, PermissionType.BLOCK_BREAK.getPermissionKey()));
    }

    @Test
    public void executeSetPermissionCommand__ToggleSuccess() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.BLOCK_BREAK, false).build();
        PlayerMock playerMock = new UserBuilder(serverMock).setBypassing().withTeam(team).withRank(2).build();

        serverMock.dispatchCommand(playerMock, "test setpermission blockBreak member");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().permissionSet
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%permission%", "blockBreak")
                .replace("%rank%", "Member")
                .replace("%allowed%", "true")
        ));
        playerMock.assertNoMoreSaid();
        assertTrue(TestPlugin.getInstance().getTeamManager().getTeamPermission(team, 1, PermissionType.BLOCK_BREAK.getPermissionKey()));
    }

    @Test
    public void executeSetPermissionCommand__Success() {
        TestTeam team = new TeamBuilder().withPermission(1, PermissionType.BLOCK_BREAK, false).build();
        PlayerMock playerMock = new UserBuilder(serverMock).setBypassing().withTeam(team).withRank(2).build();

        serverMock.dispatchCommand(playerMock, "test setpermission blockBreak member true");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().permissionSet
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%permission%", "blockBreak")
                .replace("%rank%", "Member")
                .replace("%allowed%", "true")
        ));
        playerMock.assertNoMoreSaid();
        assertTrue(TestPlugin.getInstance().getTeamManager().getTeamPermission(team, 1, PermissionType.BLOCK_BREAK.getPermissionKey()));
    }

    @Test
    public void tabCompleteSetPermissionsCommand__FirstArgument() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        assertEquals(TestPlugin.getInstance().getPermissionList().keySet().stream().sorted().collect(Collectors.toList()), serverMock.getCommandTabComplete(playerMock, "test setpermission "));
    }

    @Test
    public void tabCompleteSetPermissionsCommand__SecondArgument() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        assertEquals(Arrays.asList("CoOwner", "Member", "Moderator", "Owner", "Visitor"), serverMock.getCommandTabComplete(playerMock, "test setpermission permission "));
    }

    @Test
    public void tabCompleteSetPermissionsCommand__ThirdArgument() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        assertEquals(Arrays.asList("false", "true"), serverMock.getCommandTabComplete(playerMock, "test setpermission permission Member "));
    }

    @Test
    public void tabCompleteSetPermissionsCommand__FourthArgument() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        assertEquals(Collections.emptyList(), serverMock.getCommandTabComplete(playerMock, "test setpermission permission Member true "));
    }
}