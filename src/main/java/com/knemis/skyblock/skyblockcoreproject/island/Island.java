package com.knemis.skyblock.skyblockcoreproject.island;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer; // OfflinePlayer kullanmak daha iyi olabilir
import org.bukkit.World; // World importu eklendi

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant; // Oluşturulma tarihi için

public class Island {

    private final UUID ownerUUID;
    private String islandName; // Ada ismi
    private Location baseLocation; // Ada şematiğinin yapıştırıldığı temel nokta
    private final Instant creationDate; // Ada oluşturulma tarihi
    private boolean isPublic; // Herkes ziyaret edebilir mi?
    private boolean boundariesEnforced; // Sınır kontrolü aktif mi?


    private Set<UUID> members; // Ada üyelerinin UUID'leri
    private Set<UUID> bannedPlayers; // Adadan yasaklanan oyuncuların UUID'leri (ziyaret edemezler)
    private Map<String, Location> namedHomes; // İsimlendirilmiş evler
    private String welcomeMessage;

    private transient World world; // Ada dünyası (geçici, baseLocation'dan alınacak)
    private String currentBiome;
    // Yeni ada oluşturulurken kullanılacak constructor
    public Island(UUID ownerUUID, Location baseLocation, String defaultIslandName) {
        this.ownerUUID = ownerUUID;
        this.baseLocation = baseLocation;
        this.world = baseLocation.getWorld();
        this.islandName = defaultIslandName; // Başlangıçta varsayılan bir isim atanabilir
        this.creationDate = Instant.now();
        this.isPublic = false; // Varsayılan olarak özel
        this.boundariesEnforced = true; // Varsayılan olarak sınırlar aktif
        this.members = new HashSet<>();
        this.bannedPlayers = new HashSet<>();
        this.namedHomes = new HashMap<>();
        this.currentBiome = null;
        this.welcomeMessage = null;
    }

    // Veri kaynağından (örn: islands.yml) yüklenirken kullanılacak constructor
    public Island(UUID ownerUUID, String islandName, Location baseLocation, long creationTimestamp,
                  boolean isPublic, boolean boundariesEnforced,
                  Set<UUID> members, Set<UUID> bannedPlayers, Map<String, Location> namedHomes,
                  String currentBiome, String welcomeMessage) { // welcomeMessage parametresi EKLENDİ
        this.ownerUUID = ownerUUID;
        this.islandName = islandName;
        this.baseLocation = baseLocation;
        if (this.baseLocation != null && this.baseLocation.getWorld() != null) { // getWorld() null kontrolü eklendi
            this.world = this.baseLocation.getWorld();
        }
        this.creationDate = Instant.ofEpochMilli(creationTimestamp);
        this.isPublic = isPublic;
        this.boundariesEnforced = boundariesEnforced;
        this.members = members != null ? members : new HashSet<>();
        this.bannedPlayers = bannedPlayers != null ? bannedPlayers : new HashSet<>();
        this.namedHomes = namedHomes != null ? namedHomes : new HashMap<>();
        this.currentBiome = currentBiome; // Parametreden gelen değer atanıyor
        this.welcomeMessage = welcomeMessage;
    }

    // Getter ve Setter'lar (currentBiome için zaten vardı, doğru)

    // Getter ve Setter'lar



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
        if (baseLocation != null) {
            this.world = baseLocation.getWorld();
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
        if (memberUUID.equals(ownerUUID)) return false; // Sahip zaten üye sayılmaz (doğrudan yetkili)
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
        if (playerUUID.equals(ownerUUID) || isMember(playerUUID)) return false; // Sahip veya üyeler banlanamaz
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
        return namedHomes.get(homeName.toLowerCase());
    }

    public boolean setNamedHome(String homeName, Location location) {
        // İsimlendirme kuralları ve maksimum ev sayısı kontrolü IslandManager'da yapılabilir.
        namedHomes.put(homeName.toLowerCase(), location.clone());
        return true;
    }

    public boolean deleteNamedHome(String homeName) {
        return namedHomes.remove(homeName.toLowerCase()) != null;
    }

    public List<String> getHomeNames() {
        return new ArrayList<>(namedHomes.keySet());
    }

    // Helper method to check if a player can visit (considering bans and privacy)
    public boolean canPlayerVisit(OfflinePlayer player) {
        if (player.getUniqueId().equals(ownerUUID) || isMember(player.getUniqueId())) {
            return true; // Sahip ve üyeler her zaman ziyaret edebilir
        }
        if (isBanned(player.getUniqueId())) {
            return false; // Yasaklıysa ziyaret edemez
        }
        return isPublic; // Herkese açıksa ziyaret edebilir
    }

    public String getWelcomeMessage() { // EKLENDİ
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) { // EKLENDİ
        this.welcomeMessage = welcomeMessage;
    }

    public String getCurrentBiome() {
        return currentBiome;
    }

    public void setCurrentBiome(String currentBiome) {
        this.currentBiome = currentBiome;
    }

    // İleride eklenecek özellikler için yer tutucular:
    // - Ada seviyesi
    // - Ada bankası
    // - Üye rolleri ve izinleri (daha detaylı)
    // - Ada yükseltmeleri
}