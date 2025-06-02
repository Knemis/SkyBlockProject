package com.knemis.skyblock.skyblockcoreproject.teams.gui;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.sorting.TeamSorting;
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
public class TopGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private TeamSorting<T> sortingType;
    private int page = 1;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public TopGUI(TeamSorting<T> sortingType, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().topGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.sortingType = sortingType;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().topGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        List<T> teams = SkyBlockProjectTeams.getTeamManager().getTeams(sortingType, true);

        for (int rank : SkyBlockProjectTeams.getConfiguration().teamTopSlots.keySet()) {
            int slot = SkyBlockProjectTeams.getConfiguration().teamTopSlots.get(rank);
            int actualRank = rank + (SkyBlockProjectTeams.getConfiguration().teamTopSlots.size() * (page - 1));
            if (teams.size() >= actualRank) {
                T team = teams.get(actualRank - 1);
                inventory.setItem(slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().topGUI.item, SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team)));
            } else {
                inventory.setItem(slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().topGUI.filler));
            }
        }

        for (TeamSorting<T> sortingType : SkyBlockProjectTeams.getSortingTypes()) {
            inventory.setItem(sortingType.getItem().slot, ItemStackUtils.makeItem(sortingType.getItem()));
        }

        inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().nextPage));
        inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().previousPage));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (event.getSlot() == SkyBlockProjectTeams.getInventories().topGUI.size - 7 && page > 1) {
            page--;
            event.getWhoClicked().openInventory(getInventory());
            return;
        }

        if (event.getSlot() == SkyBlockProjectTeams.getInventories().topGUI.size - 3 && SkyBlockProjectTeams.getTeamManager().getTeams().size() >= 1 + (SkyBlockProjectTeams.getConfiguration().teamTopSlots.size() * page)) {
            page++;
            event.getWhoClicked().openInventory(getInventory());
        }

        SkyBlockProjectTeams.getSortingTypes().stream().filter(sorting -> sorting.item.slot == event.getSlot()).findFirst().ifPresent(sortingType -> {
            this.sortingType = sortingType;
            addContent(event.getInventory());
        });
    }
}
