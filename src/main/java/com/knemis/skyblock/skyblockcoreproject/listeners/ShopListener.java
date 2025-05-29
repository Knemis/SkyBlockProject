package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.ShopAdminGUIManager; // New addition
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;

import org.bukkit.Bukkit;
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
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) SHIFT+RIGHT_CLICKED block %s at %s to potentially create/administer shop.",
                    player.getName(), player.getUniqueId(), clickedBlock.getType(), chestLocation));

            if (plugin.getPlayerShopSetupState().containsKey(player.getUniqueId()) || plugin.getPlayerChoosingShopMode().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "You are already in a shop creation process. Type 'cancel' to abort.");
                event.setCancelled(true);
                return;
            }

            if (!player.hasPermission("skyblock.shop.create")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to create shops.");
                plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) lacks skyblock.shop.create permission for chest at %s.",
                        player.getName(), player.getUniqueId(), chestLocation));
                event.setCancelled(true);
                return;
            }

            Island island = plugin.getIslandDataHandler().getIslandAt(chestLocation);
            boolean canCreateHere = false;

            if (player.hasPermission("skyblock.admin.createshopanywhere")) {
                if (island == null) {
                    plugin.getLogger().info(String.format("ShopListener: Admin %s (UUID: %s) is creating a shop at non-island location %s (allowed).",
                            player.getName(), player.getUniqueId(), chestLocation));
                }
                canCreateHere = true;
            } else {
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "You cannot create a shop here as there is no island at this location.");
                    plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) tried to create shop at %s but no island was found (non-admin).",
                            player.getName(), player.getUniqueId(), chestLocation));
                    event.setCancelled(true);
                    return;
                }
                if (island.isOwner(player.getUniqueId()) || island.isMember(player.getUniqueId())) {
                    canCreateHere = true;
                }
            }

            if (!canCreateHere) {
                player.sendMessage(ChatColor.RED + "You can only create shops on your island or an island you are a member of.");
                plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) tried to create shop at %s on island %s, but is not owner/member.",
                        player.getName(), player.getUniqueId(), chestLocation, (island != null ? island.getRegionId() : "NULL_ISLAND_OBJ")));
                event.setCancelled(true);
                return;
            }

            Shop existingShop = shopManager.getActiveShop(chestLocation);
            if (existingShop == null) {
                existingShop = shopManager.getPendingShop(chestLocation);
            }

            if (existingShop != null) {
                if (existingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    if (existingShop.isSetupComplete()) {
                        shopAdminGUIManager.openAdminMenu(player, existingShop);
                        plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) is owner of existing active shop at %s. Opening admin menu.",
                                player.getName(), player.getUniqueId(), chestLocation));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Resuming setup for this pending shop.");
                        plugin.getPlayerShopSetupState().put(player.getUniqueId(), chestLocation);
                        shopSetupGUIManager.openItemSelectionMenu(player, existingShop);
                        plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) is owner of existing pending shop at %s. Opening item selection menu to resume setup.",
                                player.getName(), player.getUniqueId(), chestLocation));
                    }
                } else {
                    String ownerName = Bukkit.getOfflinePlayer(existingShop.getOwnerUUID()).getName();
                    player.sendMessage(ChatColor.RED + "A shop owned by " + ownerName + " already exists here.");
                    plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) tried to create shop at %s, but it's owned by %s.",
                            player.getName(), player.getUniqueId(), chestLocation, ownerName));
                }
                event.setCancelled(true);
                return;
            }

            plugin.getPlayerChoosingShopMode().put(player.getUniqueId(), chestLocation);
            player.sendMessage(ChatColor.GOLD + "------------------------------------------");
            player.sendMessage(ChatColor.YELLOW + "Choose a shop mode for this chest location:");
            player.sendMessage(ChatColor.GREEN + "Type 'market'" + ChatColor.GRAY + " - Players open chest, select custom amount.");
            player.sendMessage(ChatColor.GREEN + "Type 'bank'" + ChatColor.GRAY + "   - Players click sign/chest to buy fixed bundles.");
            player.sendMessage(ChatColor.RED + "Type 'cancel'" + ChatColor.GRAY + " - Abort shop creation.");
            player.sendMessage(ChatColor.GOLD + "------------------------------------------");
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) initiating shop mode choice for new shop at %s.",
                    player.getName(), player.getUniqueId(), chestLocation));
            event.setCancelled(true);

        } else { // Normal Right Click (not sneaking)
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) RIGHT_CLICKED block %s at %s.",
                    player.getName(), player.getUniqueId(), clickedBlock.getType(), chestLocation));
            Shop activeShop = shopManager.getActiveShop(chestLocation);

            if (activeShop != null && activeShop.isSetupComplete()) {
                event.setCancelled(true);
                if (!activeShop.getOwnerUUID().equals(player.getUniqueId())) {
                    plugin.getPlayerViewingShopLocation().put(player.getUniqueId(), chestLocation);
                    shopVisitGUIManager.openShopVisitMenu(player, activeShop);
                    plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) opening active shop (Owner: %s) at %s for visit.",
                            player.getName(), player.getUniqueId(), Bukkit.getOfflinePlayer(activeShop.getOwnerUUID()).getName(), chestLocation));
                } else {
                    event.setCancelled(false); // Their own shop, let them open the chest
                    plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) opening their own active shop chest at %s (normal interaction).",
                            player.getName(), player.getUniqueId(), chestLocation));
                }
            } else {
                Shop pendingShop = shopManager.getPendingShop(chestLocation);
                if (pendingShop != null && pendingShop.getOwnerUUID().equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    if (plugin.getShopSetupGUIManager() != null) {
                        plugin.getShopSetupGUIManager().openItemSelectionMenu(player, pendingShop);
                        player.sendMessage(ChatColor.YELLOW + "Resuming shop setup: Please select an item.");
                        plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) right-clicked their own pending shop at %s. Attempting to resume setup by opening item selection.",
                                player.getName(), player.getUniqueId(), chestLocation));
                    } else {
                        player.sendMessage(ChatColor.RED + "Error: Shop setup GUI is unavailable.");
                        plugin.getLogger().severe("ShopListener: ShopSetupGUIManager is null when trying to resume pending shop for " + player.getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (plugin.getPlayerChoosingShopMode().containsKey(playerId)) {
            Location chestLocation = plugin.getPlayerChoosingShopMode().get(playerId); // Get before removing
            String message = event.getMessage().toLowerCase().trim();
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) in shop mode selection chat state (Location: %s). Message: '%s'",
                    player.getName(), playerId, chestLocation, message));
            event.setCancelled(true);

            ShopMode selectedMode = null;

            if (message.equals("market")) {
                selectedMode = ShopMode.MARKET_CHEST;
            } else if (message.equals("bank")) {
                selectedMode = ShopMode.BANK_CHEST;
            } else if (message.equals("cancel")) {
                plugin.getPlayerChoosingShopMode().remove(playerId);
                player.sendMessage(ChatColor.YELLOW + "Shop creation cancelled.");
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) cancelled shop creation at mode selection for location %s.",
                        player.getName(), playerId, chestLocation));
                return;
            } else {
                player.sendMessage(ChatColor.RED + "Invalid choice. Type 'market', 'bank', or 'cancel'.");
                plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) made invalid shop mode choice: '%s' for location %s.",
                        player.getName(), playerId, message, chestLocation));
                return;
            }

            final ShopMode finalSelectedMode = selectedMode;
            plugin.getPlayerChoosingShopMode().remove(playerId);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.getShopManager().isShop(chestLocation)) {
                        player.sendMessage(ChatColor.RED + "A shop was created at this location while you were choosing. Please try again.");
                        plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) chose mode for %s, but shop was created concurrently by another process.",
                                player.getName(), playerId, chestLocation));
                        return;
                    }

                    Shop newShop = plugin.getShopManager().initiateShopCreation(chestLocation, player, finalSelectedMode);
                    if (newShop != null) {
                        plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) selected shop mode '%s' for chest at %s. Shop initiated.",
                                player.getName(), playerId, finalSelectedMode.name(), chestLocation));
                        if (plugin.getShopSetupGUIManager() != null) {
                            plugin.getPlayerShopSetupState().put(playerId, chestLocation);
                            plugin.getShopSetupGUIManager().openItemSelectionMenu(player, newShop);
                            player.sendMessage(ChatColor.GREEN + "Shop mode '" + finalSelectedMode.name() + "' selected. Now, please select the item for your shop.");
                            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) successfully initiated shop at %s with mode %s. Opening item selection. (Internal ID: %s)",
                                    player.getName(), playerId, Shop.locationToString(newShop.getLocation()), finalSelectedMode.name(), newShop.getShopId()));
                        } else {
                            plugin.getLogger().severe("ShopSetupGUIManager is null! Cannot open item selection menu for " + player.getName());
                            player.sendMessage(ChatColor.RED + "Error: Shop setup GUI could not be opened. Please contact an admin.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to initiate shop creation. Please ensure the location is valid and try again.");
                        plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) selected mode %s for %s, but shopManager.initiateShopCreation failed.",
                                player.getName(), playerId, finalSelectedMode.name(), chestLocation));
                    }
                }
            }.runTask(plugin);
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory == null) return;

        String viewTitle = event.getView().getTitle(); // GetTitle returns String, not Component
        ItemStack currentItem = event.getCurrentItem();
        String currentItemName = (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) ?
                                 currentItem.getItemMeta().getDisplayName() :
                                 (currentItem != null ? currentItem.getType().name() : "null");


        if (viewTitle.equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) clicked in Shop Admin GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), player.getUniqueId(), viewTitle, event.getRawSlot(), currentItemName));
            event.setCancelled(true);
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            Location shopLocation = plugin.getPlayerAdministeringShop().get(player.getUniqueId());
            Shop shop = (shopLocation != null) ? shopManager.getActiveShop(shopLocation) : null;

            if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Error: Shop not found or you do not have permission to manage this shop.");
                plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) in Shop Admin GUI, but shop location/object is null or not owner. ShopLoc: %s, Shop: %s",
                        player.getName(), player.getUniqueId(), shopLocation, shop));
                player.closeInventory();
                plugin.getPlayerAdministeringShop().remove(player.getUniqueId());
                return;
            }

            int displayNameSlot = ShopAdminGUIManager.DISPLAY_NAME_SLOT;
            int priceSlot = ShopAdminGUIManager.PRICE_SLOT;

            if (event.getRawSlot() == displayNameSlot) {
                shopAdminGUIManager.initiateDisplayNameChange(player, shop);
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) initiated display name change for shop at %s via admin GUI.",
                        player.getName(), player.getUniqueId(), Shop.locationToString(shopLocation)));
            } else if (event.getRawSlot() == priceSlot) {
                shopAdminGUIManager.initiatePriceChange(player, shop);
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) initiated price change for shop at %s via admin GUI.",
                        player.getName(), player.getUniqueId(), Shop.locationToString(shopLocation)));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        String viewTitle = event.getView().getTitle(); // GetTitle returns String

        if (viewTitle.equals(ShopVisitGUIManager.SHOP_VISIT_TITLE)) {
            if (plugin.getPlayerViewingShopLocation().remove(playerId) != null) {
                 plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) closed Shop Visit GUI. Cleared viewing state.", player.getName(), playerId));
            }
        } else if (viewTitle.equals(ShopAdminGUIManager.SHOP_ADMIN_TITLE)) {
            if (plugin.getPlayerAdministeringShop().remove(playerId) != null) {
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) closed Shop Admin GUI. Cleared admin state.", player.getName(), playerId));
            }
            ShopAdminGUIManager.AdminInputType expectedInput = plugin.getPlayerWaitingForAdminInput().remove(playerId);
            if (expectedInput != null) {
                player.sendMessage(ChatColor.YELLOW + "Shop setting input cancelled.");
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) cancelled shop admin input for %s by closing GUI.",
                        player.getName(), playerId, expectedInput.name()));
            }
        }
    }
}