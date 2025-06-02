package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.Item;
import com.keviin.keviincore.gui.PagedGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RewardsGUI<T extends Team, U extends keviinUser<T>> extends PagedGUI<TeamReward> {

    private final keviinTeams<T, U> keviinTeams;
    private final T team;

    public RewardsGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(
                1,
                keviinTeams.getInventories().rewardsGUI.size,
                keviinTeams.getInventories().rewardsGUI.background,
                keviinTeams.getInventories().previousPage,
                keviinTeams.getInventories().nextPage,
                player,
                keviinTeams.getInventories().backButton
        );
        this.keviinTeams = keviinTeams;
        this.team = team;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);
        Item item = keviinTeams.getInventories().rewardsGUI.item;
        inventory.setItem(item.slot, ItemStackUtils.makeItem(item));
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().rewardsGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<TeamReward> getPageObjects() {
        return keviinTeams.getTeamManager().getTeamRewards(team);
    }

    @Override
    public ItemStack getItemStack(TeamReward teamReward) {
        return ItemStackUtils.makeItem(teamReward.getReward().item);
    }

    @Override
    public boolean isPaged() {
        return true;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if(event.getSlot() == keviinTeams.getInventories().rewardsGUI.item.slot){
            for(TeamReward teamReward : getPageObjects()){
                keviinTeams.getTeamManager().claimTeamReward(teamReward, (Player) event.getWhoClicked());
            }
            return;
        }

        TeamReward teamReward = getItem(event.getSlot());
        if (teamReward == null) return;
        keviinTeams.getTeamManager().claimTeamReward(teamReward, (Player) event.getWhoClicked());
    }
}
