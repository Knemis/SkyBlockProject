package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

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

public class TrustsGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends PagedGUI<TeamTrust> {

    private final T team;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public TrustsGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(
                1,
                SkyBlockProjectTeams.getInventories().trustsGUI.size,
                SkyBlockProjectTeams.getInventories().trustsGUI.background,
                SkyBlockProjectTeams.getInventories().previousPage,
                SkyBlockProjectTeams.getInventories().nextPage,
                player,
                SkyBlockProjectTeams.getInventories().backButton
        );
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().trustsGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<TeamTrust> getPageObjects() {
        return SkyBlockProjectTeams.getTeamManager().getTeamTrusts(team);
    }

    @Override
    public ItemStack getItemStack(TeamTrust teamTrust) {
        Optional<U> user = SkyBlockProjectTeams.getUserManager().getUserByUUID(teamTrust.getUser());
        Optional<U> truster = SkyBlockProjectTeams.getUserManager().getUserByUUID(teamTrust.getTruster());
        List<Placeholder> placeholderList = new ArrayList<>(SkyBlockProjectTeams.getUserPlaceholderBuilder().getPlaceholders(user));
        placeholderList.add(new Placeholder("trusted_time", teamTrust.getTime().format(DateTimeFormatter.ofPattern(SkyBlockProjectTeams.getConfiguration().dateTimeFormat))));
        placeholderList.add(new Placeholder("truster", truster.map(U::getName).orElse(SkyBlockProjectTeams.getMessages().nullPlaceholder)));
        return ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().trustsGUI.item, placeholderList);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        TeamTrust teamTrust = getItem(event.getSlot());
        if (teamTrust == null) return;

        String username = SkyBlockProjectTeams.getUserManager().getUserByUUID(teamTrust.getUser()).map(U::getName).orElse(SkyBlockProjectTeams.getMessages().nullPlaceholder);
        SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().unTrustCommand, new String[]{username});
    }
}
