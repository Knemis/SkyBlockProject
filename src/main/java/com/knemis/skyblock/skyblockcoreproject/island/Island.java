package com.knemis.skyblock.skyblockcoreproject.island;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
// Gerekirse Bukkit.getLogger() için import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private int maxHomesLimit;
    private double islandWorth;
    private int islandLevel;

    private String regionId; // Bölge ID'si

    private transient World world;

    // Yeni ada oluşturmak için yapıcı metot
    public Island(UUID ownerUUID, Location baseLocation, String defaultIslandName, int initialMaxHomes) {
        if (ownerUUID == null) throw new IllegalArgumentException("Owner UUID cannot be null for a new island.");
        // baseLocation null olabilir (örneğin, ada oluşturulurken koordinatlar henüz belirlenmemişse ve sonra ayarlanacaksa),
        // ancak null ise getWorld() gibi metotlar düzgün çalışmayacaktır.
        // IslandLifecycleManager'da baseLocation her zaman dolu geliyor.

        this.ownerUUID = ownerUUID;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.islandName = (defaultIslandName != null && !defaultIslandName.isEmpty()) ? defaultIslandName : "Ada-" + ownerUUID.toString().substring(0, 8);
        this.creationDate = Instant.now();
        this.isPublic = false;
        this.boundariesEnforced = true;
        this.members = new HashSet<>();
        this.namedHomes = new HashMap<>();
        this.maxHomesLimit = initialMaxHomes;
        this.islandWorth = 0.0;
        this.islandLevel = 1;
        // regionId, WorldGuard ve diğer sistemlerle tutarlılık için belirli bir formatta olmalı.
        this.regionId = "skyblock_island_" + ownerUUID.toString(); // TUTARLI FORMAT
    }

    // Veri kaynağından ada yüklemek için yapıcı metot
    public Island(UUID ownerUUID, String islandName, Location baseLocation, long creationTimestamp,
                  boolean isPublic, boolean boundariesEnforced,
                  Set<UUID> members, Map<String, Location> namedHomes,
                  String currentBiome, String welcomeMessage,
                  int maxHomesLimit, double islandWorth, int islandLevel, String regionId) { // regionId parametresi
        if (ownerUUID == null) throw new IllegalArgumentException("Owner UUID cannot be null when loading an island.");

        this.ownerUUID = ownerUUID;
        this.islandName = islandName;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null && this.baseLocation.getWorld() != null) {
            this.world = this.baseLocation.getWorld();
        }
        this.creationDate = Instant.ofEpochMilli(creationTimestamp);
        this.isPublic = isPublic;
        this.boundariesEnforced = boundariesEnforced;
        this.members = (members != null) ? new HashSet<>(members) : new HashSet<>(); // Savunmacı kopya
        this.namedHomes = (namedHomes != null) ? new HashMap<>(namedHomes) : new HashMap<>(); // Savunmacı kopya
        this.currentBiome = currentBiome;
        this.welcomeMessage = welcomeMessage;
        this.maxHomesLimit = maxHomesLimit;
        this.islandWorth = islandWorth;
        this.islandLevel = islandLevel;

        // Yüklenen regionId null veya boş ise, bir fallback olarak ownerUUID'den türet.
        if (regionId == null || regionId.trim().isEmpty()) {
            this.regionId = "skyblock_island_" + ownerUUID.toString(); // TUTARLI FORMAT
            // Opsiyonel: Bu durumun loglanması, veri bütünlüğü sorunlarını tespit etmeye yardımcı olabilir.
            // import org.bukkit.Bukkit;
            // Bukkit.getLogger().warning("[SkyBlock] Loaded island for " + ownerUUID + " with missing or empty regionId. Generated default: " + this.regionId);
        } else {
            this.regionId = regionId;
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // YENİ EKLENEN METOT: isOwner
    /**
     * Verilen UUID'nin bu adanın sahibi olup olmadığını kontrol eder.
     * @param playerUUID Kontrol edilecek oyuncunun UUID'si.
     * @return Oyuncu adanın sahibiyse true, değilse false.
     */
    public boolean isOwner(UUID playerUUID) {
        if (playerUUID == null) {
            return false;
        }
        return this.ownerUUID.equals(playerUUID);
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Location getBaseLocation() {
        return baseLocation != null ? baseLocation.clone() : null; // Savunmacı kopya
    }

    public Location getSpawnPoint() {
        return this.baseLocation != null ? this.baseLocation.clone() : null;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        if (regionId == null || regionId.trim().isEmpty()) {
            this.regionId = "skyblock_island_" + this.ownerUUID.toString(); // TUTARLI FORMAT
            return;
        }
        this.regionId = regionId;
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

    public boolean areBoundariesEnforced() {
        return boundariesEnforced;
    }

    public void setBoundariesEnforced(boolean boundariesEnforced) {
        this.boundariesEnforced = boundariesEnforced;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(new HashSet<>(members)); // Değiştirilemez kopya
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
        Map<String, Location> defensiveCopy = new HashMap<>();
        for (Map.Entry<String, Location> entry : namedHomes.entrySet()) {
            if (entry.getValue() != null) {
                defensiveCopy.put(entry.getKey(), entry.getValue().clone());
            } else {
                defensiveCopy.put(entry.getKey(), null);
            }
        }
        return Collections.unmodifiableMap(defensiveCopy);
    }

    public Location getNamedHome(String homeName) {
        if (homeName == null) return null;
        Location loc = namedHomes.get(homeName.toLowerCase());
        return loc != null ? loc.clone() : null;
    }

    public void setNamedHome(String homeName, Location location) {
        if (homeName == null || homeName.trim().isEmpty() || location == null || location.getWorld() == null) {
            return;
        }
        if (namedHomes.size() >= maxHomesLimit && !namedHomes.containsKey(homeName.toLowerCase())) {
            return;
        }
        namedHomes.put(homeName.toLowerCase(), location.clone());
    }

    public int getMaxHomesLimit() {
        return maxHomesLimit;
    }

    public void setMaxHomesLimit(int maxHomesLimit) {
        this.maxHomesLimit = Math.max(0, maxHomesLimit);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Island island = (Island) o;
        return ownerUUID.equals(island.ownerUUID) && Objects.equals(regionId, island.regionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerUUID, regionId);
    }
}