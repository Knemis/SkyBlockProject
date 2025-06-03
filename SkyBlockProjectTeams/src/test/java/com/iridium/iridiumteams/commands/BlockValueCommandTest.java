package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.TeamBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.UserBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValueGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValuesTypeSelectorGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.SpawnerValueGUI;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BlockValueCommandTest {

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
    public void executeBlockValueCommand__WithNoTeam() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().dontHaveTeam
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeBlockValueCommand__WhenSpecifiedTeamDoesntExist() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues Team1");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().teamDoesntExistByName
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeBlockValueCommand__WithTeam() {
        TestTeam testTeam = new TeamBuilder().withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues");

        playerMock.assertNoMoreSaid();
        assertInstanceOf(BlockValuesTypeSelectorGUI.class, playerMock.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    public void executeBlockValueCommand__WithTeam__Blocks() {
        TestTeam testTeam = new TeamBuilder().withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues blocks");

        playerMock.assertNoMoreSaid();
        assertInstanceOf(BlockValueGUI.class, playerMock.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    public void executeBlockValueCommand__WithTeam__Spawners() {
        TestTeam testTeam = new TeamBuilder().withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues spawners");

        playerMock.assertNoMoreSaid();
        assertInstanceOf(SpawnerValueGUI.class, playerMock.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    public void executeBlockValueCommand__WithoutTeam__Blocks__WhenPrivate() {
        TestTeam testTeam = new TeamBuilder("TestTeam").withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues TestTeam blocks");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().teamIsPrivate
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeBlockValueCommand__WithoutTeam__Spawners__WhenPrivate() {
        TestTeam testTeam = new TeamBuilder("TestTeam").withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues TestTeam spawners");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().teamIsPrivate
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeBlockValueCommand__WithoutTeam__Blocks() {
        TestTeam testTeam = new TeamBuilder("TestTeam").build();
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues TestTeam blocks");

        playerMock.assertNoMoreSaid();
        assertInstanceOf(BlockValueGUI.class, playerMock.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    public void executeBlockValueCommand__WithoutTeam__Spawners() {
        TestTeam testTeam = new TeamBuilder("TestTeam").build();
        PlayerMock playerMock = new UserBuilder(serverMock).build();

        serverMock.dispatchCommand(playerMock, "test blockvalues TestTeam spawners");

        playerMock.assertNoMoreSaid();
        assertInstanceOf(SpawnerValueGUI.class, playerMock.getOpenInventory().getTopInventory().getHolder());
    }

}