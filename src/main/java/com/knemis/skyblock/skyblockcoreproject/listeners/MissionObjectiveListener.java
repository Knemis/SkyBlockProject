package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MissionObjectiveListener implements Listener {

    private final SkyBlockProject plugin;
    private final MissionManager missionManager;

    public MissionObjectiveListener(SkyBlockProject plugin, MissionManager missionManager) {
        this.plugin = plugin;
        this.missionManager = missionManager;
    }

    // --- GATHER_ITEM Objective Listeners ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check gather objectives when player joins, in case they gathered items while offline
        // or to update missions that might not have registered for some reason.
        final Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                missionManager.checkGatherItemObjectives(player);
            }
        }.runTaskLater(plugin, 20L); // Delay slightly to allow other join processes
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        // Inventory updates might not be immediate, schedule a check for the next tick.
        new BukkitRunnable() {
            @Override
            public void run() {
                missionManager.checkGatherItemObjectives(player);
            }
        }.runTask(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        // Crafting involves clicking, and the result is placed on cursor.
        // The check should ideally run after the item is actually in the player's main inventory.
        // InventoryClickEvent might be more reliable, or check after a slight delay.
        new BukkitRunnable() {
            @Override
            public void run() {
                missionManager.checkGatherItemObjectives(player);
            }
        }.runTask(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        InventoryType inventoryType = event.getInventory().getType();

        // Check only if the click could result in items being added to player's main inventory
        boolean checkNeeded = false;
        InventoryAction action = event.getAction();

        // Picking up items from a block inventory or crafting output
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action.name().startsWith("PICKUP_")) {
                checkNeeded = true;
            }
        }
        // Placing items into player inventory from cursor
        else if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
             if (action.name().startsWith("PLACE_") || action == InventoryAction.SWAP_WITH_CURSOR) {
                 checkNeeded = true;
             }
        }
        // Shift-clicking from a non-player inventory to player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
             checkNeeded = true;
        }


        if (checkNeeded) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    missionManager.checkGatherItemObjectives(player);
                }
            }.runTask(plugin);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        // This is for items gathered from breaking blocks.
        // The actual item drop and pickup is handled by EntityPickupItemEvent or auto-pickup plugins.
        // However, some custom scenarios might add items directly on break.
        // A delayed check ensures inventory has updated.
        new BukkitRunnable() {
            @Override
            public void run() {
                missionManager.checkGatherItemObjectives(player);
            }
        }.runTask(plugin);
    }

    // --- PLACE_BLOCKS Objective Listener ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material placedBlockMaterial = event.getBlockPlaced().getType();
        missionManager.updatePlaceBlockProgress(player, placedBlockMaterial);
    }
    
    // ISLAND_LEVEL objectives are updated by calling a method in MissionManager directly
    // when the island level changes, typically from IslandWorthManager or a command.
}
