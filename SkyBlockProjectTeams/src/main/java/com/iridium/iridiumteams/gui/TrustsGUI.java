package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.PagedGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamTrust;
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

public class TrustsGUI<T extends Team, U extends keviinUser<T>> extends PagedGUI<TeamTrust> {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;

    public TrustsGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(
                1,
                keviinTeams.getInventories().trustsGUI.size,
                keviinTeams.getInventories().trustsGUI.background,
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
        NoItemGUI noItemGUI = keviinTeams.getInventories().trustsGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<TeamTrust> getPageObjects() {
        return keviinTeams.getTeamManager().getTeamTrusts(team);
    }

    @Override
    public ItemStack getItemStack(TeamTrust teamTrust) {
        Optional<U> user = keviinTeams.getUserManager().getUserByUUID(teamTrust.getUser());
        Optional<U> truster = keviinTeams.getUserManager().getUserByUUID(teamTrust.getTruster());
        List<Placeholder> placeholderList = new ArrayList<>(keviinTeams.getUserPlaceholderBuilder().getPlaceholders(user));
        placeholderList.add(new Placeholder("trusted_time", teamTrust.getTime().format(DateTimeFormatter.ofPattern(keviinTeams.getConfiguration().dateTimeFormat))));
        placeholderList.add(new Placeholder("truster", truster.map(U::getName).orElse(keviinTeams.getMessages().nullPlaceholder)));
        return ItemStackUtils.makeItem(keviinTeams.getInventories().trustsGUI.item, placeholderList);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        TeamTrust teamTrust = getItem(event.getSlot());
        if (teamTrust == null) return;

        String username = keviinTeams.getUserManager().getUserByUUID(teamTrust.getUser()).map(U::getName).orElse(keviinTeams.getMessages().nullPlaceholder);
        keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().unTrustCommand, new String[]{username});
    }
}
