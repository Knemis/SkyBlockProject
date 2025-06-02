package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.PagedGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.BlockValues;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
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

public class BlockValueGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends PagedGUI<BlockValues.ValuableBlock> {

    private final T team;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public BlockValueGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(
                1,
                SkyBlockProjectTeams.getInventories().blockValueGUI.size,
                SkyBlockProjectTeams.getInventories().blockValueGUI.background,
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
        int maxPages = getPageObjects().size() / (getSize() - 9);
        if (getPageObjects().size() % (getSize() - 9) > 0) maxPages++;

        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().blockValueGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title
                .replace("%page%", String.valueOf(getPage()))
                .replace("%max_pages%", String.valueOf(maxPages))
        ));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<XMaterial, BlockValues.ValuableBlock> entry : SkyBlockProjectTeams.getBlockValues().blockValues.entrySet().stream().filter(entry -> entry.getValue().page == getPage()).collect(Collectors.toList())) {

            List<String> lore = new ArrayList<>();
            lore.add(SkyBlockProjectTeams.getBlockValues().valueLore
                    .replace("%block_value%", String.valueOf(entry.getValue().value))
            );
            lore.add(SkyBlockProjectTeams.getBlockValues().teamValueLore
                    .replace("%total_blocks%", String.valueOf(SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount()))
                    .replace("%total_block_value%", String.valueOf(SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, entry.getKey()).getAmount() * entry.getValue().value))
            );

            inventory.setItem(entry.getValue().slot, ItemStackUtils.makeItem(entry.getKey(), 1, entry.getValue().name, lore));
        }
    }

    @Override
    public Collection<BlockValues.ValuableBlock> getPageObjects() {
        return SkyBlockProjectTeams.getBlockValues().blockValues.values();
    }

    @Override
    public ItemStack getItemStack(BlockValues.ValuableBlock valuableBlock) {
        return null;
    }

}
