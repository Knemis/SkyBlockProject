package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BlockValuesTypeSelectorGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final String teamArg;

    public BlockValuesTypeSelectorGUI(String teamArg, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.teamArg = teamArg;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;
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
        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.blocks.item.slot && blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
            SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().blockValueCommand, new String[]{"blocks", teamArg});
        }

        if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.spawners.item.slot && blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
            SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().blockValueCommand, new String[]{"spawners", teamArg});
        }
    }
}
