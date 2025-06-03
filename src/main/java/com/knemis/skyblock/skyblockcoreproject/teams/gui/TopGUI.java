package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
// TODO: Address keviincore imports later, possibly replace with com.knemis.skyblock.skyblockcoreproject.utils.*
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting; // TODO: Update to actual TeamSorting class
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
// TODO: Update Team, User and TeamSorting to actual classes, resolve BackGUI
public class TopGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> /* extends com.keviin.keviincore.gui.BackGUI */ {

    private com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType;
    private int page = 1;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final SkyBlockTeams<T, U> skyblockTeams;
    private Player player; // Added player field

    public TopGUI(com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().topGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.sortingType = sortingType;
        this.skyblockTeams = skyblockTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = skyblockTeams.getInventories().topGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Top GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // List<T> teams = skyblockTeams.getTeamManager().getTeams(sortingType, true); // TODO: Uncomment when TeamManager and sortingType are refactored

        // for (int rank : skyblockTeams.getConfiguration().teamTopSlots.keySet()) { // TODO: Uncomment when Configuration is refactored
            // int slot = skyblockTeams.getConfiguration().teamTopSlots.get(rank);
            // int actualRank = rank + (skyblockTeams.getConfiguration().teamTopSlots.size() * (page - 1));
            // if (teams.size() >= actualRank) { // TODO: Uncomment when teams is available
                // T team = teams.get(actualRank - 1);
                // inventory.setItem(slot, ItemStackUtils.makeItem(skyblockTeams.getInventories().topGUI.item, skyblockTeams.getTeamsPlaceholderBuilder().getPlaceholders(team))); // TODO: Replace ItemStackUtils.makeItem, uncomment when getTeamsPlaceholderBuilder is available
            // } else {
                // inventory.setItem(slot, ItemStackUtils.makeItem(skyblockTeams.getInventories().topGUI.filler)); // TODO: Replace ItemStackUtils.makeItem
            // }
        // }

        // for (com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting<T> sortingType : skyblockTeams.getSortingTypes()) { // TODO: Update TeamSorting, uncomment when getSortingTypes is available
            // inventory.setItem(sortingType.getItem().slot, ItemStackUtils.makeItem(sortingType.getItem())); // TODO: Replace ItemStackUtils.makeItem
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(skyblockTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(skyblockTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // if (event.getSlot() == skyblockTeams.getInventories().topGUI.size - 7 && page > 1) {
            // page--;
            // event.getWhoClicked().openInventory(getInventory());
            // return;
        // }

        // if (event.getSlot() == skyblockTeams.getInventories().topGUI.size - 3 && skyblockTeams.getTeamManager().getTeams().size() >= 1 + (skyblockTeams.getConfiguration().teamTopSlots.size() * page)) { // TODO: Uncomment when TeamManager and Configuration are refactored
            // page++;
            // event.getWhoClicked().openInventory(getInventory());
        // }

        // skyblockTeams.getSortingTypes().stream().filter(sorting -> sorting.item.slot == event.getSlot()).findFirst().ifPresent(sortingType -> { // TODO: Uncomment when getSortingTypes is available
            // this.sortingType = sortingType;
            // addContent(event.getInventory());
        // });
    }
}
