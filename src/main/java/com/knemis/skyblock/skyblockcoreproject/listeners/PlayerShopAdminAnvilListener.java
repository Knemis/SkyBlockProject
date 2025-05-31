package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.PlayerShopAdminGUIManager;
// import com.knemis.skyblock.skyblockcoreproject.shop.Shop; // Not directly needed here, PlayerShopAdminGUIManager handles shop retrieval
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location; // For onInventoryClose logic if re-opening main admin menu
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
// import org.bukkit.event.inventory.InventoryAction; // Not strictly needed for this implementation
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent; // Added import
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack; // For checking output item, though not strictly necessary for this logic

import java.util.HashMap; // Added import
import java.util.Map; // Added import
import java.util.UUID;

public class PlayerShopAdminAnvilListener implements Listener {

    private final SkyBlockProject plugin;
    private final PlayerShopAdminGUIManager playerShopAdminGUIManager;
    private final Map<UUID, String> playerAnvilInputs = new HashMap<>(); // Added map

    public PlayerShopAdminAnvilListener(SkyBlockProject plugin, PlayerShopAdminGUIManager playerShopAdminGUIManager) {
        this.plugin = plugin;
        this.playerShopAdminGUIManager = playerShopAdminGUIManager;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) return;
        Player player = (Player) event.getView().getPlayer();
        AnvilInventory anvilInventory = event.getInventory();
        String renameText = anvilInventory.getRenameText(); // Reverted

        // Store the input, even if it's empty, to reflect user clearing text
        playerAnvilInputs.put(player.getUniqueId(), renameText != null ? renameText : "");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!(event.getInventory() instanceof AnvilInventory)) return;

        PlayerShopAdminGUIManager.AdminInputType inputType = playerShopAdminGUIManager.getPlayerWaitingForAdminInput().get(player.getUniqueId());
        if (inputType != PlayerShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME_ANVIL &&
            inputType != PlayerShopAdminGUIManager.AdminInputType.SHOP_PRICE_ANVIL) {
            return;
        }

        String currentAnvilTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
        String expectedNameTitle = LegacyComponentSerializer.legacySection().serialize(PlayerShopAdminGUIManager.ANVIL_DISPLAY_NAME_TITLE);
        String expectedPriceTitle = LegacyComponentSerializer.legacySection().serialize(PlayerShopAdminGUIManager.ANVIL_PRICE_TITLE);

        boolean titleMatches = (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME_ANVIL && currentAnvilTitle.equals(expectedNameTitle)) ||
                               (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_PRICE_ANVIL && currentAnvilTitle.equals(expectedPriceTitle));

        if (!titleMatches) {
            plugin.getLogger().warning("[PlayerShopAdminAnvilListener] Player " + player.getName() +
                                       " is in input state " + inputType +
                                       " but Anvil title '" + currentAnvilTitle + "' does not match expected. Ignoring click.");
            return;
        }

        // event.setCancelled(true); // Will be set true only for specific slots to allow Anvil mechanics

        AnvilInventory anvilInv = (AnvilInventory) event.getInventory();
        int rawSlot = event.getRawSlot();

        // Check if the click is within the Anvil inventory view (not player's inventory)
        if (event.getClickedInventory() != anvilInv) {
            // Allow clicks in player inventory
            return;
        }

        // Prevent taking items from slot 0 or 1, or placing items there if not allowed.
        // For this simple input, we primarily care about the result slot (2).
        // Bukkit's Anvil behavior can be tricky; often, cancelling events in slot 0 or 1
        // can interfere with the rename/result mechanism.
        // We will only cancel clicks in slot 0 and 1 to prevent item removal.
        // Let Bukkit handle the text input and result item generation.
        if (rawSlot == 0 || rawSlot == 1) {
            event.setCancelled(true); // Prevent taking the setup items
        } else if (rawSlot == 2) {
            // This is the result slot
            ItemStack resultItem = event.getCurrentItem(); // Item in the result slot
            if (resultItem == null || resultItem.getType().isAir()) {
                 player.sendMessage(Component.text("Please enter a value in the Anvil.", NamedTextColor.RED));
                 event.setCancelled(true); // Prevent taking an empty/invalid result
                 return;
            }

            // String inputText = anvilInv.getRenameText(); // OLD METHOD
            String inputText = playerAnvilInputs.get(player.getUniqueId());


            if (inputText == null || inputText.trim().isEmpty()) { // inputText can be null if PrepareAnvilEvent didn't fire or map was cleared
                    player.sendMessage(Component.text("Input cannot be empty.", NamedTextColor.RED));
                event.setCancelled(true); // Prevent taking an empty/invalid result
                return;
            }

            // Process the input. The GUIManager methods will handle closing inventory or re-opening anvil.
            // We don't cancel the event here if valid, to allow player to take the (cosmetic) result item,
            // though the actual processing is done with inputText.
            // The GUIManager methods also remove the player from waitingForAdminInput.
            // Player taking the item from slot 2 automatically closes the Anvil GUI.

            if (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME_ANVIL) {
                playerShopAdminGUIManager.processAnvilDisplayNameInput(player, inputText.trim());
            } else if (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_PRICE_ANVIL) {
                playerShopAdminGUIManager.processAnvilPriceInput(player, inputText.trim());
            }
            playerAnvilInputs.remove(player.getUniqueId()); // Clean up map after processing
             // Let the event proceed so player can take the item, which closes anvil.
             // State is cleared by process methods.
        }
        // If click is not in slot 0, 1, or 2, let it be (e.g. clicking empty space in Anvil inv)
        // but it's generally good to cancel if it's not an intended interaction.
        // However, for Anvil, being too restrictive can break its input mechanics.
        // The critical part is handling slot 2 correctly.
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        PlayerShopAdminGUIManager.AdminInputType inputType = playerShopAdminGUIManager.getPlayerWaitingForAdminInput().get(playerId);

        if (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME_ANVIL ||
            inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_PRICE_ANVIL) {

            String closedAnvilTitle = LegacyComponentSerializer.legacySection().serialize(event.getView().title());
            String expectedNameTitle = LegacyComponentSerializer.legacySection().serialize(PlayerShopAdminGUIManager.ANVIL_DISPLAY_NAME_TITLE);
            String expectedPriceTitle = LegacyComponentSerializer.legacySection().serialize(PlayerShopAdminGUIManager.ANVIL_PRICE_TITLE);

            boolean titleMatched = (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_DISPLAY_NAME_ANVIL && closedAnvilTitle.equals(expectedNameTitle)) ||
                                   (inputType == PlayerShopAdminGUIManager.AdminInputType.SHOP_PRICE_ANVIL && closedAnvilTitle.equals(expectedPriceTitle));

            if (titleMatched) {
                // If inputType is still present, it means slot 2 was not clicked and processed (which would have cleared it).
                // This indicates a premature close.
                if (playerShopAdminGUIManager.getPlayerWaitingForAdminInput().containsKey(playerId)) {
                    playerShopAdminGUIManager.getPlayerWaitingForAdminInput().remove(playerId);
                    playerAnvilInputs.remove(playerId); // Clean up map on close
                    // Also remove from administering shop to prevent issues if they re-open admin menu without finishing.
                    // Location shopLocation = playerShopAdminGUIManager.getPlayerAdministeringShop().remove(playerId);
                    // No, keep administering shop state, just cancel the specific input.

                    player.sendMessage(Component.text("Shop admin input cancelled.", NamedTextColor.YELLOW));
                    plugin.getLogger().info("[PlayerShopAdminAnvilListener] Player " + player.getName() +
                                           " prematurely closed Anvil GUI for " + inputType + ". Input state cleared.");

                    // Re-open the main admin menu if the player was administering a shop
                    // This provides a better UX than just closing to an empty screen.
                    // Location shopLocationForReopen = playerShopAdminGUIManager.getPlayerAdministeringShop().get(playerId);
                    // Shop shopToReopen = null;
                    // if (shopLocationForReopen != null) {
                    //    shopToReopen = plugin.getShopManager().getActiveShop(shopLocationForReopen);
                    // }
                    // if (shopToReopen != null) {
                    //    final Shop finalShopToReopen = shopToReopen; // For BukkitRunnable
                    //    new org.bukkit.scheduler.BukkitRunnable() {
                    //        @Override
                    //        public void run() {
                    //            playerShopAdminGUIManager.openAdminMenu(player, finalShopToReopen);
                    //        }
                    //    }.runTask(plugin); // Run on next tick to avoid issues with inventory events
                    //}
                }
            }
        }
    }
}
