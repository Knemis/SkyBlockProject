package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.PagedGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamInvite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InvitesGUI<T extends Team, U extends keviinUser<T>> extends PagedGUI<TeamInvite> {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;

    public InvitesGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(
                1,
                keviinTeams.getInventories().invitesGUI.size,
                keviinTeams.getInventories().invitesGUI.background,
                keviinTeams.getInventories().previousPage,
                keviinTeams.getInventories().nextPage,
                player,
                keviinTeams.getInventories().backButton
        );
        this.team = team;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().invitesGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<TeamInvite> getPageObjects() {
        return keviinTeams.getTeamManager().getTeamInvites(team);
    }

    @Override
    public ItemStack getItemStack(TeamInvite teamInvite) {
        Optional<U> user = keviinTeams.getUserManager().getUserByUUID(teamInvite.getUser());
        List<Placeholder> placeholderList = new ArrayList<>(keviinTeams.getUserPlaceholderBuilder().getPlaceholders(user));
        placeholderList.add(new Placeholder("invite_time", teamInvite.getTime().format(DateTimeFormatter.ofPattern(keviinTeams.getConfiguration().dateTimeFormat))));
        return ItemStackUtils.makeItem(keviinTeams.getInventories().invitesGUI.item, placeholderList);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        TeamInvite teamInvite = getItem(event.getSlot());
        if (teamInvite == null) return;

        String username = keviinTeams.getUserManager().getUserByUUID(teamInvite.getUser()).map(U::getName).orElse(keviinTeams.getMessages().nullPlaceholder);
        keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().unInviteCommand, new String[]{username});
    }
}
