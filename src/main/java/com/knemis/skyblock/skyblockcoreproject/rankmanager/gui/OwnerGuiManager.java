package com.knemis.skyblock.skyblockcoreproject.rankmanager.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class OwnerGuiManager {
    private final SkyBlockProject plugin;
    public static final String GUI_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Critical Permission Update";
    private final Map<UUID, Inventory> openGuis = new HashMap<>();

    public OwnerGuiManager(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    public void openReloadGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack infoSign = new ItemStack(Material.OAK_SIGN);
        ItemMeta infoMeta = infoSign.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Permissions Repaired!");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Some ranks were fixed.",
                ChatColor.GOLD + "Please reload LuckPerms!"
        ));
        infoSign.setItemMeta(infoMeta);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM RELOAD");
        confirmButton.setItemMeta(confirmMeta);

        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CANCEL RELOAD");
        cancelButton.setItemMeta(cancelMeta);

        gui.setItem(4, infoSign);
        gui.setItem(11, confirmButton);
        gui.setItem(15, cancelButton);

        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), gui);
        plugin.getLogger().info("Showed reload GUI to " + player.getName());
    }

    public void handleGuiClick(Player player, ItemStack clickedItem, Inventory clickedInventory) { // Added clickedInventory
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Ensure this is our GUI before processing the click based on title.
        // The PlayerInteractionListener's onInventoryClick should primarily use isOurGui(clickedInventory)
        // before even calling this method. This is an additional safeguard.
        if (!clickedInventory.getViewers().get(0).getOpenInventory().getTitle().equals(GUI_TITLE)) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;
        String buttonName = ChatColor.stripColor(meta.getDisplayName());

        if (buttonName.contains("CONFIRM RELOAD")) {
            plugin.getLogger().info(player.getName() + " clicked CONFIRM in GUI.");
            plugin.confirmReload("GUI (" + player.getName() + ")");
            // Player will be released from lockdown and GUI closed by confirmReload -> performLuckPermsReload -> releaseAllOwnersFromLockdown
        } else if (buttonName.contains("CANCEL RELOAD")) {
            plugin.getLogger().info(player.getName() + " clicked CANCEL in GUI.");
            plugin.cancelReload("GUI (" + player.getName() + ")");
            // Player will be released from lockdown and GUI closed by cancelReload -> releaseAllOwnersFromLockdown
        }
        // No need to manually close GUI here, the calling methods (confirmReload/cancelReload) will handle it
        // by calling releaseAllOwnersFromLockdown which closes GUIs.
    }

    public void closeGuiForPlayer(Player player) {
        if (openGuis.containsKey(player.getUniqueId())) {
            // The listener should handle actual closing to prevent loops or issues with lockdown state.
            // This method is more for external calls if needed, but primarily listener driven.
            player.closeInventory();
            openGuis.remove(player.getUniqueId()); // Still remove from tracking if closed externally.
        }
    }

    public void closeAllGuis() {
        for (UUID playerId : new HashSet<>(openGuis.keySet())) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null && p.getOpenInventory().getTitle().equals(GUI_TITLE)) { // Check if it's our GUI
               p.closeInventory();
            }
        }
        openGuis.clear();
    }

    public boolean isOurGui(Inventory inventory) {
        if (inventory == null) return false;
        // Check if the inventory is one of the GUIs created by this manager
        // This can be done by checking if the inventory instance is among the values of openGuis
        // OR by title if we are sure no other plugin uses this exact title.
        // For Bukkit.createInventory(null, ...), title is a common way.
        // A more robust way is to implement InventoryHolder.
        try {
            // Check if the inventory has a viewer and if the title of the open inventory matches.
            // This is important because event.getInventory() in InventoryCloseEvent is the inventory being closed,
            // and event.getView().getTopInventory() in InventoryClickEvent is the top inventory.
            return inventory.getViewers().size() > 0 &&
                   inventory.getViewers().get(0).getOpenInventory().getTitle().equals(GUI_TITLE);
        } catch (Exception e) {
            // IndexOutOfBoundsException if no viewers, etc.
            return false;
        }
    }
}
