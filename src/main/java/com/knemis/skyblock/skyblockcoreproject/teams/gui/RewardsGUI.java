package com.knemis.skyblock.skyblockcoreproject.teams.gui;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RewardsGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends PagedGUI<TeamReward> {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;

    public RewardsGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(
                1,
                SkyBlockProjectTeams.getInventories().rewardsGUI.size,
                SkyBlockProjectTeams.getInventories().rewardsGUI.background,
                SkyBlockProjectTeams.getInventories().previousPage,
                SkyBlockProjectTeams.getInventories().nextPage,
                player,
                SkyBlockProjectTeams.getInventories().backButton
        );
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);
        Item item = SkyBlockProjectTeams.getInventories().rewardsGUI.item;
        inventory.setItem(item.slot, ItemStackUtils.makeItem(item));
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().rewardsGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<TeamReward> getPageObjects() {
        return SkyBlockProjectTeams.getTeamManager().getTeamRewards(team);
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

        if(event.getSlot() == SkyBlockProjectTeams.getInventories().rewardsGUI.item.slot){
            for(TeamReward teamReward : getPageObjects()){
                SkyBlockProjectTeams.getTeamManager().claimTeamReward(teamReward, (Player) event.getWhoClicked());
            }
            return;
        }

        TeamReward teamReward = getItem(event.getSlot());
        if (teamReward == null) return;
        SkyBlockProjectTeams.getTeamManager().claimTeamReward(teamReward, (Player) event.getWhoClicked());
    }
}
