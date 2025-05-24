package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages core island logic and data.
 * NOTE: This class has been significantly refactored. Most of its previous responsibilities
 * have been delegated to more specific manager classes such as IslandDataHandler,
 * IslandLifecycleManager, IslandSettingsManager, IslandMemberManager, and IslandTeleportManager.
 *
 * This class might be further reduced or eliminated if all functionalities are covered
 * by the new specific managers and accessed via the main SkyBlockProject plugin class.
 */
public class IslandManager {

    private final SkyBlockProject plugin;
    // private World skyblockWorld; // Moved to IslandDataHandler
    // private File schematicFile; // Handled by IslandLifecycleManager
    // private File islandsFile; // Handled by IslandDataHandler
    // private FileConfiguration islandsConfig; // Handled by IslandDataHandler
    // private Map<UUID, Island> islandsData; // Handled by IslandDataHandler

    public IslandManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        // Initialization of schematicFile, islandsFile, islandsData, etc.,
        // is now handled by IslandDataHandler and IslandLifecycleManager.
        // Loading of the skyblockWorld and island data is also handled by IslandDataHandler.
        plugin.getLogger().info("IslandManager initialized (Refactored). Functionalities are now primarily in specific managers.");
    }

    // Most methods previously in IslandManager are now in their respective specific managers:
    // - Island creation/deletion/reset -> IslandLifecycleManager
    // - Island data loading/saving -> IslandDataHandler
    // - Island settings (name, public, boundary) -> IslandSettingsManager
    // - Island members -> IslandMemberManager
    // - Island homes and teleportation -> IslandTeleportManager & IslandDataHandler
    // - Biome management -> IslandBiomeManager
    // - Welcome messages -> IslandWelcomeManager
    // - Flag management -> IslandFlagManager & FlagGUIManager

    // Utility methods that might have been here, like getRegionId, getIslandTerritoryRegion,
    // are now typically within IslandLifecycleManager or directly accessed/calculated where needed
    // using IslandDataHandler and plugin.getRegionManager().

    /**
     * Retrieves the WorldGuard RegionManager for a given Bukkit world.
     * This is a utility function, the primary way to get RegionManager is plugin.getRegionManager(world).
     * @param bukkitWorld The Bukkit world.
     * @return The RegionManager for that world, or null if not found.
     */
    public RegionManager getWGRegionManager(World bukkitWorld) {
        return plugin.getRegionManager(bukkitWorld); // Delegate to main plugin class
    }

    /**
     * Gets an Island object by its owner's UUID.
     * Delegated to IslandDataHandler.
     * @param ownerUUID The UUID of the island owner.
     * @return The Island object, or null if not found.
     */
    public Island getIslandByOwner(UUID ownerUUID) {
        return plugin.getIslandDataHandler().getIslandByOwner(ownerUUID);
    }

    /**
     * Gets an Island object for a given player.
     * Delegated to IslandDataHandler.
     * @param player The player.
     * @return The Island object, or null if the player doesn't own an island.
     */
    public Island getIsland(Player player) {
        return plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
    }

    /**
     * Checks if a player has an island.
     * Delegated to IslandDataHandler.
     * @param playerUUID The UUID of the player.
     * @return True if the player has an island, false otherwise.
     */
    public boolean playerHasIsland(UUID playerUUID) {
        return plugin.getIslandDataHandler().playerHasIsland(playerUUID);
    }


    // The following methods were in the old IslandManager. Their functionalities are now
    // primarily handled by IslandLifecycleManager and IslandDataHandler.
    // They are removed from here to avoid duplication and confusion.

    // public void loadSkyblockWorld() { /* Moved to IslandDataHandler */ }
    // public void loadIslands() { /* Moved to IslandDataHandler */ }
    // public void saveAllIslandData() { /* Moved to IslandDataHandler */ }
    // public void saveIslandData(Island island) { /* Moved to IslandDataHandler */ }
    // private String getRegionId(UUID playerUUID) { /* Now in IslandLifecycleManager or internal to it */ }
    // public Island getIslandAt(Location location) { /* Requires iteration and WG checks - likely in IslandDataHandler or via IslandLifecycleManager */ }
    // public ProtectedRegion getProtectedRegion(UUID ownerUUID) { /* IslandLifecycleManager or direct WG calls via plugin.getRegionManager() */ }
    // public void createIsland(Player player) { /* Moved to IslandLifecycleManager */ }
    // public boolean deleteIsland(Player player) { /* Moved to IslandLifecycleManager */ }
    // public boolean resetIsland(Player player) { /* Moved to IslandLifecycleManager */ }
    // private CuboidRegion getPastedSchematicRegion(Location islandBaseLocation) throws IOException { /* Now in IslandLifecycleManager */ }
    // public CuboidRegion getIslandTerritoryRegion(Location islandBaseLocation) throws IOException { /* Now in IslandLifecycleManager */ }
    // public boolean setNamedHome(Player player, String homeName, Location homeLocation) { /* IslandTeleportManager for logic, IslandDataHandler for persistence */ }
    // public boolean deleteNamedHome(Player player, String homeName) { /* IslandTeleportManager for logic, IslandDataHandler for persistence */ }
    // public List<String> getNamedHomesList(Player player) { /* Island object or IslandTeleportManager */ }
    // public void teleportToIsland(Player player) { /* Moved to IslandTeleportManager */ }
    // public void teleportToNamedHome(Player player, String homeName) { /* Moved to IslandTeleportManager */ }
    // public boolean setIslandName(Player player, String newName) { /* Moved to IslandSettingsManager */ }
    // public boolean setIslandPublic(Player player, boolean isPublic) { /* Moved to IslandSettingsManager */ }
    // public boolean toggleIslandBoundaries(Player player) { /* Moved to IslandSettingsManager */ }
    // public boolean addIslandMember(Player owner, OfflinePlayer targetPlayer) { /* Moved to IslandMemberManager */ }
    // public boolean removeIslandMember(Player owner, OfflinePlayer targetPlayer) { /* Moved to IslandMemberManager */ }
    // public List<OfflinePlayer> getIslandMembers(UUID ownerUUID) { /* Moved to IslandMemberManager */ }

    // EmptyWorldGenerator is now in IslandDataHandler or SkyBlockProject if it's a general utility.
    // For this refactor, it's better placed in IslandDataHandler.

    // If there are any shared utility methods that were in IslandManager and don't logically fit
    // into any of the new specific managers, they could remain here. However, the goal of
    // such a refactor is usually to make classes more focused.
}