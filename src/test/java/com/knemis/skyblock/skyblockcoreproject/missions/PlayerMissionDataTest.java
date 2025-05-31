package com.knemis.skyblock.skyblockcoreproject.missions;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class PlayerMissionDataTest {

    @Mock private SkyBlockProject plugin;
    @Mock private LuckPerms luckPermsApi;
    @Mock private UserManager userManager;
    @Mock private User lpUser;
    @Mock private Mission mission;
    @Mock private Player player;
    // @Mock private PluginManager pluginManager; // For Bukkit.getPluginManager() if used for plugin instance

    private MockedStatic<Bukkit> bukkitMockedStatic;
    private final UUID playerUUID = UUID.randomUUID();
    private final String missionId = "testMission";
    private PlayerMissionData playerMissionData;

    @BeforeEach
    void setUp() {
        // Mock static Bukkit.getPlayer()
        bukkitMockedStatic = Mockito.mockStatic(Bukkit.class);
        bukkitMockedStatic.when(() -> Bukkit.getPlayer(playerUUID)).thenReturn(player);

        // Mock Player
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getName()).thenReturn("TestPlayer");

        // Mock Plugin Logger
        // Use a real logger or a simple mock to avoid NPE if methods like getLogger().info() are called.
        Logger mockLogger = Logger.getLogger("MockLoggerPMD");
        when(plugin.getLogger()).thenReturn(mockLogger); // Assuming getLogger returns java.util.logging.Logger

        // Standard PlayerMissionData instantiation for each test
        // Constructor: PlayerMissionData(UUID playerUuid, SkyBlockProject plugin, Map activeMissions, Set completedMissions, Map missionCooldowns)
        // For simplicity, using empty collections for non-cooldown related parts.
        playerMissionData = new PlayerMissionData(playerUUID, plugin, new HashMap<>(), Collections.emptySet(), new HashMap<>());
    }

    @AfterEach
    void tearDown() {
        bukkitMockedStatic.close();
    }

    private void setupLuckPermsUser(String primaryGroup) {
        when(plugin.getLuckPermsApi()).thenReturn(luckPermsApi);
        when(luckPermsApi.getUserManager()).thenReturn(userManager);
        when(userManager.getUser(playerUUID)).thenReturn(lpUser);
        when(lpUser.getPrimaryGroup()).thenReturn(primaryGroup);
    }

    private void setupLuckPermsApiUnavailable() {
        when(plugin.getLuckPermsApi()).thenReturn(null);
    }

    @Test
    void ownerBypassesMissionCooldown() {
        setupLuckPermsUser("owner");
        when(mission.getId()).thenReturn(missionId);
        when(mission.getRepeatableType()).thenReturn("COOLDOWN");
        when(mission.getCooldownHours()).thenReturn(1);

        playerMissionData.markMissionCompleted(missionId, mission);

        assertFalse(playerMissionData.isMissionOnCooldown(missionId), "Owner should bypass mission cooldown.");
        assertTrue(playerMissionData.getMissionCooldowns().isEmpty(), "Cooldown map should be empty for owner bypass.");
         verify(plugin.getLogger()).info("Mission cooldown for " + missionId + " bypassed for owner " + player.getName());
    }

    @Test
    void nonOwnerSubjectToMissionCooldown() {
        setupLuckPermsUser("default");
        when(mission.getId()).thenReturn(missionId);
        when(mission.getRepeatableType()).thenReturn("COOLDOWN");
        when(mission.getCooldownHours()).thenReturn(1);

        playerMissionData.markMissionCompleted(missionId, mission);

        assertTrue(playerMissionData.isMissionOnCooldown(missionId), "Non-owner should be subject to mission cooldown.");
        assertFalse(playerMissionData.getMissionCooldowns().isEmpty(), "Cooldown map should not be empty for non-owner.");
        assertTrue(playerMissionData.getMissionCooldowns().containsKey(missionId), "Cooldown map should contain the mission ID.");
        verify(plugin.getLogger(), never()).info("Mission cooldown for " + missionId + " bypassed for owner " + player.getName());
    }

    @Test
    void luckPermsApiUnavailableAppliesCooldown() {
        setupLuckPermsApiUnavailable(); // Simulate LuckPerms not being available
        when(mission.getId()).thenReturn(missionId);
        when(mission.getRepeatableType()).thenReturn("COOLDOWN");
        when(mission.getCooldownHours()).thenReturn(1);
        // When LuckPerms is unavailable, a warning should be logged.
        // We can verify this if the logger is also mocked and injected into PlayerMissionData if necessary.
        // For now, the main check is that cooldown is applied.

        playerMissionData.markMissionCompleted(missionId, mission);

        assertTrue(playerMissionData.isMissionOnCooldown(missionId), "Cooldown should apply if LuckPerms API is unavailable.");
        assertFalse(playerMissionData.getMissionCooldowns().isEmpty(), "Cooldown map should not be empty when API is unavailable.");
        assertTrue(playerMissionData.getMissionCooldowns().containsKey(missionId));
        // Also verify a warning log might have occurred due to LuckPerms API being null
        // This depends on the exact logging implemented in PlayerMissionData
        verify(plugin.getLogger()).warning("LuckPerms API not available for mission cooldown bypass check for player " + player.getName());
    }

    @Test
    void missionNotRepeatableNoCooldownApplied() {
        // No LuckPerms setup needed as it shouldn't reach that part
        when(mission.getId()).thenReturn(missionId);
        when(mission.getRepeatableType()).thenReturn("NONE"); // Not repeatable
        when(mission.getCooldownHours()).thenReturn(1); // Cooldown hours present but type is NONE

        playerMissionData.markMissionCompleted(missionId, mission);

        assertFalse(playerMissionData.isMissionOnCooldown(missionId), "Cooldown should not apply if mission is not repeatable.");
        assertTrue(playerMissionData.getMissionCooldowns().isEmpty(), "Cooldown map should be empty for non-repeatable mission.");
    }

    @Test
    void missionNoCooldownHoursNoCooldownApplied() {
        // No LuckPerms setup needed
        when(mission.getId()).thenReturn(missionId);
        when(mission.getRepeatableType()).thenReturn("COOLDOWN");
        when(mission.getCooldownHours()).thenReturn(0); // No cooldown hours

        playerMissionData.markMissionCompleted(missionId, mission);

        assertFalse(playerMissionData.isMissionOnCooldown(missionId), "Cooldown should not apply if mission has 0 cooldown hours.");
        assertTrue(playerMissionData.getMissionCooldowns().isEmpty(), "Cooldown map should be empty for 0 cooldown hours mission.");
    }
}
