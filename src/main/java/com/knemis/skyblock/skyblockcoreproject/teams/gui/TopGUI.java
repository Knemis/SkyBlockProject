package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.sorting.TeamSorting;
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
public class TopGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private TeamSorting<T> sortingType;
    private int page = 1;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final keviinTeams<T, U> keviinTeams;

    public TopGUI(TeamSorting<T> sortingType, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().topGUI.background, player, keviinTeams.getInventories().backButton);
        this.sortingType = sortingType;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().topGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        List<T> teams = keviinTeams.getTeamManager().getTeams(sortingType, true);

        for (int rank : keviinTeams.getConfiguration().teamTopSlots.keySet()) {
            int slot = keviinTeams.getConfiguration().teamTopSlots.get(rank);
            int actualRank = rank + (keviinTeams.getConfiguration().teamTopSlots.size() * (page - 1));
            if (teams.size() >= actualRank) {
                T team = teams.get(actualRank - 1);
                inventory.setItem(slot, ItemStackUtils.makeItem(keviinTeams.getInventories().topGUI.item, keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(team)));
            } else {
                inventory.setItem(slot, ItemStackUtils.makeItem(keviinTeams.getInventories().topGUI.filler));
            }
        }

        for (TeamSorting<T> sortingType : keviinTeams.getSortingTypes()) {
            inventory.setItem(sortingType.getItem().slot, ItemStackUtils.makeItem(sortingType.getItem()));
        }

        inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(keviinTeams.getInventories().nextPage));
        inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(keviinTeams.getInventories().previousPage));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (event.getSlot() == keviinTeams.getInventories().topGUI.size - 7 && page > 1) {
            page--;
            event.getWhoClicked().openInventory(getInventory());
            return;
        }

        if (event.getSlot() == keviinTeams.getInventories().topGUI.size - 3 && keviinTeams.getTeamManager().getTeams().size() >= 1 + (keviinTeams.getConfiguration().teamTopSlots.size() * page)) {
            page++;
            event.getWhoClicked().openInventory(getInventory());
        }

        keviinTeams.getSortingTypes().stream().filter(sorting -> sorting.item.slot == event.getSlot()).findFirst().ifPresent(sortingType -> {
            this.sortingType = sortingType;
            addContent(event.getInventory());
        });
    }
}
