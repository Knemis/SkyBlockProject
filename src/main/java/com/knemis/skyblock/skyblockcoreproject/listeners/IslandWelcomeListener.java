package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // IslandManager yerine IslandDataHandler
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
    // IslandManager alanı IslandDataHandler ile değiştirildi
    private final IslandDataHandler islandDataHandler;
    private final IslandWelcomeManager welcomeManager;
    private final Map<UUID, UUID> lastPlayerIslandOwner; // Oyuncu UUID -> Son bulunduğu adanın sahibi UUID

    // Constructor güncellendi: IslandManager yerine IslandDataHandler
    public IslandWelcomeListener(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandWelcomeManager welcomeManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
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

        // IslandDataHandler üzerinden ada bilgisini al
        // IslandDataHandler.getIslandAt(Location) metodu, WorldGuard bölgelerini kontrol ederek
        // o konumdaki adayı bulmalıdır. Bu metodun IslandDataHandler içinde implemente edilmesi gerekecek.
        Island islandAtTo = islandDataHandler.getIslandAt(to);
        UUID currentIslandOwnerUUID = (islandAtTo != null) ? islandAtTo.getOwnerUUID() : null;
        UUID lastIslandOwnerUUID = lastPlayerIslandOwner.get(player.getUniqueId());

        // Eğer oyuncu farklı bir adaya girdiyse veya bir adadan çıkıp başka bir adaya girdiyse
        if (currentIslandOwnerUUID != null && !currentIslandOwnerUUID.equals(lastIslandOwnerUUID)) {
            // Oyuncu kendi adasına veya üyesi olduğu adaya giriyorsa mesaj gösterme
            if (currentIslandOwnerUUID.equals(player.getUniqueId()) || (islandAtTo != null && islandAtTo.isMember(player.getUniqueId()))) {
                lastPlayerIslandOwner.put(player.getUniqueId(), currentIslandOwnerUUID);
                return;
            }

            String welcomeMessage = welcomeManager.getWelcomeMessage(islandAtTo);
            if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
                // Mesajı PlaceholderAPI ile zenginleştirebiliriz (opsiyonel)
                // String formattedMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, welcomeMessage);
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