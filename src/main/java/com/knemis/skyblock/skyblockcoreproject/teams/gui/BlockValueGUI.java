package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.BlockValues;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Update Team and User to actual classes
public class BlockValueGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> extends PagedGUI<BlockValues.ValuableBlock> {

    private final T team;
    private final SkyBlockTeams<T, U> skyblockTeams;
    // private Player player; // Player field is likely handled by PagedGUI constructor
    // private int page; // Page field is handled by PagedGUI

    public BlockValueGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        super(
                1, // Default page
                skyblockTeams.getInventories().blockValueGUI.size,
                skyblockTeams.getInventories().blockValueGUI.background,
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
        int maxPages = getPageObjects().size() / (getSize() - 9); // getSize() and getPageObjects() should be from PagedGUI
        if (getPageObjects().size() % (getSize() - 9) > 0) maxPages++;

        NoItemGUI noItemGUI = skyblockTeams.getInventories().blockValueGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title
                .replace("%page%", String.valueOf(getPage())) // getPage() should be from PagedGUI
                .replace("%max_pages%", String.valueOf(maxPages))
        ));
        // Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "BlockValue GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory); // Assumes PagedGUI has this method

        for (Map.Entry<XMaterial, BlockValues.ValuableBlock> entry : skyblockTeams.getBlockValues().blockValues.entrySet().stream().filter(valuableBlockEntry -> valuableBlockEntry.getValue().page == getPage()).collect(Collectors.toList())) {

            List<String> lore = new ArrayList<>();
            lore.add(skyblockTeams.getBlockValues().valueLore
                    .replace("%block_value%", String.valueOf(entry.getValue().value))
            );
            lore.add(skyblockTeams.getBlockValues().teamValueLore
                    .replace("%total_blocks%", String.valueOf(skyblockTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount())) // TODO: Ensure TeamManager and getTeamBlock are functional
                    .replace("%total_block_value%", String.valueOf(skyblockTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount() * entry.getValue().value)) // TODO: Ensure TeamManager and getTeamBlock are functional
            );

            inventory.setItem(entry.getValue().slot, ItemStackUtils.makeItem(entry.getKey(), 1, entry.getValue().name, lore));
        }
    }

    @Override
    public Collection<BlockValues.ValuableBlock> getPageObjects() {
        return skyblockTeams.getBlockValues().blockValues.values(); // TODO: Ensure getBlockValues is functional
        // return Collections.emptyList(); // Placeholder
    }

    @Override
    public ItemStack getItemStack(BlockValues.ValuableBlock valuableBlock) {
        // This method seems to be required by PagedGUI to render items,
        // but the addContent method above directly adds items.
        // If PagedGUI's addContent calls this, then this needs to be implemented properly.
        // For now, returning null as the direct addContent is used.
        // If the direct addContent is removed in favor of PagedGUI handling, this needs implementation.
        // Example:
        // List<String> lore = new ArrayList<>();
        // lore.add(skyblockTeams.getBlockValues().valueLore.replace("%block_value%", String.valueOf(valuableBlock.value)));
        // lore.add(skyblockTeams.getBlockValues().teamValueLore
        // .replace("%total_blocks%", String.valueOf(skyblockTeams.getTeamManager().getTeamBlock(team, /*Need XMaterial here from valuableBlock*/).getAmount()))
        // .replace("%total_block_value%", String.valueOf(skyblockTeams.getTeamManager().getTeamBlock(team, /*Need XMaterial here from valuableBlock*/).getAmount() * valuableBlock.value))
        // );
        // return ItemStackUtils.makeItem(XMaterial.matchXMaterial(valuableBlock.materialName).orElse(XMaterial.STONE), 1, valuableBlock.name, lore);
        return null;
    }

    // Helper methods getPage() and getSize() are removed as they should be inherited from PagedGUI
}
