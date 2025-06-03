package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class PermissionsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;
    @Getter
    private final int rank;
    @Getter
    private int page;
    private Player player; // Added player field

    public PermissionsGUI(T team, int rank, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().permissionsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
        this.rank = rank;
        this.page = 1;
    }

    public PermissionsGUI(T team, int rank, int page, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().permissionsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
        this.rank = rank;
        this.page = page;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().permissionsGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().permissionsGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, SkyBlockProjectTeams.getInventories().permissionsGUI.size, "Permissions GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Permission> permission : SkyBlockProjectTeams.getPermissionList().entrySet()) { // TODO: Uncomment when getPermissionList is available
            // if (permission.getValue().getPage() != page) continue;
            // boolean allowed = SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, rank, permission.getKey()); // TODO: Uncomment when TeamManager is refactored
            // inventory.setItem(permission.getValue().getItem().slot, ItemStackUtils.makeItem(permission.getValue().getItem(), Collections.singletonList(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("permission", allowed ? SkyBlockProjectTeams.getPermissions().allowed : SkyBlockProjectTeams.getPermissions().denied)))); // TODO: Replace ItemStackUtils.makeItem and Placeholder, uncomment when getPermissions is available
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // if (event.getSlot() == SkyBlockProjectTeams.getInventories().permissionsGUI.size - 7 && page > 1) {
            // page--;
            // event.getWhoClicked().openInventory(getInventory());
            // return;
        // }

        // if (event.getSlot() == SkyBlockProjectTeams.getInventories().permissionsGUI.size - 3 && SkyBlockProjectTeams.getPermissionList().values().stream().anyMatch(permission -> permission.getPage() == page + 1)) { // TODO: Uncomment when getPermissionList is available
            // page++;
            // event.getWhoClicked().openInventory(getInventory());
        // }

        // for (Map.Entry<String, Permission> permission : SkyBlockProjectTeams.getPermissionList().entrySet()) { // TODO: Uncomment when getPermissionList is available
            // if (permission.getValue().getItem().slot != event.getSlot()) continue;
            // if (permission.getValue().getPage() != page) continue;

            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().setPermissionCommand, new String[]{permission.getKey(), SkyBlockProjectTeams.getUserRanks().get(rank).name}); // TODO: Uncomment when CommandManager, Commands and getUserRanks are available
            // return;
        // }
    }
}
