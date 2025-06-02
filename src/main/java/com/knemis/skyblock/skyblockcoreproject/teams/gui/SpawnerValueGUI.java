package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.cryptomorin.xseries.XMaterial;
// import com.keviin.keviincore.gui.PagedGUI; // TODO: Replace with actual PagedGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.BlockValues;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
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

public class SpawnerValueGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.PagedGUI<BlockValues.ValuableBlock> */ { // TODO: Update Team and IridiumUser to actual classes, resolve PagedGUI

    private final T team;
    private final IridiumTeams<T, U> iridiumTeams;
    private Player player; // Added player field
    private int page; // Added page field

    public SpawnerValueGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super( // TODO: Uncomment when PagedGUI is refactored
                // 1,
                // iridiumTeams.getInventories().spawnerValueGUI.size,
                // iridiumTeams.getInventories().spawnerValueGUI.background,
                // iridiumTeams.getInventories().previousPage,
                // iridiumTeams.getInventories().nextPage,
                // player,
                // iridiumTeams.getInventories().backButton
        // );
        this.player = player; // Added
        this.page = 1; // Added
        this.team = team;
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if PagedGUI is a proper GUI base class
        // int maxPages = getPageObjects().size() / (getSize() - 9); // TODO: Uncomment when getPageObjects and getSize are available
        // if (getPageObjects().size() % (getSize() - 9) > 0) maxPages++;

        NoItemGUI noItemGUI = iridiumTeams.getInventories().spawnerValueGUI;
        // Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder. getSize() might not be available.
                // .replace("%page%", String.valueOf(getPage())) // TODO: Uncomment when getPage is available
                // .replace("%max_pages%", String.valueOf(maxPages))
        // ));
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "SpawnerValue GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if PagedGUI is extended and has this method
        // for (Map.Entry<EntityType, BlockValues.ValuableBlock> entry : iridiumTeams.getBlockValues().spawnerValues.entrySet().stream().filter(entry -> entry.getValue().page == getPage()).collect(Collectors.toList())) { // TODO: Uncomment when getBlockValues and getPage are available

            // List<String> lore = new ArrayList<>();
            // lore.add(iridiumTeams.getBlockValues().valueLore // TODO: Uncomment when getBlockValues is available
                    // .replace("%block_value%", String.valueOf(entry.getValue().value))
            // );
            // lore.add(iridiumTeams.getBlockValues().teamValueLore // TODO: Uncomment when getBlockValues and TeamManager are refactored
                    // .replace("%total_blocks%", String.valueOf(iridiumTeams.getTeamManager().getTeamSpawners(team, entry.getKey()).getAmount()))
                    // .replace("%total_block_value%", String.valueOf(iridiumTeams.getTeamManager().getTeamSpawners(team, entry.getKey()).getAmount() * entry.getValue().value))
            // );

            // String itemName = entry.getKey().name().toUpperCase() + "_SPAWN_EGG";
            // XMaterial item = XMaterial.matchXMaterial(itemName).orElse(XMaterial.SPAWNER);

            // inventory.setItem(entry.getValue().slot, ItemStackUtils.makeItem(item, 1, entry.getValue().name, lore)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public Collection<BlockValues.ValuableBlock> getPageObjects() {
        // return iridiumTeams.getBlockValues().spawnerValues.values(); // TODO: Uncomment when getBlockValues is available
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
        return iridiumTeams.getInventories().spawnerValueGUI.size;
    }
}
