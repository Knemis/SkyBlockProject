package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviincore.gui.PagedGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.BlockValues;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnerValueGUI<T extends Team, U extends keviinUser<T>> extends PagedGUI<BlockValues.ValuableBlock> {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;

    public SpawnerValueGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(
                1,
                keviinTeams.getInventories().spawnerValueGUI.size,
                keviinTeams.getInventories().spawnerValueGUI.background,
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
        int maxPages = getPageObjects().size() / (getSize() - 9);
        if (getPageObjects().size() % (getSize() - 9) > 0) maxPages++;

        NoItemGUI noItemGUI = keviinTeams.getInventories().spawnerValueGUI;
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
        for (Map.Entry<EntityType, BlockValues.ValuableBlock> entry : keviinTeams.getBlockValues().spawnerValues.entrySet().stream().filter(entry -> entry.getValue().page == getPage()).collect(Collectors.toList())) {

            List<String> lore = new ArrayList<>();
            lore.add(keviinTeams.getBlockValues().valueLore
                    .replace("%block_value%", String.valueOf(entry.getValue().value))
            );
            lore.add(keviinTeams.getBlockValues().teamValueLore
                    .replace("%total_blocks%", String.valueOf(keviinTeams.getTeamManager().getTeamSpawners(team, entry.getKey()).getAmount()))
                    .replace("%total_block_value%", String.valueOf(keviinTeams.getTeamManager().getTeamSpawners(team, entry.getKey()).getAmount() * entry.getValue().value))
            );

            String itemName = entry.getKey().name().toUpperCase() + "_SPAWN_EGG";
            XMaterial item = XMaterial.matchXMaterial(itemName).orElse(XMaterial.SPAWNER);

            inventory.setItem(entry.getValue().slot, ItemStackUtils.makeItem(item, 1, entry.getValue().name, lore));
        }
    }

    @Override
    public Collection<BlockValues.ValuableBlock> getPageObjects() {
        return keviinTeams.getBlockValues().spawnerValues.values();
    }

    @Override
    public ItemStack getItemStack(BlockValues.ValuableBlock valuableBlock) {
        return null;
    }

}
