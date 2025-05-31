package com.knemis.skyblock.skyblockcoreproject.rankmanager.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory; // Added
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerInteractionListener implements Listener {

    private final SkyBlockProject plugin;
    private final OwnerGUIManager ownerGuiManager;
    private final Set<UUID> lockedPlayers = new HashSet<>();

    public PlayerInteractionListener(SkyBlockProject plugin, OwnerGUIManager ownerGuiManager) {
        this.plugin = plugin;
        this.ownerGuiManager = ownerGuiManager;
    }

    public void addPlayerToLockdown(UUID playerId) {
        lockedPlayers.add(playerId);
        Player p = Bukkit.getPlayer(playerId);
        if (p != null) plugin.getLogger().info("Oyuncu " + p.getName() + " GUI için şimdi kilitlendi.");
    }

    public void removePlayerFromLockdown(UUID playerId) {
        lockedPlayers.remove(playerId);
        Player p = Bukkit.getPlayer(playerId);
        if (p != null) plugin.getLogger().info("Oyuncu " + p.getName() + " kilidi şimdi AÇILDI.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (lockedPlayers.contains(player.getUniqueId())) {
            Inventory topInventory = event.getView().getTopInventory();
            if (ownerGuiManager.isOurGui(topInventory)) {
                ItemStack clickedItem = event.getCurrentItem();
                ownerGuiManager.handleGuiClick(player, clickedItem, topInventory); // Pass topInventory
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
                player.sendMessage(SkyBlockProject.PLUGIN_PREFIX + ChatColor.RED + "Önce kritik yetki güncellemesine yanıt vermelisiniz!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (lockedPlayers.contains(player.getUniqueId()) && ownerGuiManager.isOurGui(event.getInventory())) {
            if (plugin.getOwnersPendingConfirmation().contains(player.getUniqueId())) {
                plugin.getLogger().info(player.getName() + " GUI'den ESC ile çıkmaya çalıştı. Yeniden açılıyor...");
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (lockedPlayers.contains(player.getUniqueId()) && plugin.getOwnersPendingConfirmation().contains(player.getUniqueId())) {
                        ownerGuiManager.openReloadGui(player);
                    }
                }, 1L);
            } else {
                removePlayerFromLockdown(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        lockedPlayers.remove(playerId);
        plugin.getOwnersPendingConfirmation().remove(playerId); // Ensure they are also removed from pending confirmation
    }

    private boolean checkAndBlockIfLocked(Player player, org.bukkit.event.Cancellable event, String actionMessage) {
        if (lockedPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(SkyBlockProject.PLUGIN_PREFIX + ChatColor.RED + "Önce kritik yetki güncellemesine yanıt vermelisiniz! (" + actionMessage + " engellendi)");
            // Check if our GUI is NOT open, but player is still locked and pending confirmation
            // This implies the GUI was closed without resolving (e.g. server lag, another plugin)
            if (!ownerGuiManager.isOurGui(player.getOpenInventory().getTopInventory()) &&
                plugin.getOwnersPendingConfirmation().contains(player.getUniqueId())) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    // Double check state before reopening
                    if (lockedPlayers.contains(player.getUniqueId()) &&
                        plugin.getOwnersPendingConfirmation().contains(player.getUniqueId())) {
                         ownerGuiManager.openReloadGui(player);
                    }
                }, 1L);
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        checkAndBlockIfLocked(event.getPlayer(), event, "Hareket");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase().split(" ")[0];
        if (command.startsWith("/srm") || command.startsWith("/rankmanageradmin")) {
            if (event.getPlayer().hasPermission("skyblockrankmanager.admin")) return;
        }
        checkAndBlockIfLocked(event.getPlayer(), event, "Komutlar");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkAndBlockIfLocked(event.getPlayer(), event, "Etkileşim");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        checkAndBlockIfLocked(event.getPlayer(), event, "Eşya Bırakma");
    }
}
