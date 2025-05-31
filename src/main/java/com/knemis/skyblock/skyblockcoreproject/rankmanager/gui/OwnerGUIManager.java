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

// Corrected class name to match filename OwnerGUIManager.java
public class OwnerGUIManager {
    private final SkyBlockProject plugin;
    public static final String GUI_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Critical Permission Update";
    private final Map<UUID, Inventory> openGuis = new HashMap<>();

    public OwnerGUIManager(SkyBlockProject plugin) {
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

    // Signature matches the one in PlayerInteractionListener
    public void handleGuiClick(Player player, ItemStack clickedItem, Inventory clickedInventory) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // This check might be redundant if PlayerInteractionListener already does it,
        // but it's a good safeguard.
        // It should check the title of the inventory the item was clicked IN.
        if (!clickedInventory.getViewers().get(0).getOpenInventory().getTitle().equals(GUI_TITLE)) {
             return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return; // Ensure meta and display name exist
        String buttonName = ChatColor.stripColor(meta.getDisplayName());

        if (buttonName.contains("CONFIRM RELOAD")) {
            plugin.getLogger().info(player.getName() + " clicked CONFIRM in GUI.");
            plugin.confirmReload("GUI (" + player.getName() + ")");
        } else if (buttonName.contains("CANCEL RELOAD")) {
            plugin.getLogger().info(player.getName() + " clicked CANCEL in GUI.");
            plugin.cancelReload("GUI (" + player.getName() + ")");
        }
    }

    public void closeGuiForPlayer(Player player) {
        if (openGuis.containsKey(player.getUniqueId())) {
            player.closeInventory();
            openGuis.remove(player.getUniqueId());
        }
    }

    public void closeAllGuis() {
        for (UUID playerId : new HashSet<>(openGuis.keySet())) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null && p.getOpenInventory().getTitle().equals(GUI_TITLE)) {
               p.closeInventory();
            }
        }
        openGuis.clear();
    }

    public boolean isOurGui(Inventory inventory) {
        if (inventory == null) return false;
        try {
            // Check if the inventory has a viewer and if the title of the open inventory matches.
            return inventory.getViewers().size() > 0 &&
                   inventory.getViewers().get(0).getOpenInventory().getTitle().equals(GUI_TITLE);
        } catch (Exception e) {
            return false;
        }
    }
}
