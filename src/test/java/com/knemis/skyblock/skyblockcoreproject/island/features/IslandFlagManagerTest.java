package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags; // Assuming this is where VISITOR_SHOP_USE is
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.junit.jupiter.api.AfterEach;
import java.lang.reflect.Field; // For reflection
import java.lang.reflect.Modifier; // For reflection
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IslandFlagManagerTest {

    @Mock private SkyBlockProject plugin;
    @Mock private IslandDataHandler islandDataHandler;
    @Mock private FileConfiguration fileConfiguration;
    @Mock private ConfigurationSection defaultConfigSection;
    @Mock private PluginLogger pluginLogger; // Bukkit's PluginLogger

    @Mock private WorldGuard worldGuard;
    @Mock private FlagRegistry flagRegistry;

    // Real StateFlags or well-behaved Mocks
    private StateFlag pvpFlag;
    private StateFlag buildFlag;
    private StateFlag blockBreakFlag;
    private StateFlag visitorShopUseFlagMock; // Mock for CustomFlags.VISITOR_SHOP_USE

    private IslandFlagManager islandFlagManager;
    private MockedStatic<WorldGuard> worldGuardMockedStatic;
    private MockedStatic<CustomFlags> customFlagsMockedStatic;


    @BeforeEach
    void setUp() {
        worldGuardMockedStatic = Mockito.mockStatic(WorldGuard.class);
        customFlagsMockedStatic = Mockito.mockStatic(CustomFlags.class); // Mock static access to CustomFlags.VISITOR_SHOP_USE

        when(plugin.getLogger()).thenReturn(pluginLogger);
        when(plugin.getConfig()).thenReturn(fileConfiguration);
        when(fileConfiguration.getConfigurationSection("island.default-flags")).thenReturn(defaultConfigSection);
        when(fileConfiguration.getBoolean("logging.detailed-flag-changes", false)).thenReturn(false); // Default to no detailed logging for tests unless specified

        worldGuardMockedStatic.when(WorldGuard::getInstance).thenReturn(worldGuard);
        when(worldGuard.getFlagRegistry()).thenReturn(flagRegistry);

        // Initialize real StateFlag objects or detailed mocks
        pvpFlag = Flags.PVP; // Using actual WorldGuard flag
        buildFlag = Flags.BUILD;
        blockBreakFlag = Flags.BLOCK_BREAK; // Assuming this is a standard WG flag

        // Mock our custom flag
        visitorShopUseFlagMock = mock(StateFlag.class);
        when(visitorShopUseFlagMock.getName()).thenReturn("visitor-shop-use"); // Ensure it has a name for logging/map keys

        // Make CustomFlags.VISITOR_SHOP_USE return our mock via flagRegistry
        // The static field itself will be handled by reflection in specific tests if needed.
        // customFlagsMockedStatic.when(() -> CustomFlags.VISITOR_SHOP_USE).thenReturn(visitorShopUseFlagMock); // Removed problematic line

        // Default behavior for flagRegistry.get()
        lenient().<Flag<?>>when(flagRegistry.get(eq("PVP"))).thenReturn(pvpFlag);
        lenient().<Flag<?>>when(flagRegistry.get(eq("BUILD"))).thenReturn(buildFlag);
        lenient().<Flag<?>>when(flagRegistry.get(eq("BLOCK_BREAK"))).thenReturn(blockBreakFlag);
        lenient().<Flag<?>>when(flagRegistry.get(eq("VISITOR_SHOP_USE"))).thenReturn(visitorShopUseFlagMock);
        lenient().<Flag<?>>when(flagRegistry.get(eq("INTERACT"))).thenReturn(Flags.INTERACT);
        lenient().<Flag<?>>when(flagRegistry.get(eq("USE"))).thenReturn(Flags.USE);
        lenient().<Flag<?>>when(flagRegistry.get(eq("CHEST_ACCESS"))).thenReturn(Flags.CHEST_ACCESS);
        lenient().<Flag<?>>when(flagRegistry.get(eq("DAMAGE_ANIMALS"))).thenReturn(Flags.DAMAGE_ANIMALS);


        // IslandFlagManager is instantiated after mocks are set up
        // islandFlagManager = new IslandFlagManager(plugin, islandDataHandler);
        // This will be done in each test or a setup method that runs after mock configurations specific to that test
    }

    @AfterEach
    void tearDown() {
        worldGuardMockedStatic.close();
        // customFlagsMockedStatic.close(); // Closed if it was opened. Now removed.
        resetCustomFlagsStaticField(); // Reset reflection changes
    }

    // Helper to set static final field CustomFlags.VISITOR_SHOP_USE
    private void setStaticVisitorShopUseFlag(StateFlag flag) throws Exception {
        Field field = CustomFlags.class.getDeclaredField("VISITOR_SHOP_USE");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, flag);
    }

    // Helper to reset the field if necessary (e.g., to null or original if known)
    private void resetCustomFlagsStaticField() {
        try {
            // Attempt to set it back to null, or its original value if it was known
            // This is to prevent test side effects. For simple cases, null is fine.
            setStaticVisitorShopUseFlag(null);
        } catch (Exception e) {
            // Log or handle if reset fails, though for tests it might not be critical
            // if each test needing it sets it appropriately.
        }
    }


    @Test
    void testInitializeDefaultFlags_LoadsFromConfigCorrectly() {
        Map<String, String> flagsInConfig = new HashMap<>();
        flagsInConfig.put("PVP", "DENY");
        flagsInConfig.put("BLOCK_BREAK", "DENY");
        flagsInConfig.put("VISITOR_SHOP_USE", "ALLOW");
        flagsInConfig.put("BUILD", "ALLOW"); // Example of one being different

        when(defaultConfigSection.getKeys(false)).thenReturn(new HashSet<>(flagsInConfig.keySet()));
        for (Map.Entry<String, String> entry : flagsInConfig.entrySet()) {
            when(defaultConfigSection.getString(entry.getKey(), "")).thenReturn(entry.getValue());
        }

        // Ensure CustomFlags.VISITOR_SHOP_USE is treated as a known key if present in config
        // This is covered by flagRegistry.get("VISITOR_SHOP_USE") returning visitorShopUseFlagMock

        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler); // Initialize here to trigger initializeDefaultFlags

        assertEquals(StateFlag.State.DENY, islandFlagManager.getDefaultStateForFlag(pvpFlag));
        assertEquals(StateFlag.State.DENY, islandFlagManager.getDefaultStateForFlag(blockBreakFlag));
        assertEquals(StateFlag.State.ALLOW, islandFlagManager.getDefaultStateForFlag(visitorShopUseFlagMock));
        assertEquals(StateFlag.State.ALLOW, islandFlagManager.getDefaultStateForFlag(buildFlag)); // Check the one that's different from "standard" protection

        verify(pluginLogger, atLeastOnce()).info(contains("Loading default island flags from config.yml..."));
        verify(pluginLogger).info(contains("Loaded default flag from config: PVP -> DENY"));
        verify(pluginLogger).info(contains("Loaded default flag from config: VISITOR_SHOP_USE -> ALLOW"));
    }

    @Test
    void testInitializeDefaultFlags_HandlesFallbackForVisitorShopUseFlag() throws Exception {
        when(defaultConfigSection.getKeys(false)).thenReturn(Collections.emptySet()); // No "VISITOR_SHOP_USE" in config

        // Set the static CustomFlags.VISITOR_SHOP_USE to our mock for this test
        setStaticVisitorShopUseFlag(visitorShopUseFlagMock);
        // Ensure flagRegistry returns it for string lookup IF IslandFlagManager were to use string first
        // but the current IFM logic directly uses the CustomFlags.VISITOR_SHOP_USE static field as key.
        // So, the above setStatic is the crucial part.

        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler);

        assertEquals(StateFlag.State.ALLOW, islandFlagManager.getDefaultStateForFlag(visitorShopUseFlagMock));
        verify(pluginLogger).info(contains("Custom flag 'VISITOR_SHOP_USE' was not defined in config.yml, adding with default state ALLOW as a fallback."));
    }

    @Test
    void testInitializeDefaultFlags_HandlesUnknownFlagInConfig() {
        String unknownFlagName = "TOTALLY_UNKNOWN_FLAG";
        Map<String, String> flagsInConfig = new HashMap<>();
        flagsInConfig.put(unknownFlagName, "ALLOW"); // This line correctly populates the map

        // Corrected line for mocking getKeys:
        when(defaultConfigSection.getKeys(false)).thenReturn(new HashSet<>(flagsInConfig.keySet()));

        // Mock the getString call for the unknown flag name:
        when(defaultConfigSection.getString(unknownFlagName, "")).thenReturn("ALLOW");

        // Simulate that the flag registry does not know this flag:
        when(flagRegistry.get(unknownFlagName)).thenReturn(null);

        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler);

        assertNull(islandFlagManager.getDefaultStateForFlag(mock(StateFlag.class))); // Should not be added
        verify(pluginLogger).warning(contains("Unknown flag name '" + unknownFlagName + "' in config.yml"));
    }

    @Test
    void testInitializeDefaultFlags_HandlesNonStateFlagInConfig() {
        String nonStateFlagName = "MY_CUSTOM_STRING_FLAG";
        Flag<?> mockNonStateFlag = mock(Flag.class); // A generic flag, not a StateFlag
        when(mockNonStateFlag.getName()).thenReturn(nonStateFlagName);

        Map<String, String> flagsInConfig = new HashMap<>();
        flagsInConfig.put(nonStateFlagName, "ANY_VALUE");

        when(defaultConfigSection.getKeys(false)).thenReturn(new HashSet<>(flagsInConfig.keySet()));
        when(defaultConfigSection.getString(nonStateFlagName, "")).thenReturn("ANY_VALUE");
        lenient().<Flag<?>>when(flagRegistry.get(nonStateFlagName)).thenReturn(mockNonStateFlag);


        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler);

        verify(pluginLogger).warning(contains("Configured default flag '" + nonStateFlagName + "' is not a StateFlag"));
    }


    @Test
    void testApplyDefaultFlagsToRegion_AppliesLoadedDefaults() {
        // Setup config to load some defaults
        Map<String, String> flagsInConfig = new HashMap<>();
        flagsInConfig.put("PVP", "DENY");
        flagsInConfig.put("BUILD", "ALLOW");
        when(defaultConfigSection.getKeys(false)).thenReturn(new HashSet<>(flagsInConfig.keySet()));
        for (Map.Entry<String, String> entry : flagsInConfig.entrySet()) {
            when(defaultConfigSection.getString(entry.getKey(), "")).thenReturn(entry.getValue());
        }
        // VISITOR_SHOP_USE will be added by fallback

        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler); // Loads defaults

        ProtectedRegion mockedRegion = mock(ProtectedRegion.class);
        islandFlagManager.applyDefaultFlagsToRegion(mockedRegion);

        verify(mockedRegion).setFlag(pvpFlag, StateFlag.State.DENY);
        verify(mockedRegion).setFlag(buildFlag, StateFlag.State.ALLOW);
        verify(mockedRegion).setFlag(visitorShopUseFlagMock, StateFlag.State.ALLOW); // From fallback
    }

    @Test
    void testSetIslandFlagState_SetsFlagOnRegionCorrectly() throws Exception {
        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler); // Basic init

        Player mockedPlayer = mock(Player.class);
        UUID ownerUuid = UUID.randomUUID();
        Island mockedIsland = mock(Island.class);
        World mockedBukkitWorld = mock(World.class);
        RegionManager mockedRegionManager = mock(RegionManager.class);
        ProtectedRegion mockedProtectedRegion = mock(ProtectedRegion.class);

        when(mockedPlayer.getUniqueId()).thenReturn(ownerUuid); // Player is owner
        when(islandDataHandler.getIslandByOwner(ownerUuid)).thenReturn(mockedIsland);
        when(mockedIsland.getWorld()).thenReturn(mockedBukkitWorld);
        when(plugin.getRegionManager(mockedBukkitWorld)).thenReturn(mockedRegionManager);
        when(mockedRegionManager.getRegion(anyString())).thenReturn(mockedProtectedRegion);

        boolean result = islandFlagManager.setIslandFlagState(mockedPlayer, ownerUuid, pvpFlag, StateFlag.State.ALLOW);

        assertTrue(result);
        verify(mockedProtectedRegion).setFlag(pvpFlag, StateFlag.State.ALLOW);
        verify(mockedRegionManager).saveChanges();
        verify(mockedPlayer).sendMessage(any(Component.class)); // Verify success message
    }

    @Test
    void testSetIslandFlagState_HandlesStorageException() throws Exception {
        islandFlagManager = new IslandFlagManager(plugin, islandDataHandler);

        Player mockedPlayer = mock(Player.class);
        UUID ownerUuid = UUID.randomUUID();
        Island mockedIsland = mock(Island.class);
        World mockedBukkitWorld = mock(World.class);
        RegionManager mockedRegionManager = mock(RegionManager.class);
        ProtectedRegion mockedProtectedRegion = mock(ProtectedRegion.class);

        when(mockedPlayer.getUniqueId()).thenReturn(ownerUuid);
        when(islandDataHandler.getIslandByOwner(ownerUuid)).thenReturn(mockedIsland);
        when(mockedIsland.getWorld()).thenReturn(mockedBukkitWorld);
        when(plugin.getRegionManager(mockedBukkitWorld)).thenReturn(mockedRegionManager);
        when(mockedRegionManager.getRegion(anyString())).thenReturn(mockedProtectedRegion);

        // Simulate saveChanges throwing an exception
        doThrow(new com.sk89q.worldguard.protection.managers.storage.StorageException("Test Error"))
            .when(mockedRegionManager).saveChanges();

        boolean result = islandFlagManager.setIslandFlagState(mockedPlayer, ownerUuid, pvpFlag, StateFlag.State.DENY);

        assertFalse(result);
        verify(mockedProtectedRegion).setFlag(pvpFlag, StateFlag.State.DENY); // Flag is set before save
        verify(mockedPlayer).sendMessage(argThat((Component component) -> component.toString().contains("beklenmedik bir hata oluştu")));
        verify(pluginLogger).log(eq(java.util.logging.Level.SEVERE), contains("setIslandFlagState sırasında hata"), any(com.sk89q.worldguard.protection.managers.storage.StorageException.class));
    }
}
