package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BlockValuesTypeSelectorGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final keviinTeams<T, U> keviinTeams;
    private final String teamArg;

    public BlockValuesTypeSelectorGUI(String teamArg, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().blockValuesTypeSelectorGUI.background, player, keviinTeams.getInventories().backButton);
        this.keviinTeams = keviinTeams;
        this.teamArg = teamArg;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().blockValuesTypeSelectorGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = keviinTeams.getInventories().blockValuesTypeSelectorGUI;
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
        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = keviinTeams.getInventories().blockValuesTypeSelectorGUI;

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.blocks.item.slot && blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().blockValueCommand, new String[]{"blocks", teamArg});
        }

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.spawners.item.slot && blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().blockValueCommand, new String[]{"spawners", teamArg});
        }
    }
}
