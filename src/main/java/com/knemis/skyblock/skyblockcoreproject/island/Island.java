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
    private final Location baseLocation;
    private final Instant creationDate;
    private boolean isPublic;
    private boolean boundariesEnforced;

    private final Set<UUID> members;
    private final Map<String, Location> namedHomes;

    private String welcomeMessage;
    private String currentBiome;
    private int maxHomesLimit; // Adaya özel maksimum ev limiti
    private double islandWorth;     // YENİ: Adanın hesaplanmış değeri
    private int islandLevel;        // YENİ: Adanın mevcut seviyesi

    private transient World world;

    // Constructor for creating a new island
    // DÜZELTME: initialMaxHomes parametresi eklendi
    public Island(UUID ownerUUID, Location baseLocation, String defaultIslandName, int initialMaxHomes) {
        this.ownerUUID = ownerUUID;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.islandName = defaultIslandName;
        this.creationDate = Instant.now();
        this.isPublic = false;
        this.boundariesEnforced = true;
        this.members = new HashSet<>();
        this.namedHomes = new HashMap<>();
        this.currentBiome = null;
        this.welcomeMessage = null;
        this.maxHomesLimit = initialMaxHomes;
        this.islandWorth = 0.0; // Başlangıç değeri
        this.islandLevel = 1;   // Başlangıç seviyesi
    }

    // Constructor for loading an island from data source
    // Bu constructor zaten doğruydu, maxHomesLimit parametresini alıyordu.
    public Island(UUID ownerUUID, String islandName, Location baseLocation, long creationTimestamp,
                  boolean isPublic, boolean boundariesEnforced,
                  Set<UUID> members, Map<String, Location> namedHomes,
                  String currentBiome, String welcomeMessage,
                  int maxHomesLimit, double islandWorth, int islandLevel) { // Son 3 parametre önemli
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
        this.namedHomes = (namedHomes != null) ? namedHomes : new HashMap<>();
        this.currentBiome = currentBiome;
        this.welcomeMessage = welcomeMessage;
        this.maxHomesLimit = maxHomesLimit;
        this.islandWorth = islandWorth;         // Bu satır eklenmiş olmalı
        this.islandLevel = islandLevel;         // Bu satır eklenmiş olmalı
    }

    // ... (getOwnerUUID() ve diğer getter/setter'lar aynı kalacak) ...

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
    public double getIslandWorth() {
        return islandWorth;
    }

    public void setIslandWorth(double islandWorth) {
        this.islandWorth = islandWorth;
    }

    public int getIslandLevel() {
        return islandLevel;
    }

    public void setIslandLevel(int islandLevel) {
        this.islandLevel = islandLevel;
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

    public Map<String, Location> getNamedHomes() {
        return namedHomes;
    }

    public Location getNamedHome(String homeName) {
        if (homeName == null) return null;
        return namedHomes.get(homeName.toLowerCase());
    }

    public void setNamedHome(String homeName, Location location) {
        if (homeName == null || homeName.trim().isEmpty() || location == null) {
            return;
        }
        namedHomes.put(homeName.toLowerCase(), location.clone());
    }

    public int getMaxHomesLimit() {
        return maxHomesLimit;
    }

    public void setMaxHomesLimit(int maxHomesLimit) {
        this.maxHomesLimit = maxHomesLimit;
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
        return isPublic;
    }
}