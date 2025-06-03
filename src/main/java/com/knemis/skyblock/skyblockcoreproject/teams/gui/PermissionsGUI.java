package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
// TODO: Address keviincore imports later, possibly replace with com.knemis.skyblock.skyblockcoreproject.utils.*
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Permission;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

// TODO: Update Team and User to actual classes, resolve BackGUI
public class PermissionsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> /* extends com.keviin.keviincore.gui.BackGUI */ {

    private final SkyBlockTeams<T, U> skyblockTeams;
    private final T team;
    @Getter
    private final int rank;
    @Getter
    private int page;
    private Player player; // Added player field

    public PermissionsGUI(T team, int rank, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().permissionsGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.skyblockTeams = skyblockTeams;
        this.team = team;
        this.rank = rank;
        this.page = 1;
    }

    public PermissionsGUI(T team, int rank, int page, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().permissionsGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.skyblockTeams = skyblockTeams;
        this.team = team;
        this.rank = rank;
        this.page = page;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, skyblockTeams.getInventories().permissionsGUI.size, StringUtils.color(skyblockTeams.getInventories().permissionsGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, skyblockTeams.getInventories().permissionsGUI.size, "Permissions GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Permission> permission : skyblockTeams.getPermissionList().entrySet()) { // TODO: Uncomment when getPermissionList is available
            // if (permission.getValue().getPage() != page) continue;
            // boolean allowed = skyblockTeams.getTeamManager().getTeamPermission(team, rank, permission.getKey()); // TODO: Uncomment when TeamManager is refactored
            // inventory.setItem(permission.getValue().getItem().slot, ItemStackUtils.makeItem(permission.getValue().getItem(), Collections.singletonList(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("permission", allowed ? skyblockTeams.getPermissions().allowed : skyblockTeams.getPermissions().denied)))); // TODO: Replace ItemStackUtils.makeItem and Placeholder, uncomment when getPermissions is available
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(skyblockTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(skyblockTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // if (event.getSlot() == skyblockTeams.getInventories().permissionsGUI.size - 7 && page > 1) {
            // page--;
            // event.getWhoClicked().openInventory(getInventory());
            // return;
        // }

        // if (event.getSlot() == skyblockTeams.getInventories().permissionsGUI.size - 3 && skyblockTeams.getPermissionList().values().stream().anyMatch(permission -> permission.getPage() == page + 1)) { // TODO: Uncomment when getPermissionList is available
            // page++;
            // event.getWhoClicked().openInventory(getInventory());
        // }

        // for (Map.Entry<String, Permission> permission : skyblockTeams.getPermissionList().entrySet()) { // TODO: Uncomment when getPermissionList is available
            // if (permission.getValue().getItem().slot != event.getSlot()) continue;
            // if (permission.getValue().getPage() != page) continue;

            // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().setPermissionCommand, new String[]{permission.getKey(), skyblockTeams.getUserRanks().get(rank).name}); // TODO: Uncomment when CommandManager, Commands and getUserRanks are available
            // return;
        // }
    }
}
