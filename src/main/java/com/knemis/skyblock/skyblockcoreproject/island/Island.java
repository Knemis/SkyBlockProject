package com.knemis.skyblock.skyblockcoreproject.island;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer; // For canPlayerVisit
import org.bukkit.World;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Island {

    private final UUID ownerUUID;
    private String islandName;
    private Location baseLocation;
    private final Instant creationDate;
    private boolean isPublic;
    private boolean boundariesEnforced;

    private Set<UUID> members;
    private Set<UUID> bannedPlayers;
    private Map<String, Location> namedHomes;
    private String welcomeMessage;
    private String currentBiome;

    private transient World world; // To be derived from baseLocation

    // Constructor for creating a new island
    public Island(UUID ownerUUID, Location baseLocation, String defaultIslandName) {
        this.ownerUUID = ownerUUID;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.islandName = defaultIslandName;
        this.creationDate = Instant.now();
        this.isPublic = false; // Default to private
        this.boundariesEnforced = true; // Default to boundaries enforced
        this.members = new HashSet<>();
        this.bannedPlayers = new HashSet<>();
        this.namedHomes = new HashMap<>();
        this.currentBiome = null; // Default biome (or could be set from schematic's actual biome)
        this.welcomeMessage = null; // No welcome message by default
    }

    // Constructor for loading an island from data source (e.g., YAML)
    public Island(UUID ownerUUID, String islandName, Location baseLocation, long creationTimestamp,
                  boolean isPublic, boolean boundariesEnforced,
                  Set<UUID> members, Set<UUID> bannedPlayers, Map<String, Location> namedHomes,
                  String currentBiome, String welcomeMessage) {
        this.ownerUUID = ownerUUID;
        this.islandName = islandName;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null && this.baseLocation.getWorld() != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.creationDate = Instant.ofEpochMilli(creationTimestamp);
        this.isPublic = isPublic;
        this.boundariesEnforced = boundariesEnforced;
        this.members = (members != null) ? members : new HashSet<>();
        this.bannedPlayers = (bannedPlayers != null) ? bannedPlayers : new HashSet<>();
        this.namedHomes = (namedHomes != null) ? namedHomes : new HashMap<>();
        this.currentBiome = currentBiome;
        this.welcomeMessage = welcomeMessage;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Location getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(Location baseLocation) {
        this.baseLocation = baseLocation;
        if (this.baseLocation != null) {
            this.world = this.baseLocation.getWorld();
        } else {
            this.world = null;
        }
    }

    public World getWorld() {
        if (world == null && baseLocation != null) {
            world = baseLocation.getWorld();
        }
        return world;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public long getCreationTimestamp() {
        return creationDate.toEpochMilli();
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean areBoundariesEnforced() {
        return boundariesEnforced;
    }

    public void setBoundariesEnforced(boolean boundariesEnforced) {
        this.boundariesEnforced = boundariesEnforced;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean addMember(UUID memberUUID) {
        if (memberUUID.equals(ownerUUID)) return false; // Owner cannot be a member of their own island
        return members.add(memberUUID);
    }

    public boolean removeMember(UUID memberUUID) {
        return members.remove(memberUUID);
    }

    public boolean isMember(UUID playerUUID) {
        return members.contains(playerUUID);
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public boolean banPlayer(UUID playerUUID) {
        if (playerUUID.equals(ownerUUID) || isMember(playerUUID)) return false; // Owner or members cannot be banned
        return bannedPlayers.add(playerUUID);
    }

    public boolean unbanPlayer(UUID playerUUID) {
        return bannedPlayers.remove(playerUUID);
    }

    public boolean isBanned(UUID playerUUID) {
        return bannedPlayers.contains(playerUUID);
    }

    public Map<String, Location> getNamedHomes() {
        return namedHomes;
    }

    public Location getNamedHome(String homeName) {
        if (homeName == null) return null;
        return namedHomes.get(homeName.toLowerCase());
    }

    public boolean setNamedHome(String homeName, Location location) {
        if (homeName == null || location == null) return false;
        // Further validation (max homes, name pattern, location within island bounds)
        // should be handled by a manager class (e.g., IslandTeleportManager or IslandSettingsManager)
        // before calling this method.
        namedHomes.put(homeName.toLowerCase(), location.clone());
        return true;
    }

    public boolean deleteNamedHome(String homeName) {
        if (homeName == null) return false;
        return namedHomes.remove(homeName.toLowerCase()) != null;
    }

    public List<String> getHomeNames() {
        return new ArrayList<>(namedHomes.keySet());
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getCurrentBiome() {
        return currentBiome;
    }

    public void setCurrentBiome(String currentBiome) {
        this.currentBiome = currentBiome;
    }

    /**
     * Helper method to check if a player can visit (considering bans and privacy).
     * @param player The player to check.
     * @return True if the player can visit, false otherwise.
     */
    public boolean canPlayerVisit(OfflinePlayer player) {
        if (player == null) return false;
        UUID playerUUID = player.getUniqueId();

        if (playerUUID.equals(ownerUUID) || isMember(playerUUID)) {
            return true; // Owner and members can always visit
        }
        if (isBanned(playerUUID)) {
            return false; // Banned players cannot visit
        }
        return isPublic; // Otherwise, depends on public status
    }
}