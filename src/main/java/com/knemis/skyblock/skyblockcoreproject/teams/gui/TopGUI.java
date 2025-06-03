package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

@Getter
@Setter
public class TopGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType; // TODO: Update TeamSorting to actual class
    private int page = 1;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private Player player; // Added player field

    public TopGUI(com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) { // TODO: Update TeamSorting
        // super(SkyBlockProjectTeams.getInventories().topGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.sortingType = sortingType;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().topGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Top GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // List<T> teams = SkyBlockProjectTeams.getTeamManager().getTeams(sortingType, true); // TODO: Uncomment when TeamManager and sortingType are refactored

        // for (int rank : SkyBlockProjectTeams.getConfiguration().teamTopSlots.keySet()) { // TODO: Uncomment when Configuration is refactored
            // int slot = SkyBlockProjectTeams.getConfiguration().teamTopSlots.get(rank);
            // int actualRank = rank + (SkyBlockProjectTeams.getConfiguration().teamTopSlots.size() * (page - 1));
            // if (teams.size() >= actualRank) { // TODO: Uncomment when teams is available
                // T team = teams.get(actualRank - 1);
                // inventory.setItem(slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().topGUI.item, SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team))); // TODO: Replace ItemStackUtils.makeItem, uncomment when getTeamsPlaceholderBuilder is available
            // } else {
                // inventory.setItem(slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().topGUI.filler)); // TODO: Replace ItemStackUtils.makeItem
            // }
        // }

        // for (com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType : SkyBlockProjectTeams.getSortingTypes()) { // TODO: Update TeamSorting, uncomment when getSortingTypes is available
            // inventory.setItem(sortingType.getItem().slot, ItemStackUtils.makeItem(sortingType.getItem())); // TODO: Replace ItemStackUtils.makeItem
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // if (event.getSlot() == SkyBlockProjectTeams.getInventories().topGUI.size - 7 && page > 1) {
            // page--;
            // event.getWhoClicked().openInventory(getInventory());
            // return;
        // }

        // if (event.getSlot() == SkyBlockProjectTeams.getInventories().topGUI.size - 3 && SkyBlockProjectTeams.getTeamManager().getTeams().size() >= 1 + (SkyBlockProjectTeams.getConfiguration().teamTopSlots.size() * page)) { // TODO: Uncomment when TeamManager and Configuration are refactored
            // page++;
            // event.getWhoClicked().openInventory(getInventory());
        // }

        // SkyBlockProjectTeams.getSortingTypes().stream().filter(sorting -> sorting.item.slot == event.getSlot()).findFirst().ifPresent(sortingType -> { // TODO: Uncomment when getSortingTypes is available
            // this.sortingType = sortingType;
            // addContent(event.getInventory());
        // });
    }
}
