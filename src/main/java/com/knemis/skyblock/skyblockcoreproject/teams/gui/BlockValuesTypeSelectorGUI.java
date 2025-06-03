package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

// TODO: Update Team and User to actual classes
public class BlockValuesTypeSelectorGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> extends BackGUI {

    private final SkyBlockTeams<T, U> skyblockTeams;
    private final String teamArg;
    // private Player player; // Player field is likely handled by BackGUI constructor

    public BlockValuesTypeSelectorGUI(String teamArg, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        super(skyblockTeams.getInventories().blockValuesTypeSelectorGUI.background, player, skyblockTeams.getInventories().backButton);
        // this.player = player;
        this.skyblockTeams = skyblockTeams;
        this.teamArg = teamArg;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = skyblockTeams.getInventories().blockValuesTypeSelectorGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = skyblockTeams.getInventories().blockValuesTypeSelectorGUI;
        if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
            inventory.setItem(blockValuesTypeSelectorInventoryConfig.blocks.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.blocks.item));
        }

        if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
            inventory.setItem(blockValuesTypeSelectorInventoryConfig.spawners.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.spawners.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = skyblockTeams.getInventories().blockValuesTypeSelectorGUI;

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.blocks.item.slot && blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
            skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().blockValueCommand, new String[]{"blocks", teamArg});
        }

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.spawners.item.slot && blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
            skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().blockValueCommand, new String[]{"spawners", teamArg});
        }
    }
}
