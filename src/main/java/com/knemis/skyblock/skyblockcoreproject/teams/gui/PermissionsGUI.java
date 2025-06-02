package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.Permission;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class PermissionsGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;
    @Getter
    private final int rank;
    @Getter
    private int page;

    public PermissionsGUI(T team, int rank, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().permissionsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
        this.rank = rank;
        this.page = 1;
    }

    public PermissionsGUI(T team, int rank, int page, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().permissionsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);

        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
        this.rank = rank;
        this.page = page;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().permissionsGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().permissionsGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, Permission> permission : SkyBlockProjectTeams.getPermissionList().entrySet()) {
            if (permission.getValue().getPage() != page) continue;
            boolean allowed = SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, rank, permission.getKey());
            inventory.setItem(permission.getValue().getItem().slot, ItemStackUtils.makeItem(permission.getValue().getItem(), Collections.singletonList(new Placeholder("permission", allowed ? SkyBlockProjectTeams.getPermissions().allowed : SkyBlockProjectTeams.getPermissions().denied))));
        }

        inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().nextPage));
        inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().previousPage));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (event.getSlot() == SkyBlockProjectTeams.getInventories().permissionsGUI.size - 7 && page > 1) {
            page--;
            event.getWhoClicked().openInventory(getInventory());
            return;
        }

        if (event.getSlot() == SkyBlockProjectTeams.getInventories().permissionsGUI.size - 3 && SkyBlockProjectTeams.getPermissionList().values().stream().anyMatch(permission -> permission.getPage() == page + 1)) {
            page++;
            event.getWhoClicked().openInventory(getInventory());
        }

        for (Map.Entry<String, Permission> permission : SkyBlockProjectTeams.getPermissionList().entrySet()) {
            if (permission.getValue().getItem().slot != event.getSlot()) continue;
            if (permission.getValue().getPage() != page) continue;

            SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().setPermissionCommand, new String[]{permission.getKey(), SkyBlockProjectTeams.getUserRanks().get(rank).name});
            return;
        }
    }
}
