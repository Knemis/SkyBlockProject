package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer; // EKLENDİ: OfflinePlayer importu
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class IslandTeleportManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;

    public IslandTeleportManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
    }

    public void teleportPlayerToIslandSpawn(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Henüz bir adanız yok! Oluşturmak için /island create yazın.");
            return;
        }

        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanızın konumu veya dünyası bulunamadı. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().warning("teleportPlayerToIslandSpawn: " + player.getName() + " için ada temel konumu veya dünyası null.");
            return;
        }

        double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
        double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
        double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);

        Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
        teleportLocation.setYaw(player.getLocation().getYaw());
        teleportLocation.setPitch(player.getLocation().getPitch());

        safeTeleport(player, teleportLocation, ChatColor.GREEN + "Adanızın ana noktasına ışınlandınız!");
    }

    public void teleportPlayerToNamedHome(Player player, String homeName) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Evine ışınlanabilmek için önce bir ada oluşturmalısın!");
            return;
        }

        Location homeLocation = island.getNamedHome(homeName.toLowerCase());
        if (homeLocation == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adında bir ev noktan bulunmuyor. Evlerini listelemek için /island home list yaz.");
            return;
        }

        if (homeLocation.getWorld() == null || Bukkit.getWorld(homeLocation.getWorld().getName()) == null) {
            player.sendMessage(ChatColor.RED + "'" + homeName + "' adlı evinin bulunduğu dünya yüklenemedi! Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().warning("teleportPlayerToNamedHome: " + player.getName() + " için '" + homeName + "' evinin dünyası null veya yüklenemiyor.");
            return;
        }
        safeTeleport(player, homeLocation, ChatColor.GREEN + "'" + homeName + "' adlı evine ışınlandın!");
    }

    public void teleportPlayerToVisitIsland(Player visitor, Island targetIsland) {
        if (targetIsland == null) {
            visitor.sendMessage(ChatColor.RED + "Ziyaret edilecek ada bulunamadı.");
            return;
        }
        Location islandBaseLocation = targetIsland.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            visitor.sendMessage(ChatColor.RED + "Bu adanın konumu bulunamadı veya dünya yüklenemedi.");
            plugin.getLogger().warning("teleportPlayerToVisitIsland: " + visitor.getName() + " için hedef ada ("+targetIsland.getOwnerUUID()+") temel konumu veya dünyası null.");
            return;
        }

        double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
        double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
        double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);

        Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
        teleportLocation.setYaw(visitor.getLocation().getYaw());
        teleportLocation.setPitch(visitor.getLocation().getPitch());

        OfflinePlayer owner = Bukkit.getOfflinePlayer(targetIsland.getOwnerUUID()); // OfflinePlayer burada kullanılıyor
        String ownerName = owner.getName() != null ? owner.getName() : "Bilinmeyen Oyuncu"; // owner.getName() burada kullanılıyor

        safeTeleport(visitor, teleportLocation, ChatColor.GREEN + ownerName + " adlı oyuncunun adasına ışınlandın!");
    }

    private void safeTeleport(Player player, Location location, String successMessage) {
        if (location == null || location.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Işınlanma konumu geçersiz. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().severe("safeTeleport: " + player.getName() + " için konum veya dünya null geldi.");
            return;
        }

        World world = location.getWorld();
        if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            world.loadChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }

        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                if (location.getChunk().isLoaded() || attempts >= 20) {
                    if (location.getChunk().isLoaded()){
                        player.teleport(location);
                        if (successMessage != null && !successMessage.isEmpty()) {
                            player.sendMessage(successMessage);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Işınlanma noktası yüklenemedi, lütfen tekrar deneyin.");
                        plugin.getLogger().warning("safeTeleport: " + player.getName() + " için chunk yüklenemedi: " + location.toString());
                    }
                    this.cancel();
                }
                attempts++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}