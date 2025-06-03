package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite; // TODO: Update to actual TeamInvite class
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

// TODO: Update Team, User, TeamInvite to actual classes
public class InvitesGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> extends PagedGUI<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite> {

    private final T team;
    private final SkyBlockTeams<T, U> skyblockTeams;
    // private Player player; // Player field is likely handled by PagedGUI constructor
    // private int page; // Page field is handled by PagedGUI

    public InvitesGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        super(
                1, // Default page
                skyblockTeams.getInventories().invitesGUI.size,
                skyblockTeams.getInventories().invitesGUI.background,
                skyblockTeams.getInventories().previousPage,
                skyblockTeams.getInventories().nextPage,
                player,
                skyblockTeams.getInventories().backButton
        );
        // this.player = player;
        // this.page = 1;
        this.team = team;
        this.skyblockTeams = skyblockTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = skyblockTeams.getInventories().invitesGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite> getPageObjects() { // TODO: Update TeamInvite to actual class
        return skyblockTeams.getTeamManager().getTeamInvites(team); // TODO: Uncomment when TeamManager is refactored
        // return Collections.emptyList(); // Placeholder
    }

    @Override
    public ItemStack getItemStack(com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite teamInvite) { // TODO: Update TeamInvite to actual class
        Optional<U> user = skyblockTeams.getUserManager().getUserByUUID(teamInvite.getUser()); // TODO: Uncomment when UserManager is refactored
        List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholderList = new ArrayList<>(skyblockTeams.getUserPlaceholderBuilder().getPlaceholders(user)); // This is teams.Placeholder
        placeholderList.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("invite_time", teamInvite.getTime().format(DateTimeFormatter.ofPattern(skyblockTeams.getConfiguration().dateTimeFormat))));
        placeholderList.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("truster", skyblockTeams.getUserManager().getUserByUUID(teamInvite.getTruster()).map(U::getName).orElse(skyblockTeams.getMessages().nullPlaceholder)));
        return ItemStackUtils.makeItem(skyblockTeams.getInventories().invitesGUI.item, placeholderList);
        // return null; // Placeholder
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite teamInvite = getItem(event.getSlot()); // getItem from PagedGUI
        if (teamInvite == null) return;

        String username = skyblockTeams.getUserManager().getUserByUUID(teamInvite.getUser()).map(U::getName).orElse(skyblockTeams.getMessages().nullPlaceholder);
        skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().unInviteCommand, new String[]{username});
    }

    // Helper methods getItem() and getSize() are removed as they should be inherited from PagedGUI
}
