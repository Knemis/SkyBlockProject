package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandWelcomeListener implements Listener {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    private final IslandWelcomeManager welcomeManager;
    private final Map<UUID, UUID> lastPlayerIslandOwner; // Oyuncu UUID -> Son bulunduğu adanın sahibi UUID

    public IslandWelcomeListener(SkyBlockProject plugin, IslandManager islandManager, IslandWelcomeManager welcomeManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.welcomeManager = welcomeManager;
        this.lastPlayerIslandOwner = new HashMap<>();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerIslandEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Aynı blok içindeyse veya dünya değişmemişse işlem yapma
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld()))) {
            return;
        }

        // Sadece skyblock dünyasında çalışsın
        if (!to.getWorld().getName().equals(plugin.getConfig().getString("skyblock-world-name", "skyblock_world"))) {
            lastPlayerIslandOwner.remove(player.getUniqueId()); // Skyblock dünyasından çıkarsa bilgiyi temizle
            return;
        }

        Island islandAtTo = islandManager.getIslandAt(to);
        UUID currentIslandOwnerUUID = (islandAtTo != null) ? islandAtTo.getOwnerUUID() : null;
        UUID lastIslandOwnerUUID = lastPlayerIslandOwner.get(player.getUniqueId());

        // Eğer oyuncu farklı bir adaya girdiyse veya bir adadan çıkıp başka bir adaya girdiyse
        if (currentIslandOwnerUUID != null && !currentIslandOwnerUUID.equals(lastIslandOwnerUUID)) {
            // Oyuncu kendi adasına veya üyesi olduğu adaya giriyorsa mesaj gösterme
            if (currentIslandOwnerUUID.equals(player.getUniqueId()) || islandAtTo.isMember(player.getUniqueId())) {
                lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
                return;
            }

            String welcomeMessage = welcomeManager.getWelcomeMessage(islandAtTo);
            if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
                // Mesajı göstermeden önce bekleme süresi eklenebilir (spam önlemek için)
                // Veya oyuncuya sadece bir adaya ilk girişinde gösterilebilir.
                // Şimdilik her farklı adaya girişte gösterelim.

                // Mesajı PlaceholderAPI ile zenginleştirebiliriz (opsiyonel)
                // String formattedMessage = PlaceholderAPI.setPlaceholders(player, welcomeMessage);
                // player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMessage));
            }
        }

        // Oyuncunun son bulunduğu ada bilgisini güncelle
        if (currentIslandOwnerUUID != null) {
            lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
        } else {
            lastPlayerIslandOwner.remove(player.getUniqueId()); // Eğer bir adada değilse bilgiyi temizle
        }
    }
}