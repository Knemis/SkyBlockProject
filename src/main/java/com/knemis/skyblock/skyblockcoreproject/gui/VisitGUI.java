package com.knemis.skyblock.skyblockcoreproject.gui;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.base.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.placeholders.IslandPlaceholderBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class VisitGUI extends PagedGUI<Island> {
    private final SkyBlockProject plugin;
    private final IslandPlaceholderBuilder islandPlaceholderBuilder;

    public VisitGUI(Player player, SkyBlockProject plugin) {
        super(
                1, // initialPage
                plugin.getConfig().getInt("gui.visit.size", 54),
                InventoryUtils.createSolidBackgroundItem(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()),
                InventoryUtils.createGuiItem(plugin, "gui.visit.previous-page-item", Material.ARROW, "&cPrevious Page"),
                InventoryUtils.createGuiItem(plugin, "gui.visit.next-page-item", Material.ARROW, "&aNext Page"),
                player,
                InventoryUtils.createGuiItem(plugin, "gui.visit.back-button", Material.BARRIER, "&cBack")
        );
        this.plugin = plugin;
        this.islandPlaceholderBuilder = new IslandPlaceholderBuilder(plugin);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        String title = ChatUtils.colorize(plugin.getConfig().getString("gui.visit.title", "&4&lVisit Islands")); // Changed default title
        Inventory inventory = Bukkit.createInventory(this, getSize(), title);

        // Add navigation and background first
        super.addContent(inventory);

        // Populate with items from current page
        // Sort islands by some criteria, e.g., level or name, for consistent display
        List<Island> islandList = getPageObjects().stream()
            .sorted(Comparator.comparing(Island::getIslandLevel).reversed().thenComparing(Island::getIslandName))
            .collect(Collectors.toList());

        int itemsPerPage = getSize() - 9; // Slots available for items, assuming 9 for nav/border
        if (itemsPerPage <= 0) itemsPerPage = 1; // Ensure positive

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, islandList.size());

        super.items.clear();
        for (int i = 0; i < itemsPerPage && (startIndex + i) < islandList.size(); i++) {
            // Ensure we don't try to set items into navigation slots if GUI is too small
            if (i >= getSize() - 9 && getSize() % 9 != 0) continue; // Simple check, might need refinement based on layout
            if (getSlotMappings().get(i) == -1) continue; // Skip if slot is a nav/border slot

            Island island = islandList.get(startIndex + i);
            ItemStack itemStack = getItemStack(island);
            inventory.setItem(getSlotMappings().get(i), itemStack); // Use mapped slot
            super.items.put(getSlotMappings().get(i), island);
        }

        return inventory;
    }

    // Provides mapping from sequential item index (0 to itemsPerPage-1) to actual inventory slot
    private List<Integer> getSlotMappings() {
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            if (i != getBackButtonSlot() && i != getNextButtonSlot() && i != getPreviousButtonSlot()) {
                 // Add more reserved slots here if any (e.g. border items)
                availableSlots.add(i);
            }
        }
        // This simple version assumes items are just filled into non-nav slots.
        // A more complex GUI might have specific "content" areas.
        return availableSlots;
    }


    @Override
    public Collection<Island> getPageObjects() {
        // Filter for public islands or apply other visit criteria if necessary
        return plugin.getIslandDataHandler().getAllIslandsDataView().values().stream()
            // .filter(island -> island.isPublic()) // TODO: Add isPublic or similar to Island class
            .collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack(Island island) {
        String materialName = plugin.getConfig().getString("gui.visit.item.material", "PLAYER_HEAD");
        Optional<XMaterial> xMat = XMaterial.matchXMaterial(materialName);
        Material itemMaterial = xMat.isPresent() ? xMat.get().parseMaterial() : Material.GRASS_BLOCK;
        if(itemMaterial == null) itemMaterial = Material.GRASS_BLOCK;


        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<Placeholder> placeholders = islandPlaceholderBuilder.getPlaceholders(island);

            String displayNameFormat = plugin.getConfig().getString("gui.visit.item.name", "&9&l%island_name%");
            meta.setDisplayName(Placeholder.process(displayNameFormat, placeholders));

            List<String> loreFormat = plugin.getConfig().getStringList("gui.visit.item.lore");
            if (loreFormat != null && !loreFormat.isEmpty()) {
                 meta.setLore(Placeholder.process(loreFormat, placeholders));
            } else {
                meta.setLore(Collections.emptyList());
            }

            if (itemMaterial == Material.PLAYER_HEAD && meta instanceof SkullMeta && island.getOwnerUUID() != null) {
               OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerUUID());
               if(owner != null) {
                 ((SkullMeta) meta).setOwningPlayer(owner);
               }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        // Check if superclass (PagedGUI) already handled the click (e.g. nav button, which closes GUI or reopens)
        // If player.getOpenInventory().getTopInventory() is not this GUI instance, it means super changed it.
        // However, PagedGUI reopens the same inventory for page changes.
        // A simple check is if the event was cancelled by super and it was a nav button.
        // For now, if it was a nav button, PagedGUI handles it and we don't proceed.
        int clickedSlot = event.getSlot();
        if (clickedSlot == getBackButtonSlot() || clickedSlot == getNextButtonSlot() || clickedSlot == getPreviousButtonSlot()){
            return;
        }
        // If event was cancelled by super for other reasons (e.g. background click), but was not a nav button,
        // then we still might not want to proceed.
        // The `event.isCancelled()` check after super call is the most reliable.
        if (event.isCancelled() && getItem(clickedSlot) == null) { // If cancelled and not an actual item slot we mapped
             return;
        }
        event.setCancelled(true); // Cancel again if this class handles it, or ensure it's cancelled.


        Island island = getItem(clickedSlot); // Uses slot from event, which is correct
        if (island == null || island.getIslandName() == null) return;

        String visitCommandFormat = plugin.getConfig().getString("commands.island-visit.format", "island visit %island_name%");
        String commandToDispatch = visitCommandFormat.replace("%island_name%", island.getIslandName());

        if(event.getWhoClicked() instanceof Player){
             Player clicker = (Player) event.getWhoClicked();
             if (clicker.isOnline()){
                // Bukkit.dispatchCommand is preferred for player-like commands for permission checks,
                // but for simple teleports, player.performCommand can also work.
                // Let's use dispatch if it's a global command, or player.performCommand if it's meant to be "as if player typed it"
                clicker.performCommand(commandToDispatch);
             }
        }
        // No need to close inventory here, as performCommand often triggers other actions or teleports
        // which will close the GUI. If the command is very light and doesn't close it, then:
        // event.getWhoClicked().closeInventory();
    }
}
