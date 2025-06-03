package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.BlockValues;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public class BlockValueGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.PagedGUI<BlockValues.ValuableBlock> */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve PagedGUI

    private final T team;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private Player player; // Added player field
    private int page; // Added page field

    public BlockValueGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super( // TODO: Uncomment when PagedGUI is refactored
                // 1,
                // SkyBlockProjectTeams.getInventories().blockValueGUI.size,
                // SkyBlockProjectTeams.getInventories().blockValueGUI.background,
                // SkyBlockProjectTeams.getInventories().previousPage,
                // SkyBlockProjectTeams.getInventories().nextPage,
                // player,
                // SkyBlockProjectTeams.getInventories().backButton
        // );
        this.player = player; // Added
        this.page = 1; // Added
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if PagedGUI is a proper GUI base class
        // int maxPages = getPageObjects().size() / (getSize() - 9); // TODO: Uncomment when getPageObjects and getSize are available
        // if (getPageObjects().size() % (getSize() - 9) > 0) maxPages++;

        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().blockValueGUI;
        // Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder. getSize() might not be available.
                // .replace("%page%", String.valueOf(getPage())) // TODO: Uncomment when getPage is available
                // .replace("%max_pages%", String.valueOf(maxPages))
        // ));
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "BlockValue GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if PagedGUI is extended and has this method

        // for (Map.Entry<XMaterial, BlockValues.ValuableBlock> entry : SkyBlockProjectTeams.getBlockValues().blockValues.entrySet().stream().filter(entry -> entry.getValue().page == getPage()).collect(Collectors.toList())) { // TODO: Uncomment when getBlockValues and getPage are available

            // List<String> lore = new ArrayList<>();
            // lore.add(SkyBlockProjectTeams.getBlockValues().valueLore // TODO: Uncomment when getBlockValues is available
                    // .replace("%block_value%", String.valueOf(entry.getValue().value))
            // );
            // lore.add(SkyBlockProjectTeams.getBlockValues().teamValueLore // TODO: Uncomment when getBlockValues and TeamManager are refactored
                    // .replace("%total_blocks%", String.valueOf(SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount()))
                    // .replace("%total_block_value%", String.valueOf(SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount() * entry.getValue().value))
            // );

            // inventory.setItem(entry.getValue().slot, ItemStackUtils.makeItem(entry.getKey(), 1, entry.getValue().name, lore)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public Collection<BlockValues.ValuableBlock> getPageObjects() {
        // return SkyBlockProjectTeams.getBlockValues().blockValues.values(); // TODO: Uncomment when getBlockValues is available
        return Collections.emptyList(); // Placeholder
    }

    // @Override //TODO: Uncomment if super class method has it
    public ItemStack getItemStack(BlockValues.ValuableBlock valuableBlock) {
        return null;
    }

    // Helper methods to replace PagedGUI functionality for now
    public int getPage(){
        return page;
    }

    public int getSize(){
        // Return a default size or size from config
        return SkyBlockProjectTeams.getInventories().blockValueGUI.size;
    }
}
