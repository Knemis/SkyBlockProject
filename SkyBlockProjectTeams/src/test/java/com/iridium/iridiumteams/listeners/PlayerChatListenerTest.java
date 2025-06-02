package com.keviin.keviinteams.listeners;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerChatListenerTest {

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
    public void onPlayerChatNoTeam() {
        PlayerMock messageSender = new UserBuilder(serverMock).build();

        AsyncPlayerChatEvent asyncPlayerChatEvent = new AsyncPlayerChatEvent(false, messageSender, "test", new HashSet<>(serverMock.getOnlinePlayers()));
        serverMock.getPluginManager().callEvent(asyncPlayerChatEvent);
        assertFalse(asyncPlayerChatEvent.getRecipients().isEmpty());
    }

    @Test
    public void onPlayerChatNoChatType() {
        TestTeam team = new TeamBuilder().build();
        PlayerMock messageSender = new UserBuilder(serverMock).withTeam(team).withChatType("none").build();

        AsyncPlayerChatEvent asyncPlayerChatEvent = new AsyncPlayerChatEvent(false, messageSender, "test", new HashSet<>(serverMock.getOnlinePlayers()));
        serverMock.getPluginManager().callEvent(asyncPlayerChatEvent);
        assertFalse(asyncPlayerChatEvent.getRecipients().isEmpty());
    }

    @Test
    public void onPlayerChat__TeamChatType() {
        TestTeam team = new TeamBuilder().build();
        PlayerMock messageSender = new UserBuilder(serverMock).withTeam(team).withChatType("team").build();
        PlayerMock otherTeamMember = new UserBuilder(serverMock).withTeam(team).build();
        PlayerMock nonTeamMember = new UserBuilder(serverMock).build();

        AsyncPlayerChatEvent asyncPlayerChatEvent = new AsyncPlayerChatEvent(false, messageSender, "test", new HashSet<>(serverMock.getOnlinePlayers()));
        serverMock.getPluginManager().callEvent(asyncPlayerChatEvent);
        assertTrue(asyncPlayerChatEvent.getRecipients().isEmpty());


        messageSender.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().chatFormat
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%player%", messageSender.getName())
                .replace("%message%", "test"))
        );
        messageSender.assertNoMoreSaid();
        otherTeamMember.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().chatFormat
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%player%", messageSender.getName())
                .replace("%message%", "test"))
        );
        otherTeamMember.assertNoMoreSaid();
        nonTeamMember.assertNoMoreSaid();
    }
}