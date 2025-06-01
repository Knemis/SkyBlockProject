package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.cryptomorin.xseries.XMaterial; // For XMaterial.END_PORTAL
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;

import com.sk89q.worldedit.bukkit.BukkitAdapter; // For WorldGuard check
import com.sk89q.worldguard.WorldGuard; // For WorldGuard check
import com.sk89q.worldguard.protection.flags.Flags; // For WorldGuard StateFlag.BUILD
import com.sk89q.worldguard.bukkit.WorldGuardPlugin; // For wrapping player

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin; // For WorldGuard lookup

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerInteractListener implements Listener {

    private final SkyBlockProject plugin;
    private final boolean obsidianBucketEnabled;
    private final boolean endPortalFramePickEnabled;
    private final String cannotInteractMessage;

    public PlayerInteractListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.obsidianBucketEnabled = plugin.getConfig().getBoolean("features.obsidian-bucket.enabled", true);
        this.endPortalFramePickEnabled = plugin.getConfig().getBoolean("features.end-portal-frame-pick.enabled", true);

        String prefix = plugin.getConfig().getString("messages.prefix", "&b[SkyBlock] &r");
        this.cannotInteractMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.interaction.cannot-interact", "&cYou cannot interact here."));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand(); // Use getItemInMainHand for modern Bukkit
        Block clickedBlock = event.getClickedBlock();

        // Crystal deposit logic was here - REMOVED.

        if (clickedBlock == null) {
            return;
        }

        // Obsidian Bucket Feature
        if (obsidianBucketEnabled && action == Action.RIGHT_CLICK_BLOCK &&
            clickedBlock.getType() == Material.OBSIDIAN &&
            itemInHand != null && itemInHand.getType() == Material.BUCKET) {

            if (!canPlayerBuild(player, clickedBlock.getLocation())) {
                player.sendMessage(cannotInteractMessage);
                event.setCancelled(true);
                return;
            }

            clickedBlock.setType(Material.AIR);
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
                player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET)).values().forEach(itemStack ->
                        player.getWorld().dropItem(player.getLocation(), itemStack)
                );
            } else {
                // player.getInventory().setItemInMainHand(new ItemStack(Material.LAVA_BUCKET)); // Direct replacement
                 itemInHand.setType(Material.LAVA_BUCKET); // Mutates the existing stack
            }
            // No setCancelled needed if default behavior after changing type is fine.
            return;
        }

        // End Portal Frame Pick Feature
        if (endPortalFramePickEnabled && action == Action.LEFT_CLICK_BLOCK && player.isSneaking() &&
            clickedBlock.getType() == Material.END_PORTAL_FRAME &&
            itemInHand != null && itemInHand.getType().name().toUpperCase().contains("PICKAXE")) { // Robust check for pickaxe

            if (!canPlayerBuild(player, clickedBlock.getLocation())) {
                player.sendMessage(cannotInteractMessage);
                // event.setCancelled(true); // Left click block is complex, let WG handle final say if break event occurs
                return; // Don't proceed with custom logic
            }

            // This custom break logic circumvents normal break events and protections if not careful.
            // WG usually handles block break permissions. This is a feature to bypass that for a specific case.
            // Ensure this doesn't allow duping or unauthorized destruction if WG is misconfigured.

            // Store location before breaking if breakNaturally doesn't work as expected
            Location blockLocation = clickedBlock.getLocation().clone().add(0.5,0.5,0.5);

            // Break the block - this should trigger BlockBreakEvent if not cancelled by other plugins
            // However, this listener might act before BlockBreakEvent or other listeners.
            // For custom drop, it's often better to set to air and drop manually.
            clickedBlock.setType(Material.AIR);
            player.getWorld().dropItemNaturally(blockLocation, new ItemStack(Material.END_PORTAL_FRAME));


            Material endPortalMaterial = XMaterial.END_PORTAL.parseMaterial();
            if (endPortalMaterial != null) {
                 removeAdjacentBlocks(clickedBlock.getLocation(), endPortalMaterial, new AtomicInteger(9)); // Max 9 portal blocks
            }
            event.setCancelled(true); // Crucial to prevent default left-click actions (like starting to break)
            return;
        }

        // Generic interaction permission for other right-clicks on blocks in island worlds
        if (action == Action.RIGHT_CLICK_BLOCK) {
            IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
            // Check if in a Skyblock world managed by the plugin
            if (islandDataHandler != null && islandDataHandler.getSkyblockWorld() != null &&
                player.getWorld().equals(islandDataHandler.getSkyblockWorld())) {

                // If it's not one of the special features handled above...
                // Check general build permission for other right-click interactions.
                // This is a broad protection. Specific blocks (chests, doors, levers)
                // are usually handled by WorldGuard's USE or CHEST_ACCESS flags.
                // testBuild is a general approximation.
                if (!canPlayerBuild(player, clickedBlock.getLocation())) {
                    // Before cancelling, consider if the block is a common interactable
                    // that WG might allow via specific flags even if general build is denied.
                    // E.g., buttons, levers, doors, chests.
                    // For now, if canPlayerBuild is false, we cancel to be safe.
                    // More nuanced checks (e.g., checking block type and then specific WG flags)
                    // would be needed for finer control.
                    Material type = clickedBlock.getType();
                    boolean isCommonInteractable = type.isInteractable() &&
                                                   (type.name().contains("BUTTON") ||
                                                    type.name().contains("LEVER") ||
                                                    type.name().contains("DOOR") ||
                                                    type.name().contains("GATE") ||
                                                    type.name().contains("CHEST") ||
                                                    type.name().contains("SHULKER_BOX") ||
                                                    type == Material.CRAFTING_TABLE ||
                                                    type == Material.ANVIL ||
                                                    type == Material.ENCHANTING_TABLE ||
                                                    type == Material.ENDER_CHEST ||
                                                    type == Material.FURNACE || type.name().contains("FURNACE") ||
                                                    type == Material.BREWING_STAND ||
                                                    type == Material.JUKEBOX ||
                                                    type == Material.NOTE_BLOCK ||
                                                    type == Material.BEACON);

                    if (!isCommonInteractable) { // If not a common interactable, and can't build, deny.
                        player.sendMessage(cannotInteractMessage);
                        event.setCancelled(true);
                    }
                    // If it IS a common interactable, we assume WorldGuard's specific flags (USE, CHEST_ACCESS)
                    // will handle it. If those also deny, the interaction will fail anyway.
                    // If those allow, then this listener shouldn't block it based on general build perms.
                }
            }
        }
    }

    private boolean canPlayerBuild(Player player, Location location) {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin instanceof WorldGuardPlugin) {
            try {
                // Use the Location object directly with BukkitAdapter
                return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                        .testBuild(BukkitAdapter.adapt(location), ((WorldGuardPlugin) wgPlugin).wrapPlayer(player), Flags.BUILD);
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.WARNING, "WorldGuard build check failed: " + e.getMessage(), e);
                return false; // Fail safe if WorldGuard check throws an error
            }
        }
        // If WorldGuard is not found or not the expected type, fallback.
        // SkyBlockProject has WG as a hard dependency, so this should ideally not be reached.
        // Allowing build here means no protection if WG is missing.
        // Denying build here means players can't do anything if WG is missing.
        // Let's assume if WG is missing, something is very wrong, so deny.
        plugin.getLogger().warning("WorldGuard plugin not found or not instance of WorldGuardPlugin. Denying build by default.");
        return false;
    }

    // Recursive removal was potentially problematic. This version clears immediate neighbors.
    // The AtomicInteger can be used to limit total blocks removed if this were expanded to search further.
    public void removeAdjacentBlocks(Location centerLocation, Material targetMaterial, AtomicInteger maxBlocksToRemove) {
        if (maxBlocksToRemove.get() <= 0) {
            return;
        }
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (xOffset == 0 && yOffset == 0 && zOffset == 0) {
                        continue; // Skip the center block itself
                    }
                    if (maxBlocksToRemove.get() <= 0) { // Check limit before each block
                        return;
                    }
                    Block block = centerLocation.clone().add(xOffset, yOffset, zOffset).getBlock();
                    if (block.getType() == targetMaterial) {
                        block.setType(Material.AIR, true); // true for physics update
                        maxBlocksToRemove.getAndDecrement();
                    }
                }
            }
        }
    }
}
