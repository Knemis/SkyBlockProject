package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MembersGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends PagedGUI<U> {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;

    public MembersGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(
                1,
                SkyBlockProjectTeams.getInventories().membersGUI.size,
                SkyBlockProjectTeams.getInventories().membersGUI.background,
                SkyBlockProjectTeams.getInventories().previousPage,
                SkyBlockProjectTeams.getInventories().nextPage,
                player,
                SkyBlockProjectTeams.getInventories().backButton
        );
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().membersGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<U> getPageObjects() {
        return SkyBlockProjectTeams.getTeamManager().getTeamMembers(team);
    }

    @Override
    public ItemStack getItemStack(U user) {
        return ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().membersGUI.item, SkyBlockProjectTeams.getUserPlaceholderBuilder().getPlaceholders(user));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        U user = getItem(event.getSlot());
        if (user == null) return;

        switch (event.getClick()) {
            case LEFT:
                if (user.getUserRank() != 1) {
                    SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().demoteCommand, new String[]{user.getName()});
                } else {
                    SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().kickCommand, new String[]{user.getName()});
                }
                break;
            case RIGHT:
                SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().promoteCommand, new String[]{user.getName()});
                break;
        }
    }
}
