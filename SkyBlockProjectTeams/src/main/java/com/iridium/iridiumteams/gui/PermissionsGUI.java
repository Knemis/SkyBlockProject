package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Permission;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class PermissionsGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final keviinTeams<T, U> keviinTeams;
    private final T team;
    @Getter
    private final int rank;
    @Getter
    private int page;

    public PermissionsGUI(T team, int rank, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().permissionsGUI.background, player, keviinTeams.getInventories().backButton);
        this.keviinTeams = keviinTeams;
        this.team = team;
        this.rank = rank;
        this.page = 1;
    }

    public PermissionsGUI(T team, int rank, int page, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().permissionsGUI.background, player, keviinTeams.getInventories().backButton);

        this.keviinTeams = keviinTeams;
        this.team = team;
        this.rank = rank;
        this.page = page;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, keviinTeams.getInventories().permissionsGUI.size, StringUtils.color(keviinTeams.getInventories().permissionsGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, Permission> permission : keviinTeams.getPermissionList().entrySet()) {
            if (permission.getValue().getPage() != page) continue;
            boolean allowed = keviinTeams.getTeamManager().getTeamPermission(team, rank, permission.getKey());
            inventory.setItem(permission.getValue().getItem().slot, ItemStackUtils.makeItem(permission.getValue().getItem(), Collections.singletonList(new Placeholder("permission", allowed ? keviinTeams.getPermissions().allowed : keviinTeams.getPermissions().denied))));
        }

        inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(keviinTeams.getInventories().nextPage));
        inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(keviinTeams.getInventories().previousPage));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (event.getSlot() == keviinTeams.getInventories().permissionsGUI.size - 7 && page > 1) {
            page--;
            event.getWhoClicked().openInventory(getInventory());
            return;
        }

        if (event.getSlot() == keviinTeams.getInventories().permissionsGUI.size - 3 && keviinTeams.getPermissionList().values().stream().anyMatch(permission -> permission.getPage() == page + 1)) {
            page++;
            event.getWhoClicked().openInventory(getInventory());
        }

        for (Map.Entry<String, Permission> permission : keviinTeams.getPermissionList().entrySet()) {
            if (permission.getValue().getItem().slot != event.getSlot()) continue;
            if (permission.getValue().getPage() != page) continue;

            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().setPermissionCommand, new String[]{permission.getKey(), keviinTeams.getUserRanks().get(rank).name});
            return;
        }
    }
}
