package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.TeamBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.UserBuilder;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.UpgradesGUI;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.api.EnhancementUpdateEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradesCommandTest {

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
    public void executeUpgradesCommand__NoTeam() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        serverMock.dispatchCommand(playerMock, "test upgrades");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().dontHaveTeam
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUpgradesCommand__Success() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test upgrades");
        assertTrue(playerMock.getOpenInventory().getTopInventory().getHolder() instanceof UpgradesGUI<?, ?>);
    }

    @Test
    public void executeUpgradesCommand__Buy__InvalidSyntax() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test upgrades bad syntax");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getCommands().upgradesCommand.syntax
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUpgradesCommand__Buy__NotUpgrade() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test upgrades buy farming");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().noSuchUpgrade
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUpgradesCommand__Buy__LowLevel() {
        TestTeam testTeam = new TeamBuilder().build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test upgrades buy haste");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().notHighEnoughLevel
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%level%", "5")
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUpgradesCommand__Buy__NotEnoughMoney() {
        TestTeam testTeam = new TeamBuilder().withLevel(5).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        serverMock.dispatchCommand(playerMock, "test upgrades buy haste");
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().notEnoughMoney
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%level%", "5")
        ));
        playerMock.assertNoMoreSaid();
    }

    @Test
    public void executeUpgradesCommand__Buy__MaxLevel() {
        TestTeam testTeam = new TeamBuilder().withLevel(50).withEnhancement("haste", 3).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        TestPlugin.getInstance().getEconomy().depositPlayer(playerMock, 100000);

        serverMock.dispatchCommand(playerMock, "test upgrades buy haste");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().maxUpgradeLevelReached
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%booster%", "farming")
        ));
        playerMock.assertNoMoreSaid();

        assertEquals(100000, TestPlugin.getInstance().getEconomy().getBalance(playerMock));
        assertEquals(3, TestPlugin.getInstance().getTeamManager().getTeamEnhancement(testTeam, "haste").getLevel());
    }

    @Test
    public void executeUpgradesCommand__Buy__Success() {
        TestTeam testTeam = new TeamBuilder().withLevel(5).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        TestPlugin.getInstance().getEconomy().depositPlayer(playerMock, 100000);

        serverMock.dispatchCommand(playerMock, "test upgrades buy haste");

        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().purchasedUpgrade
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%upgrade%", "haste")
        ));
        playerMock.assertNoMoreSaid();

        assertEquals(90000, TestPlugin.getInstance().getEconomy().getBalance(playerMock));
        assertEquals(1, TestPlugin.getInstance().getTeamManager().getTeamEnhancement(testTeam, "haste").getLevel());
        assertTrue(TestPlugin.getInstance().getTeamManager().getTeamEnhancement(testTeam, "haste").isActive());
        assertTrue(EnhancementUpdateEvent.called);
    }

}