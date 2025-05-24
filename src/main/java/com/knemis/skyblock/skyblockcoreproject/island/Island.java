package com.knemis.skyblock.skyblockcoreproject.island;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
    private final Location baseLocation; // final yapıldı, setBaseLocation kaldırıldı
    private final Instant creationDate;
    private boolean isPublic;
    private boolean boundariesEnforced;

    // Bu alanlar final yapıldı
    private final Set<UUID> members;
    // private final Set<UUID> bannedPlayers; // Kullanılmadığı için kaldırıldı
    private final Map<String, Location> namedHomes;

    private String welcomeMessage;
    private String currentBiome;

    private transient World world;

    // Constructor for creating a new island
    public Island(UUID ownerUUID, Location baseLocation, String defaultIslandName) {
        this.ownerUUID = ownerUUID;
        this.baseLocation = baseLocation; // final olduğu için burada atanmalı
        if (this.baseLocation != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.islandName = defaultIslandName;
        this.creationDate = Instant.now();
        this.isPublic = false;
        this.boundariesEnforced = true;
        this.members = new HashSet<>();
        // this.bannedPlayers = new HashSet<>(); // Kaldırıldı
        this.namedHomes = new HashMap<>();
        this.currentBiome = null;
        this.welcomeMessage = null;
    }

    // Constructor for loading an island from data source
    // bannedPlayers parametresi kaldırıldı
    public Island(UUID ownerUUID, String islandName, Location baseLocation, long creationTimestamp,
                  boolean isPublic, boolean boundariesEnforced,
                  Set<UUID> members, /* Set<UUID> bannedPlayers, */ Map<String, Location> namedHomes,
                  String currentBiome, String welcomeMessage) {
        this.ownerUUID = ownerUUID;
        this.islandName = islandName;
        this.baseLocation = baseLocation; // final olduğu için burada atanmalı
        if (this.baseLocation != null && this.baseLocation.getWorld() != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.creationDate = Instant.ofEpochMilli(creationTimestamp);
        this.isPublic = isPublic;
        this.boundariesEnforced = boundariesEnforced;
        this.members = (members != null) ? members : new HashSet<>();
        // this.bannedPlayers = (bannedPlayers != null) ? bannedPlayers : new HashSet<>(); // Kaldırıldı
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

    // public void setBaseLocation(Location baseLocation) { // Kullanılmadığı için kaldırıldı
    // this.baseLocation = baseLocation;
    // if (this.baseLocation != null) {
    // this.world = this.baseLocation.getWorld();
    // } else {
    // this.world = null;
    // }
    // }

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
        if (memberUUID == null || memberUUID.equals(ownerUUID)) return false;
        return members.add(memberUUID);
    }

    public boolean removeMember(UUID memberUUID) {
        if (memberUUID == null) return false;
        return members.remove(memberUUID);
    }

    public boolean isMember(UUID playerUUID) {
        if (playerUUID == null) return false;
        return members.contains(playerUUID);
    }

    // Kullanılmadığı için kaldırıldı
    // public Set<UUID> getBannedPlayers() {
    // return bannedPlayers;
    // }

    // public boolean banPlayer(UUID playerUUID) {
    // if (playerUUID == null || playerUUID.equals(ownerUUID) || isMember(playerUUID)) return false;
    // return bannedPlayers.add(playerUUID);
    // }

    // public boolean unbanPlayer(UUID playerUUID) {
    // if (playerUUID == null) return false;
    // return bannedPlayers.remove(playerUUID);
    // }

    // public boolean isBanned(UUID playerUUID) {
    // if (playerUUID == null) return false;
    // return bannedPlayers.contains(playerUUID);
    // }

    public Map<String, Location> getNamedHomes() {
        return namedHomes;
    }

    public Location getNamedHome(String homeName) {
        if (homeName == null) return null;
        return namedHomes.get(homeName.toLowerCase());
    }

    // Dönüş tipi void olarak değiştirildi
    public void setNamedHome(String homeName, Location location) {
        if (homeName == null || homeName.trim().isEmpty() || location == null) {
            // İsteğe bağlı: Hatalı giriş için bir log atılabilir veya exception fırlatılabilir.
            // Şimdilik sessizce başarısız oluyor.
            return;
        }
        namedHomes.put(homeName.toLowerCase(), location.clone());
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

    public boolean canPlayerVisit(OfflinePlayer player) {
        if (player == null) return false;
        UUID playerUUID = player.getUniqueId();

        if (playerUUID.equals(ownerUUID) || isMember(playerUUID)) {
            return true;
        }
        // bannedPlayers kaldırıldığı için isBanned kontrolü de kaldırıldı.
        // if (isBanned(playerUUID)) {
        // return false;
        // }
        return isPublic;
    }
}