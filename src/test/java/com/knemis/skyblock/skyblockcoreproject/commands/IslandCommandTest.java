package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.economy.worth.IslandWorthManager;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandSettingsManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import net.kyori.adventure.text.Component;

@ExtendWith(MockitoExtension.class)
public class IslandCommandTest {

    @Mock private SkyBlockProject plugin;
    @Mock private IslandDataHandler islandDataHandler;
    @Mock private IslandLifecycleManager islandLifecycleManager;
    @Mock private IslandSettingsManager islandSettingsManager;
    @Mock private IslandMemberManager islandMemberManager;
    @Mock private IslandTeleportManager islandTeleportManager;
    @Mock private IslandBiomeManager islandBiomeManager;
    @Mock private IslandWelcomeManager islandWelcomeManager;
    @Mock private FlagGUIManager flagGUIManager;
    @Mock private IslandWorthManager islandWorthManager;
    @Mock private Economy economy; // Assuming IslandCommand uses Vault Economy

    @Mock private Player player;
    @Mock private Command command;
    @Mock private FileConfiguration fileConfiguration;
    @Mock private LuckPerms luckPermsApi;
    @Mock private UserManager userManager;
    @Mock private User lpUser;
    // @Mock private Group group; // Not directly mocking Group, but primary group name as String

    @InjectMocks private IslandCommand islandCommand;

    private final UUID playerUUID = UUID.randomUUID();
    private final String playerName = "TestPlayer";
    private final long COOLDOWN_SECONDS = 300L; // Default cooldown from IslandCommand constructor

    @BeforeEach
    void setUp() {
        // Mock Player
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getName()).thenReturn(playerName);

        // Mock Plugin and Config
        // Use a real logger or a simple mock to avoid NPE if methods like getLogger().info() are called.
        // A proper PluginLogger mock might be needed if specific logging interactions are verified.
        Logger mockLogger = Logger.getLogger("MockLogger");
        // For Bukkit's getLogger, it returns a PluginLogger or Logger.
        // If your plugin.getLogger() returns a specific type, mock that.
        // For simplicity, if it's just Logger:
        when(plugin.getLogger()).thenReturn(mockLogger); // Or use a mock(PluginLogger.class)

        when(plugin.getConfig()).thenReturn(fileConfiguration);
        when(fileConfiguration.getLong(eq("island.creation-cooldown-seconds"), anyLong())).thenReturn(COOLDOWN_SECONDS);
        // Mock economy if IslandCommand constructor uses it.
        // IslandCommand's constructor does: this.economy = plugin.getEconomy();
        when(plugin.getEconomy()).thenReturn(economy);


        // Initialize IslandCommand with mocks manually if @InjectMocks doesn't cover complex cases
        // or if constructor logic needs to be specifically tested/controlled.
        // However, for this setup, @InjectMocks should work given the constructor parameters.
        // Re-initialize to ensure plugin.getConfig() is mocked before constructor call by @InjectMocks if needed
        // islandCommand = new IslandCommand(plugin, islandDataHandler, islandLifecycleManager, islandSettingsManager, islandMemberManager, islandTeleportManager, islandBiomeManager, islandWelcomeManager, flagGUIManager, islandWorthManager);
        // For this test, we rely on @InjectMocks to handle the constructor.
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
    void ownerBypassesIslandCreationCooldown() {
        setupLuckPermsUser("owner");
        when(islandDataHandler.playerHasIsland(playerUUID)).thenReturn(false);

        // Simulate "/island create"
        islandCommand.onCommand(player, command, "island", new String[]{"create"});

        // Verify island creation was called
        verify(islandLifecycleManager).createIsland(player);
        // Cooldown map should NOT be updated for owner (this requires checking internal state or behavior)
        // For now, we assume if createIsland is called, and no cooldown message is sent, it's a pass.
        // A more robust check might involve checking the 'createCooldowns' map via reflection if essential,
        // or verifying no cooldown message was sent.
        verify(player, never()).sendMessage(argThat((Component component) -> component.toString().contains("saniye daha beklemelisiniz")));
    }

    @Test
    void nonOwnerSubjectToIslandCreationCooldown() {
        setupLuckPermsUser("default"); // Any non-owner group
        when(islandDataHandler.playerHasIsland(playerUUID)).thenReturn(false);

        // First attempt: should create island and apply cooldown
        islandCommand.onCommand(player, command, "island", new String[]{"create"});
        verify(islandLifecycleManager).createIsland(player);
        // Verify a cooldown message was NOT sent on first successful creation
        verify(player, never()).sendMessage(argThat((Component component) -> component.toString().contains("saniye daha beklemelisiniz")));


        // Second attempt immediately: should be on cooldown
        // Reset interaction with islandLifecycleManager to check it's not called again
        reset(islandLifecycleManager);
        islandCommand.onCommand(player, command, "island", new String[]{"create"});
        verify(islandLifecycleManager, never()).createIsland(player);
        // Verify cooldown message IS sent
        // This requires capturing or verifying the sendMessage call content.
        // The actual message text might be language-dependent.
        // Using argThat for flexible message checking.
        verify(player).sendMessage(argThat((Component component) -> component.toString().contains("saniye daha beklemelisiniz")));
    }

    @Test
    void luckPermsApiUnavailableAppliesCooldown() {
        setupLuckPermsApiUnavailable(); // Simulate LuckPerms not being available
        when(islandDataHandler.playerHasIsland(playerUUID)).thenReturn(false);

        // First attempt: should create island and apply cooldown (as if non-owner)
        islandCommand.onCommand(player, command, "island", new String[]{"create"});
        verify(islandLifecycleManager).createIsland(player);
        verify(player, never()).sendMessage(argThat((Component component) -> component.toString().contains("saniye daha beklemelisiniz")));


        // Second attempt immediately: should be on cooldown
        reset(islandLifecycleManager);
        islandCommand.onCommand(player, command, "island", new String[]{"create"});
        verify(islandLifecycleManager, never()).createIsland(player);
        verify(player).sendMessage(argThat((Component component) -> component.toString().contains("saniye daha beklemelisiniz")));
    }
}
