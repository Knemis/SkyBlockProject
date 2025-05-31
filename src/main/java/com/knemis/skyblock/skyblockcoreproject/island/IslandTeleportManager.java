package com.knemis.skyblock.skyblockcoreproject.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
            player.sendMessage(Component.text("Henüz bir adanız yok! Oluşturmak için /island create yazın.", NamedTextColor.RED));
            return;
        }

        Location islandBaseLocation = island.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(Component.text("Adanızın konumu veya dünyası bulunamadı. Lütfen bir yetkiliye bildirin.", NamedTextColor.RED));
            plugin.getLogger().warning("teleportPlayerToIslandSpawn: " + player.getName() + " için ada temel konumu veya dünyası null.");
            return;
        }

        double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
        double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
        double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);

        Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
        teleportLocation.setYaw(player.getLocation().getYaw());
        teleportLocation.setPitch(player.getLocation().getPitch());

        safeTeleport(player, teleportLocation, Component.text("Adanızın ana noktasına ışınlandınız!", NamedTextColor.GREEN));
    }

    public void teleportPlayerToNamedHome(Player player, String homeName) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Evine ışınlanabilmek için önce bir ada oluşturmalısın!", NamedTextColor.RED));
            return;
        }

        Location homeLocation = island.getNamedHome(homeName.toLowerCase());
        if (homeLocation == null) {
            player.sendMessage(Component.text("'" + homeName + "' adında bir ev noktan bulunmuyor. Evlerini listelemek için /island home list yaz.", NamedTextColor.RED));
            return;
        }

        if (homeLocation.getWorld() == null || Bukkit.getWorld(homeLocation.getWorld().getName()) == null) {
            player.sendMessage(Component.text("'" + homeName + "' adlı evinin bulunduğu dünya yüklenemedi! Lütfen bir yetkiliye bildirin.", NamedTextColor.RED));
            plugin.getLogger().warning("teleportPlayerToNamedHome: " + player.getName() + " için '" + homeName + "' evinin dünyası null veya yüklenemiyor.");
            return;
        }
        safeTeleport(player, homeLocation, Component.text("'" + homeName + "' adlı evine ışınlandın!", NamedTextColor.GREEN));
    }

    public void teleportPlayerToVisitIsland(Player visitor, Island targetIsland) {
        if (targetIsland == null) {
            visitor.sendMessage(Component.text("Ziyaret edilecek ada bulunamadı.", NamedTextColor.RED));
            return;
        }
        Location islandBaseLocation = targetIsland.getBaseLocation();
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            visitor.sendMessage(Component.text("Bu adanın konumu bulunamadı veya dünya yüklenemedi.", NamedTextColor.RED));
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

        safeTeleport(visitor, teleportLocation, Component.text(ownerName + " adlı oyuncunun adasına ışınlandın!", NamedTextColor.GREEN));
    }

    private void safeTeleport(Player player, Location location, Component successMessage) { // Changed String to Component
        if (location == null || location.getWorld() == null) {
            player.sendMessage(Component.text("Işınlanma konumu geçersiz. Lütfen bir yetkiliye bildirin.", NamedTextColor.RED));
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
                        if (successMessage != null) { // Check if component is not null
                            player.sendMessage(successMessage);
                        }
                    } else {
                        player.sendMessage(Component.text("Işınlanma noktası yüklenemedi, lütfen tekrar deneyin.", NamedTextColor.RED));
                        plugin.getLogger().warning("safeTeleport: " + player.getName() + " için chunk yüklenemedi: " + location.toString());
                    }
                    this.cancel();
                }
                attempts++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}