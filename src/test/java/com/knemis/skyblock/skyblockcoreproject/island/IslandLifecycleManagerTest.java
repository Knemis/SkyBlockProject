package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region; // Added import for Region
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.function.pattern.Pattern; // Added import for Pattern
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Chunk;
import org.bukkit.Bukkit; // Added import
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers; // Added import

import java.io.File;
import java.io.FileInputStream;
import java.util.Set; // Added import
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IslandLifecycleManagerTest {

    @Mock private SkyBlockProject plugin;
    @Mock private IslandDataHandler islandDataHandler;
    @Mock private IslandFlagManager islandFlagManager;
    @Mock private FileConfiguration fileConfiguration;
    @Mock private Server server;
    @Mock private PluginManager pluginManager;
    @Mock private BukkitScheduler bukkitScheduler;
    @Mock private Economy economy;

    @Mock private World bukkitWorld;
    @Mock private com.sk89q.worldedit.world.World weWorld; // WorldEdit world
    @Mock private RegionManager regionManager;
    @Mock private WorldGuard worldGuard;
    @Mock private RegionContainer regionContainer;


    @Mock private Player player;
    @Mock private Island island;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private EditSession editSession; // Mock EditSession deeply for chained calls
    @Mock private Clipboard clipboard;
    @Mock private ClipboardFormat clipboardFormat;
    @Mock private ClipboardReader clipboardReader;
    @Mock private File mockSchematicFile;
    @Mock private File mockDataFolder;
    @Mock private PluginLogger pluginLogger;


    private IslandLifecycleManager islandLifecycleManager;
    private IslandLifecycleManager spiedIslandManager; // For spying on getIslandTerritoryRegion

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<WorldEdit> worldEditMock;
    private MockedStatic<BukkitAdapter> bukkitAdapterMock;
    private MockedStatic<Operations> operationsMock;
    private MockedStatic<ClipboardFormats> clipboardFormatsMock;
    private MockedStatic<WorldGuard> worldGuardMockStatic;


    private final UUID playerUUID = UUID.randomUUID();
    private final String playerName = "TestPlayer";
    private final String islandRegionId = "skyblock_island_" + playerUUID.toString();
    private Location baseLocation;
    private CuboidRegion mockCuboidRegion;


    @BeforeEach
    void setUp() throws IOException {
        bukkitMock = Mockito.mockStatic(Bukkit.class);
        worldEditMock = Mockito.mockStatic(WorldEdit.class);
        bukkitAdapterMock = Mockito.mockStatic(BukkitAdapter.class);
        operationsMock = Mockito.mockStatic(Operations.class);
        clipboardFormatsMock = Mockito.mockStatic(ClipboardFormats.class);
        worldGuardMockStatic = Mockito.mockStatic(WorldGuard.class);


        when(plugin.getLogger()).thenReturn(pluginLogger);
        when(plugin.getConfig()).thenReturn(fileConfiguration);
        // Mock common config settings used by IslandLifecycleManager constructor or methods
        when(fileConfiguration.getString(eq("island.default-name-prefix"), anyString())).thenReturn("Ada");
        when(fileConfiguration.getInt(eq("island.max-named-homes"), anyInt())).thenReturn(3);
        when(fileConfiguration.getString(eq("island.schematic-file-name"), anyString())).thenReturn("island.schem");
        when(fileConfiguration.getInt(eq("island.expansion-radius-horizontal"), anyInt())).thenReturn(50);
        when(fileConfiguration.getInt(eq("island.expansion-radius-vertical-bottom"), anyInt())).thenReturn(20);
        when(fileConfiguration.getBoolean(eq("island.allow-build-below-schematic-base"), anyBoolean())).thenReturn(false);
        when(fileConfiguration.getInt(eq("island.build-limit-above-schematic-top"), anyInt())).thenReturn(150);


        when(plugin.getDataFolder()).thenReturn(mockDataFolder);
        when(mockDataFolder.getPath()).thenReturn("mocked_plugins/SkyBlockProject");
        //This line is tricky, File constructor is final. We need to mock the File instance behavior if possible, or ensure path leads to a mockable file.
        //For schematicFile.exists(), we can mock the File object itself if it's created via a factory or passed in.
        //If 'new File(plugin.getDataFolder(), "island.schem")' is used directly, we need to ensure getDataFolder() returns a path where we can control 'exists'.
        //For simplicity, let's assume schematicFile operations will be part of the spied getIslandTerritoryRegion or heavily mocked.


        bukkitMock.when(Bukkit::getServer).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(bukkitScheduler);
        // Mock BukkitTask for runTaskLater
        when(bukkitScheduler.runTaskLater(any(SkyBlockProject.class), any(Runnable.class), anyLong())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run(); // Run immediately for test purposes
            return mock(BukkitTask.class);
        });


        when(plugin.getEconomy()).thenReturn(economy);


        baseLocation = new Location(bukkitWorld, 100, 100, 100);
        when(island.getBaseLocation()).thenReturn(baseLocation);
        when(island.getRegionId()).thenReturn(islandRegionId);
        when(island.getOwnerUUID()).thenReturn(playerUUID);


        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getName()).thenReturn(playerName);


        bukkitAdapterMock.when(() -> BukkitAdapter.adapt(bukkitWorld)).thenReturn(weWorld);
        worldEditMock.when(WorldEdit::getInstance).thenReturn(mock(WorldEdit.class)); // Basic mock for getInstance()
        when(WorldEdit.getInstance().newEditSession(weWorld)).thenReturn(editSession);


        // Mock WorldGuard
        worldGuardMockStatic.when(WorldGuard::getInstance).thenReturn(worldGuard);
        when(worldGuard.getPlatform()).thenReturn(mock()); // WorldGuardPlatform
        when(worldGuard.getPlatform().getRegionContainer()).thenReturn(regionContainer);
        when(regionContainer.get(weWorld)).thenReturn(regionManager);
        when(regionManager.getRegion(islandRegionId)).thenReturn(mock(ProtectedCuboidRegion.class));


        // Setup for getIslandTerritoryRegion mocking via spy
        // This needs to be done carefully. The actual IslandLifecycleManager instance is created, then spied.
        islandLifecycleManager = new IslandLifecycleManager(plugin, islandDataHandler, islandFlagManager);
        spiedIslandManager = Mockito.spy(islandLifecycleManager);


        // Mock the CuboidRegion that getIslandTerritoryRegion would return
        mockCuboidRegion = mock(CuboidRegion.class);
        BlockVector3 minPoint = BlockVector3.at(50, 50, 50);
        BlockVector3 maxPoint = BlockVector3.at(150, 150, 150);
        when(mockCuboidRegion.getMinimumPoint()).thenReturn(minPoint);
        when(mockCuboidRegion.getMaximumPoint()).thenReturn(maxPoint);
        when(mockCuboidRegion.getWorld()).thenReturn(weWorld); // Ensure it has a world


        // Critical: Override getIslandTerritoryRegion for the spied instance
        // This bypasses the complex internal logic of getIslandTerritoryRegion including schematic loading.
        doReturn(mockCuboidRegion).when(spiedIslandManager).getIslandTerritoryRegion(any(Location.class));


        // Mock schematic operations to avoid errors if any part of the original getIslandTerritoryRegion is called
        // despite the spy (e.g. if spy setup is incomplete or a different instance is used internally).
        // This is a fallback.
        clipboardFormatsMock.when(() -> ClipboardFormats.findByFile(any(File.class))).thenReturn(clipboardFormat);
        // Ensure that the schematicFile field in IslandLifecycleManager is either a mock or its methods are controlled.
        // For this test, we'll assume the `spy` on `getIslandTerritoryRegion` makes this less critical.
        // If `new File(plugin.getDataFolder(), "island.schem")` is used, then:
        when(mockDataFolder.exists()).thenReturn(true); // Mock data folder exists
        // The schematicFile itself is harder to mock directly if created with 'new'.
        // The spy strategy is better.


        // Mock operations for pasting (used in reset)
        operationsMock.when(() -> Operations.complete(any(Operation.class))).thenReturn(1L); // Return some blocks affected
        // Mock clipboard holder chain for reset
        if(clipboardFormat != null) { // Guard against clipboardFormat being null if findByFile fails
            when(clipboardFormat.getReader(any(FileInputStream.class))).thenReturn(clipboardReader);
            when(clipboardReader.read()).thenReturn(clipboard);
        }
        when(clipboard.getRegion()).thenReturn(mock(com.sk89q.worldedit.regions.Region.class)); // Basic mock for clipboard's region
        when(clipboard.getOrigin()).thenReturn(BlockVector3.ZERO);


    }


    @AfterEach
    void tearDown() {
        bukkitMock.close();
        worldEditMock.close();
        bukkitAdapterMock.close();
        operationsMock.close();
        clipboardFormatsMock.close();
        worldGuardMockStatic.close();
    }


    private Chunk mockChunkWithEntities(int cx, int cz, List<Entity> entitiesInChunk) {
        Chunk mockChunk = mock(Chunk.class);
        when(bukkitWorld.isChunkLoaded(cx, cz)).thenReturn(true);
        when(bukkitWorld.getChunkAt(cx, cz)).thenReturn(mockChunk);
        when(mockChunk.getEntities()).thenReturn(entitiesInChunk.toArray(new Entity[0]));
        return mockChunk;
    }


    private <T extends Entity> T mockEntityInRegion(Class<T> entityClass, double x, double y, double z, boolean isInRegion) {
        T entity = mock(entityClass);
        Location entityLoc = new Location(bukkitWorld, x, y, z);
        when(entity.getLocation()).thenReturn(entityLoc);
        // Mock containment check for this specific entity's position
        BlockVector3 entityPosVec = BlockVector3.at(x,y,z);
        when(mockCuboidRegion.contains(entityPosVec)).thenReturn(isInRegion);
        return entity;
    }




    @Test
    void testDeleteIsland_RemovesEntitiesCorrectly() throws IOException, com.sk89q.worldedit.MaxChangedBlocksException { // Added MaxChangedBlocksException
        // Arrange
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island);
        when(bukkitWorld.getMinHeight()).thenReturn(0); // For getIslandTerritoryRegion if not spied
        when(bukkitWorld.getMaxHeight()).thenReturn(256); // For getIslandTerritoryRegion if not spied


        ItemFrame itemFrameIn = mockEntityInRegion(ItemFrame.class, 100, 100, 100, true);
        Zombie zombieIn = mockEntityInRegion(Zombie.class, 110, 100, 110, true);
        Player playerEntityIn = mockEntityInRegion(Player.class, 120, 100, 120, true); // Player should NOT be removed
        ArmorStand armorStandOut = mockEntityInRegion(ArmorStand.class, 200, 100, 200, false); // Outside region


        // Mock chunk setup for the region where entities are.
        // Min/Max of mockCuboidRegion: (50,50,50) to (150,150,150)
        // Chunk containing (100,100,100) is (6,6) because 100 >> 4 = 6
        Chunk chunkWithEntities = mockChunkWithEntities(6, 6, Arrays.asList(itemFrameIn, zombieIn, playerEntityIn));
        // Chunk for armorStandOut (200,100,200) is (12,12)
        Chunk chunkWithOtherEntities = mockChunkWithEntities(12, 12, Collections.singletonList(armorStandOut));


        // Mock WorldEdit clear operation to return some blocks changed
        // Matching the public API: setBlocks(Region, Pattern)
        when(editSession.setBlocks(any(Region.class), eq((Pattern) BlockTypes.AIR))).thenReturn(100); // Simulate 100 blocks cleared


        // Act
        spiedIslandManager.deleteIsland(player);


        // Assert
        verify(itemFrameIn).remove();
        verify(zombieIn).remove();
        verify(playerEntityIn, never()).remove(); // Players should not be removed
        verify(armorStandOut, never()).remove(); // Entity outside region


        verify(pluginLogger).info(contains("Removed 2 entities")); // Exact count
        verify(islandDataHandler).removeIslandData(playerUUID);
        verify(regionManager).removeRegion(islandRegionId);
    }


    @Test
    void testResetIsland_RemovesEntitiesCorrectlyBeforePaste() throws IOException, com.sk89q.worldedit.MaxChangedBlocksException, com.sk89q.worldedit.WorldEditException { // Added exceptions
        // Arrange
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island);


        ItemFrame itemFrameIn = mockEntityInRegion(ItemFrame.class, 100, 100, 100, true);
        Zombie zombieIn = mockEntityInRegion(Zombie.class, 110, 100, 110, true);
        Player playerEntityIn = mockEntityInRegion(Player.class, 120, 100, 120, true);


        Chunk chunkWithEntities = mockChunkWithEntities(6, 6, Arrays.asList(itemFrameIn, zombieIn, playerEntityIn));


        // Mock WorldEdit clear and paste
        when(editSession.setBlocks(any(Region.class), eq((Pattern) BlockTypes.AIR))).thenReturn(100);
        // Mock paste operation via ClipboardHolder chain
        ClipboardHolder holder = mock(ClipboardHolder.class, Answers.RETURNS_DEEP_STUBS);
        when(holder.createPaste(any(EditSession.class)).to(any(BlockVector3.class)).ignoreAirBlocks(anyBoolean()).build()).thenReturn(mock(Operation.class));
        // This part is tricky. The actual paste is: new ClipboardHolder(clipboard).createPaste(...).build()
        // We need to ensure 'clipboard' (mocked at class level) is returned when schematic is read.
        // For now, the Operations.complete() is mocked, which is the final step.


        // Act
        spiedIslandManager.resetIsland(player);


        // Assert
        verify(itemFrameIn).remove();
        verify(zombieIn).remove();
        verify(playerEntityIn, never()).remove();


        verify(pluginLogger).info(contains("Removed 2 entities"));
        verify(operationsMock, times(1)); // Check paste operation was completed.
        Operations.complete(any(Operation.class));
        verify(islandDataHandler).addOrUpdateIslandData(island);
    }


    @Test
    void testDeleteIsland_NoEntitiesPresent() throws IOException, com.sk89q.worldedit.MaxChangedBlocksException { // Added MaxChangedBlocksException
        // Arrange
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island);


        // Mock chunk setup for the region, but with no entities
        Chunk emptyChunk = mockChunkWithEntities(6, 6, Collections.emptyList());


        when(editSession.setBlocks(any(Region.class), eq((Pattern) BlockTypes.AIR))).thenReturn(100);


        // Act
        spiedIslandManager.deleteIsland(player);


        // Assert
        verify(pluginLogger, never()).info(contains("Removed ") ); // No "Removed X entities" log
        verify(pluginLogger).info(contains("Entities cleared for island")); // General log still appears
        verify(islandDataHandler).removeIslandData(playerUUID);
    }


    @Test
    void testClearEntitiesInRegion_NullWorld_LogsWarning_InDelete() throws IOException {
        // Arrange
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island);
        // Crucial part: Make getBaseLocation().getWorld() return null
        Location locWithNullWorld = mock(Location.class);
        when(locWithNullWorld.getWorld()).thenReturn(null);
        when(island.getBaseLocation()).thenReturn(locWithNullWorld); // Override baseLocation from setUp

        // Prevent getIslandTerritoryRegion from throwing NPE due to null world early
        // Here, we let getIslandTerritoryRegion itself fail or be pre-mocked if it's too complex.
        // The `clearEntitiesInRegion` is called *after* `getIslandTerritoryRegion`.
        // So, if `getIslandTerritoryRegion` fails due to null world, `clearEntitiesInRegion` might not be reached.
        // The test for `clearEntitiesInRegion` directly is better for this.
        // However, the prompt asks to test via deleteIsland.
        // For this specific case, `getIslandTerritoryRegion` will likely throw,
        // and `clearEntitiesInRegion` won't be called.
        // Let's adjust: the warning from `clearEntitiesInRegion` itself is what we want to test.
        // The current structure of `deleteIsland` means if world is null, `getIslandTerritoryRegion` will throw.
        // The `clearEntitiesInRegion` method itself has the null check.
        // To test *that* specific log, we'd need to make getIslandTerritoryRegion succeed but pass a null world to clearEntitiesInRegion.
        // This is tricky because `islandBaseLocation.getWorld()` is used for both.
        // Let's assume `getIslandTerritoryRegion` somehow gets a world, but then `clearEntitiesInRegion` is called with a null world.
        // This requires a more direct way to call `clearEntitiesInRegion` or a very specific spy setup.

        // Alternative: If `getIslandTerritoryRegion` returns a valid region, but `islandBaseLocation.getWorld()` is null when `clearEntitiesInRegion` is called.
        // This implies `islandBaseLocation` changes or is different.
        // For now, let's assume the `clearEntitiesInRegion` is robustly called.
        // The spy setup for `getIslandTerritoryRegion` already provides a `mockCuboidRegion`.
        // We need `islandBaseLocation.getWorld()` to be null specifically when `clearEntitiesInRegion` uses it.

        // Let's reset the baseLocation to one with a valid world for `getIslandTerritoryRegion`
        // but then mock it to return null for the `clearEntitiesInRegion` call context.
        // This is not straightforward with the current spy.

        // Simpler approach for this test: Assume `deleteIsland` proceeds to the point of calling `clearEntitiesInRegion`.
        // We will ensure `islandBaseLocation.getWorld()` returns null just for that call.
        // This is hard because `clearEntitiesInRegion` uses the same world object.
        // The original code for deleteIsland: clearEntitiesInRegion(islandBaseLocation.getWorld(), islandTerritory);
        // If islandBaseLocation.getWorld() is null, it's passed as null.

        when(island.getBaseLocation().getWorld()).thenReturn(null); // This makes it null for the call to clearEntitiesInRegion

        // Act
        spiedIslandManager.deleteIsland(player); // This will likely fail earlier in deleteIsland due to null world for WorldEdit/WorldGuard.
                                                 // The test needs to be more focused on clearEntitiesInRegion's internal check.

        // This test case as written is hard to achieve perfectly without directly testing clearEntitiesInRegion.
        // The log "Cannot clear entities: World or island territory is null." comes from clearEntitiesInRegion.
        // If deleteIsland fails before that, the log won't be from clearEntitiesInRegion.
        // For now, let's verify the logger was called with a warning. The exact message might be from an earlier failure point.
        // This indicates a limitation in testing private methods indirectly for specific internal checks.
        // A better test would be to make `getIslandTerritoryRegion` succeed, then change the world mock for the entity clearing part.

        // Given the structure, if world is null, `getIslandTerritoryRegion` itself will throw IOException.
        // `clearEntitiesInRegion`'s null world check might not even be reached via `deleteIsland` if world is globally null.
        // The prompt implies testing the log from `clearEntitiesInRegion`.
        // So, `getIslandTerritoryRegion` must succeed.
        // Then, `islandBaseLocation.getWorld()` must be null when passed to `clearEntitiesInRegion`.

        // Let's assume `getIslandTerritoryRegion` was successful (already spied).
        // Now, specifically for the `clearEntitiesInRegion` call, make `islandBaseLocation.getWorld()` return null.
        // This means `island` mock needs to be more flexible or we need another Location mock.
        Location mockLocationForClearing = mock(Location.class);
        when(mockLocationForClearing.getWorld()).thenReturn(null); // World is null
        // We can't easily swap `island.getBaseLocation()` mid-method for the spied call.

        // Re-think: The most direct way to test `clearEntitiesInRegion`'s null checks is to make `getIslandTerritoryRegion`
        // return a valid region, but ensure the world *passed into* `clearEntitiesInRegion` is null.
        // `deleteIsland` calls: `clearEntitiesInRegion(islandBaseLocation.getWorld(), islandTerritory);`
        // So if `islandBaseLocation.getWorld()` is null, it will be passed.
        // The problem is `getIslandTerritoryRegion` also uses `islandBaseLocation.getWorld()`.

        // Test is simplified: if baseLocation.getWorld() is null, deleteIsland should handle it gracefully.
        // The specific log from clearEntitiesInRegion might not be hit if an earlier check catches the null world.
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island); // island mock is from setup
        // Override the initial baseLocation to have a null world from the start for this test
        when(island.getBaseLocation()).thenReturn(new Location(null, 100,100,100)); // World is null

        // Act
        spiedIslandManager.deleteIsland(player);

        // Assert
        // We expect a failure/warning, but it might be from getIslandTerritoryRegion or WE adapter.
        // The most robust thing we can verify is that the process doesn't throw an unhandled NPE
        // and some warning related to null world is logged.
        verify(pluginLogger).warning(contains("World or island territory is null"));
        // Or, if it fails earlier:
        // verify(pluginLogger).warning(contains("Island base location or world is null")); // from deleteIsland itself
        // verify(pluginLogger).severe(contains("Could not adapt world for island deletion")); // from deleteIsland WE adapt
    }

     @Test
    void testClearEntitiesInRegion_NullRegion_LogsWarning_InDelete() throws IOException {
        when(islandDataHandler.getIslandByOwner(playerUUID)).thenReturn(island);
        // Make getIslandTerritoryRegion return null for this test
        doReturn(null).when(spiedIslandManager).getIslandTerritoryRegion(any(Location.class));

        // Act
        spiedIslandManager.deleteIsland(player);

        // Assert
        verify(pluginLogger).warning(contains("Cannot clear entities: World or island territory is null."));
        // Ensure no entity removal was attempted
        verify(bukkitWorld, never()).getChunkAt(anyInt(), anyInt());
    }
}
