package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.PagedGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MembersGUI<T extends Team, U extends keviinUser<T>> extends PagedGUI<U> {

    private final keviinTeams<T, U> keviinTeams;
    private final T team;

    public MembersGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(
                1,
                keviinTeams.getInventories().membersGUI.size,
                keviinTeams.getInventories().membersGUI.background,
                keviinTeams.getInventories().previousPage,
                keviinTeams.getInventories().nextPage,
                player,
                keviinTeams.getInventories().backButton
        );
        this.keviinTeams = keviinTeams;
        this.team = team;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().membersGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<U> getPageObjects() {
        return keviinTeams.getTeamManager().getTeamMembers(team);
    }

    @Override
    public ItemStack getItemStack(U user) {
        return ItemStackUtils.makeItem(keviinTeams.getInventories().membersGUI.item, keviinTeams.getUserPlaceholderBuilder().getPlaceholders(user));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        U user = getItem(event.getSlot());
        if (user == null) return;

        switch (event.getClick()) {
            case LEFT:
                if (user.getUserRank() != 1) {
                    keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().demoteCommand, new String[]{user.getName()});
                } else {
                    keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().kickCommand, new String[]{user.getName()});
                }
                break;
            case RIGHT:
                keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().promoteCommand, new String[]{user.getName()});
                break;
        }
    }
}
