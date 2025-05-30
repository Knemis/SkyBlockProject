package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.PlayerShopAdminGUIManager; // Renamed import
import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.gui.shopvisit.ShopVisitGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags; // Added for new custom flag
import com.sk89q.worldguard.protection.flags.StateFlag; // Added for StateFlag.State
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession; // Added import

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
import java.util.Map; // Added import
import java.util.HashMap; // Added import

import com.knemis.skyblock.skyblockcoreproject.shop.ShopMode;

public class ShopListener implements Listener {
    private final SkyBlockProject plugin;
    private final Map<UUID, Location> playerChoosingShopMode = new HashMap<>();
    private final ShopManager shopManager;
    private final ShopSetupGUIManager shopSetupGUIManager;
    private final IslandDataHandler islandDataHandler;
    private final ShopVisitGUIManager shopVisitGUIManager;
    private final PlayerShopAdminGUIManager shopAdminGUIManager; // Renamed field type

    public ShopListener(SkyBlockProject plugin,
                        ShopManager shopManager,
                        ShopSetupGUIManager shopSetupGUIManager,
                        IslandDataHandler islandDataHandler,
                        ShopVisitGUIManager shopVisitGUIManager,
                        PlayerShopAdminGUIManager shopAdminGUIManager) { // Renamed constructor parameter type
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

            ShopSetupSession existingSession = plugin.getShopSetupGUIManager().getPlayerSession(player.getUniqueId());
            boolean inShopSetupState = existingSession != null && existingSession.getPendingShop() != null && existingSession.getCurrentGuiTitle() != null && (existingSession.getCurrentGuiTitle().equals(ShopSetupGUIManager.ITEM_SELECT_TITLE.toString()) || existingSession.getCurrentGuiTitle().equals(ShopSetupGUIManager.QUANTITY_INPUT_TITLE.toString()) || existingSession.getCurrentGuiTitle().equals(ShopSetupGUIManager.PRICE_INPUT_TITLE.toString()) || existingSession.getCurrentGuiTitle().equals(ShopSetupGUIManager.CONFIRMATION_TITLE.toString()));
            boolean inChoosingModeState = this.playerChoosingShopMode.containsKey(player.getUniqueId());
            if (inShopSetupState || inChoosingModeState) {
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
                        System.out.println("[TRACE] In ShopListener.onPlayerInteract (SHIFT+RIGHT_CLICK), about to open admin menu for player " + player.getName() + " and shop " + existingShop.getShopId());
                        shopAdminGUIManager.openAdminMenu(player, existingShop);
                        plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) is owner of existing active shop at %s. Opening admin menu.",
                                player.getName(), player.getUniqueId(), chestLocation));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Resuming setup for this pending shop.");
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

            this.playerChoosingShopMode.put(player.getUniqueId(), chestLocation);
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
                    // ADD THIS CHECK:
                    Island island = plugin.getIslandDataHandler().getIslandAt(chestLocation);
                    if (island != null && CustomFlags.VISITOR_SHOP_USE != null) {
                        StateFlag.State shopUseState = plugin.getIslandFlagManager().getIslandFlagState(island.getOwnerUUID(), CustomFlags.VISITOR_SHOP_USE);

                        // Determine effective state: if null, use default from IslandFlagManager, which should be ALLOW.
                        // If IslandFlagManager.getIslandFlagState returns null, it means the region doesn't have it set,
                        // so WorldGuard's default for that flag applies (which we set to true/ALLOW during registration).
                        // Or, if our IslandFlagManager's default map has an entry, that's the effective default for our plugin.
                        StateFlag.State effectiveState = shopUseState;
                        if (effectiveState == null) { // Not set on region, check our plugin's default for this flag
                            effectiveState = plugin.getIslandFlagManager().getDefaultStateForFlag(CustomFlags.VISITOR_SHOP_USE);
                        }

                        if (effectiveState == StateFlag.State.DENY) {
                            player.sendMessage(ChatColor.RED + "The owner has disabled visitor shop access on this island.");
                            plugin.getLogger().info(String.format("ShopListener: Player %s denied access to shop at %s due to VISITOR_SHOP_USE flag being DENY.",
                                                    player.getName(), chestLocation));
                            return; // Do not open GUI
                        }
                    }
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

        if (this.playerChoosingShopMode.containsKey(playerId)) {
            Location chestLocation = this.playerChoosingShopMode.get(playerId); // Get before removing
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
                this.playerChoosingShopMode.remove(playerId);
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
            this.playerChoosingShopMode.remove(playerId);

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
                        
                        // Create the setup session
                        // Assuming initialStockItem is not handled at this stage of interaction.
                        // If the chest had items, that logic would need to be added before or during initiateShopCreation.
                        // For now, passing null for initialStockItem.
                        plugin.getShopSetupGUIManager().createSession(player, chestLocation, newShop, null);

                        if (plugin.getShopSetupGUIManager() != null) {
                            // plugin.getPlayerShopSetupState().put(playerId, chestLocation); // This state is now in the session
                            plugin.getShopSetupGUIManager().openItemSelectionMenu(player, newShop);
                            player.sendMessage(ChatColor.GREEN + "Shop mode '" + finalSelectedMode.name() + "' selected. Now, please select the item for your shop.");
                            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) successfully initiated shop at %s with mode %s. Session created. Opening item selection. (Internal ID: %s)",
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


        if (viewTitle.equals(PlayerShopAdminGUIManager.SHOP_ADMIN_TITLE.toString())) { // Updated to use renamed class and .toString() for Component
            System.out.println("[TRACE] In ShopListener.onInventoryClick, viewTitle matches PlayerShopAdminGUIManager.SHOP_ADMIN_TITLE. Player: " + player.getName() + ", Slot: " + event.getRawSlot());
            plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) clicked in PlayerShop Admin GUI: '%s', Slot: %d, Item: %s", // Updated log
                    player.getName(), player.getUniqueId(), viewTitle, event.getRawSlot(), currentItemName));
            event.setCancelled(true);
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            Location shopLocation = shopAdminGUIManager.getPlayerAdministeringShop().get(player.getUniqueId()); // Use playerShopAdminGUIManager
            Shop shop = (shopLocation != null) ? shopManager.getActiveShop(shopLocation) : null;

            if (shop == null || !shop.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Error: Shop not found or you do not have permission to manage this shop.");
                plugin.getLogger().warning(String.format("ShopListener: Player %s (UUID: %s) in Shop Admin GUI, but shop location/object is null or not owner. ShopLoc: %s, Shop: %s",
                        player.getName(), player.getUniqueId(), shopLocation, shop));
                player.closeInventory();
                shopAdminGUIManager.getPlayerAdministeringShop().remove(player.getUniqueId()); // Use playerShopAdminGUIManager
                return;
            }

            int displayNameSlot = PlayerShopAdminGUIManager.DISPLAY_NAME_SLOT; // Updated to use renamed class
            System.out.println("[TRACE] In ShopListener.onInventoryClick, PlayerShopAdminGUIManager.DISPLAY_NAME_SLOT is " + displayNameSlot);
            int priceSlot = PlayerShopAdminGUIManager.PRICE_SLOT; // Updated to use renamed class
            System.out.println("[TRACE] In ShopListener.onInventoryClick, PlayerShopAdminGUIManager.PRICE_SLOT is " + priceSlot);

            if (event.getRawSlot() == displayNameSlot) {
                System.out.println("[TRACE] In ShopListener.onInventoryClick, clicked DISPLAY_NAME_SLOT. Player: " + player.getName());
                shopAdminGUIManager.initiateDisplayNameChange(player, shop);
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) initiated display name change for shop at %s via admin GUI.",
                        player.getName(), player.getUniqueId(), Shop.locationToString(shopLocation)));
            } else if (event.getRawSlot() == priceSlot) {
                System.out.println("[TRACE] In ShopListener.onInventoryClick, clicked PRICE_SLOT. Player: " + player.getName());
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

        if (viewTitle.equals(ShopVisitGUIManager.SHOP_VISIT_TITLE.toString())) { // .toString() for Component
            if (plugin.getPlayerViewingShopLocation().remove(playerId) != null) { // This map is still in SkyBlockProject, so plugin. access is correct
                 plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) closed Shop Visit GUI. Cleared viewing state.", player.getName(), playerId));
            }
        } else if (viewTitle.equals(PlayerShopAdminGUIManager.SHOP_ADMIN_TITLE.toString())) { // Updated to use renamed class and .toString() for Component
            System.out.println("[TRACE] In ShopListener.onInventoryClose, viewTitle matches PlayerShopAdminGUIManager.SHOP_ADMIN_TITLE. Player: " + player.getName());
            if (shopAdminGUIManager.getPlayerAdministeringShop().remove(playerId) != null) { // Use playerShopAdminGUIManager
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) closed PlayerShop Admin GUI. Cleared admin state.", player.getName(), playerId)); // Updated log
            }
            PlayerShopAdminGUIManager.AdminInputType expectedInput = shopAdminGUIManager.getPlayerWaitingForAdminInput().remove(playerId); // Use playerShopAdminGUIManager
            if (expectedInput != null) {
                player.sendMessage(ChatColor.YELLOW + "Shop setting input cancelled.");
                plugin.getLogger().info(String.format("ShopListener: Player %s (UUID: %s) cancelled shop admin input for %s by closing GUI.",
                        player.getName(), playerId, expectedInput.name()));
            }
        }
    }
}