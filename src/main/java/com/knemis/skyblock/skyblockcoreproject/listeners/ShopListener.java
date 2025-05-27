package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // New addition
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent; // Import for InventoryCloseEvent
import org.bukkit.event.inventory.InventoryCloseEvent;  // New addition
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID; // Added import

import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopType; // Keep for old finalizeShopSetup if needed by other parts, though ideally it's removed

public class ShopListener implements Listener {
    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;
    private final IslandDataHandler islandDataHandler;
    private final ShopVisitGUIManager shopVisitGUIManager;
    private final ShopAdminGUIManager shopAdminGUIManager; // New addition

    public ShopListener(SkyBlockProject plugin,
                        ShopManager shopManager,
                        ShopSetupGUIManager shopSetupGUIManager,
                        IslandDataHandler islandDataHandler,
                        ShopVisitGUIManager shopVisitGUIManager,
                        ShopAdminGUIManager shopAdminGUIManager) { // Added to constructor
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopSetupGUIManager = shopSetupGUIManager;
        this.islandDataHandler = islandDataHandler;
        this.shopVisitGUIManager = shopVisitGUIManager;
        this.shopAdminGUIManager = shopAdminGUIManager; // Assignment done
    }

    @EventHandler
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
        return;
    }

    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null || (clickedBlock.getType() != Material.CHEST && clickedBlock.getType() != Material.TRAPPED_CHEST)) {
        return;
    }

    Player player = event.getPlayer();
    Location chestLocation = clickedBlock.getLocation();

    if (player.isSneaking()) { // Shift + Right Click
        // event.setCancelled(true); // Already done by previous logic if it's an existing shop of theirs

        // Check if player is already in a shop creation process
        // TODO: Ensure getPlayerChoosingShopMode() is implemented in SkyBlockProject.java, returning a Map<UUID, Location>
        if (plugin.getPlayerShopSetupState().containsKey(player.getUniqueId()) || plugin.getPlayerChoosingShopMode().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are already in a shop creation process. Type 'cancel' to abort.");
            event.setCancelled(true);
            return;
        }

        // Permission Check
        if (!player.hasPermission("skyblock.shop.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create shops.");
            event.setCancelled(true);
            return;
        }

        // Location Check (Island Ownership/Membership)
        Island island = plugin.getIslandDataHandler().getIslandAt(chestLocation);
        boolean canCreateHere = false;

        if (player.hasPermission("skyblock.admin.createshopanywhere")) {
            // Admin can create anywhere, but we still log if island is null,
            // as it might be unexpected depending on the location.
            if (island == null) {
                plugin.getLogger().info("Admin " + player.getName() + " is creating a shop at location " + chestLocation.toString() + " where no island is present (this is allowed for admins).");
            }
            canCreateHere = true;
        } else {
            // Non-admins MUST have an island at the location
            if (island == null) {
                player.sendMessage(ChatColor.RED + "You cannot create a shop here as there is no island at this location.");
                plugin.getLogger().warning("Player " + player.getName() + " (UUID: " + player.getUniqueId() + ") tried to create a shop at " + chestLocation.toString() + " but no island was found (getIslandAt returned null).");
                event.setCancelled(true);
                return;
            }
            // If an island exists, check for ownership/membership
            if (island.isOwner(player.getUniqueId()) || island.isMember(player.getUniqueId())) {
                canCreateHere = true;
            }
        }

        // This secondary check for canCreateHere handles the case where a non-admin player is not on their island / member island.
        // For admins, canCreateHere would already be true.
        if (!canCreateHere) {
            player.sendMessage(ChatColor.RED + "You can only create shops on your island or an island you are a member of.");
            event.setCancelled(true);
            return;
        }

        // Shop Existence Check (Handles both pending and active)
        Shop existingShop = shopManager.getActiveShop(chestLocation);
        if (existingShop == null) {
            existingShop = shopManager.getPendingShop(chestLocation);
        }

        if (existingShop != null) {
            if (existingShop.getOwnerUUID().equals(player.getUniqueId())) {
                if (existingShop.isSetupComplete()) {
                    // It's their own active shop, open admin menu
                    shopAdminGUIManager.openAdminMenu(player, existingShop);
                } else {
                    // It's their own pending shop, let them continue setup (e.g., by opening item selection)
                    player.sendMessage(ChatColor.YELLOW + "Resuming setup for this pending shop.");
                    plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation); // Ensure state for GUI listener
                    shopSetupGUIManager.openItemSelectionMenu(player, existingShop);
                }
            } else {
                player.sendMessage(ChatColor.RED + "A shop owned by " + Bukkit.getOfflinePlayer(existingShop.getOwnerUUID()).getName() + " already exists here.");
            }
            event.setCancelled(true);
            return;
        }

        // Initiate Mode Choice if no shop exists
        plugin.getPlayerChoosingShopMode().put(player.getUniqueId(), chestLocation);
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Choose a shop mode for this chest location:");
        player.sendMessage(ChatColor.GREEN + "Type 'market'" + ChatColor.GRAY + " - Players open chest, select custom amount.");
        player.sendMessage(ChatColor.GREEN + "Type 'bank'" + ChatColor.GRAY + "   - Players click sign/chest to buy fixed bundles.");
        player.sendMessage(ChatColor.RED + "Type 'cancel'" + ChatColor.GRAY + " - Abort shop creation.");
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");
        event.setCancelled(true);

    } else { // Normal Right Click (not sneaking)
        Shop activeShop = shopManager.getActiveShop(chestLocation);

        if (activeShop != null && activeShop.isSetupComplete()) {
            event.setCancelled(true);
            if (!activeShop.getOwnerUUID().equals(player.getUniqueId())) {
                plugin.getPlayerViewingShopLocation().put(player.getUniqueId(), chestLocation);
                shopVisitGUIManager.openShopVisitMenu(player, activeShop);
            } else {
                event.setCancelled(false); // Their own shop, let them open the chest
            }
        } else {
            Shop pendingShop = shopManager.getPendingShop(chestLocation);
            if (pendingShop != null && pendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                event.setCancelled(true); // Prevent normal chest opening
                // If a shop is pending, direct them to the next step of setup
                // This depends on how `ShopSetupGUIManager` handles resuming pending shops.
                // For now, let's assume it might try to restart or guide them.
                // The current setup flow via chat mode selection might make this part less relevant
                // if `getPlayerShopSetupState` is primarily for GUI driven setup.
                // However, if a player logs out mid-setup, this could be a resume point.
                // The new chat-based setup doesn't use `getPlayerShopSetupState` in the same way.
                // It uses `getPlayerChoosingShopMode` first.
                // We might need to re-evaluate how to resume a pending shop setup
                // if it was initiated via chat but not completed.
                // For now, trying to open the item selection menu might be a good default.
                if (plugin.getShopSetupGUIManager() != null) {
                    plugin.getShopSetupGUIManager().openItemSelectionMenu(player, pendingShop);
                    player.sendMessage(ChatColor.YELLOW + "Resuming shop setup: Please select an item.");
                } else {
                    player.sendMessage(ChatColor.RED + "Error: Shop setup GUI is unavailable.");
                }
            }
            // else: Not a shop, or not owner's pending shop: normal chest interaction.
        }
    }
}

@EventHandler
public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();

    // TODO: Ensure getPlayerChoosingShopMode() is implemented in SkyBlockProject.java, returning a Map<UUID, Location>
    if (plugin.getPlayerChoosingShopMode().containsKey(playerId)) {
        event.setCancelled(true);
        // TODO: Ensure getPlayerChoosingShopMode() is implemented in SkyBlockProject.java, returning a Map<UUID, Location>
        Location chestLocation = plugin.getPlayerChoosingShopMode().get(playerId);
        String message = event.getMessage().toLowerCase().trim();
        ShopMode selectedMode = null;

        if (message.equals("market")) {
            selectedMode = ShopMode.MARKET_CHEST;
        } else if (message.equals("bank")) {
            selectedMode = ShopMode.BANK_CHEST;
        } else if (message.equals("cancel")) {
            // TODO: Ensure getPlayerChoosingShopMode() is implemented in SkyBlockProject.java, returning a Map<UUID, Location>
            plugin.getPlayerChoosingShopMode().remove(playerId);
            player.sendMessage(ChatColor.YELLOW + "Shop creation cancelled.");
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid choice. Type 'market', 'bank', or 'cancel'.");
            return;
        }

        final ShopMode finalSelectedMode = selectedMode;
        // TODO: Ensure getPlayerChoosingShopMode() is implemented in SkyBlockProject.java, returning a Map<UUID, Location>
        plugin.getPlayerChoosingShopMode().remove(playerId); // Remove before task to prevent re-entry

        new BukkitRunnable() {
            @Override
            public void run() {
                // Re-check if shop exists, as it might have been created by another process/admin
                // between chat input and this runnable execution.
                if (plugin.getShopManager().isShop(chestLocation)) {
                    player.sendMessage(ChatColor.RED + "A shop was created at this location while you were choosing. Please try again.");
                    return;
                }

                Shop newShop = plugin.getShopManager().initiateShopCreation(chestLocation, player, finalSelectedMode);
                if (newShop != null) {
                    if (plugin.getShopSetupGUIManager() != null) {
                        // The player is now in the item selection phase.
                        // The ShopSetupListener handles the rest of the GUI flow from here.
                        // We also need to put them into the general shop setup state.
                        plugin.getPlayerShopSetupState().put(playerId, chestLocation);
                        plugin.getShopSetupGUIManager().openItemSelectionMenu(player, newShop);
                        player.sendMessage(ChatColor.GREEN + "Shop mode '" + finalSelectedMode.name() + "' selected. Now, please select the item for your shop.");
                    } else {
                        plugin.getLogger().severe("ShopSetupGUIManager is null! Cannot open item selection menu.");
                        player.sendMessage(ChatColor.RED + "Error: Shop setup GUI could not be opened. Please contact an admin.");
                    }
                } else {
                    // initiateShopCreation should send a message if it fails,
                    // but we can add a generic one here if it doesn't.
                    player.sendMessage(ChatColor.RED + "Failed to initiate shop creation. Please ensure the location is valid and try again.");
                }
            }
        }.runTask(plugin);
    }
    // Other chat handlers for other states (like price input) are in ShopSetupListener
}


@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;
    Player player = (Player) event.getWhoClicked();
    Inventory topInventory = event.getView().getTopInventory();
    if (topInventory == null) return;

    // Your previous GUI title checks can remain here (ShopSetupGUIManager etc.)
    // ...

    // New Shop Admin GUI click management
    if (event.getView().getTitle().equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Location shopLocation = plugin.getPlayerAdministeringShop().get(player.getUniqueId());
        if (shopLocation == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not find shop information to manage.");
            player.closeInventory();
            return;
        }
        Shop shop = shopManager.getActiveShop(shopLocation);
        if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Error: Shop not found or you do not have permission to manage this shop.");
            player.closeInventory();
            plugin.getPlayerAdministeringShop().remove(player.getUniqueId()); // Clear state
            return;
        }

        // Control based on slot numbers in ShopAdminGUIManager
        // It would be better to get these slot numbers statically from the ShopAdminGUIManager class.
        // For now, let's use default values (11 and 13).
        // Match the actual slots with ShopAdminGUIManager.DISPLAY_NAME_SLOT and ShopAdminGUIManager.PRICE_SLOT.
        int displayNameSlot = 11; // Should match DISPLAY_NAME_SLOT in ShopAdminGUIManager
        int priceSlot = 13;       // Should match PRICE_SLOT in ShopAdminGUIManager

        if (event.getRawSlot() == displayNameSlot) {
            shopAdminGUIManager.initiateDisplayNameChange(player, shop);
        } else if (event.getRawSlot() == priceSlot) {
            shopAdminGUIManager.initiatePriceChange(player, shop);
        }
        // Else if blocks can be added here for other management buttons to be added in the future.
    }
    // Click management for other GUIs (ShopSetupListener etc. should be managed in their own event handlers)
}

@EventHandler
public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player)) return;
    Player player = (Player) event.getPlayer();
    UUID playerId = player.getUniqueId();

    // Clear state when ShopVisitGUI is closed
    if (event.getView().getTitle().equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
        if (plugin.getPlayerViewingShopLocation().containsKey(playerId)) {
            plugin.getPlayerViewingShopLocation().remove(playerId);
        }
    }
    // Clear relevant states when ShopAdminGUI is closed
    else if (event.getView().getTitle().equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
        plugin.getPlayerAdministeringShop().remove(playerId);
        // If the player closed the GUI while about to type something in chat,
        // also clear the expected input state and send a message.
        ShopAdminGUIManager.AdminInputType expectedInput = plugin.getPlayerWaitingForAdminInput().remove(playerId);
        if (expectedInput != null) {
            player.sendMessage(ChatColor.YELLOW + "Shop setting input cancelled.");
        }
    }
    // State cleaning for ShopSetupGUIs should be done within ShopSetupListener.
}
}