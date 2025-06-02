package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.MissionTypeSelectorInventoryConfig;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class MissionTypeSelectorGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final keviinTeams<T, U> keviinTeams;

    public MissionTypeSelectorGUI(Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().missionTypeSelectorGUI.background, player, keviinTeams.getInventories().backButton);
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().missionTypeSelectorGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);


        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = keviinTeams.getInventories().missionTypeSelectorGUI;
        if (missionTypeSelectorInventoryConfig.daily.enabled) {
            inventory.setItem(missionTypeSelectorInventoryConfig.daily.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.daily.item));
        }

        if (missionTypeSelectorInventoryConfig.weekly.enabled) {
            inventory.setItem(missionTypeSelectorInventoryConfig.weekly.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.weekly.item));
        }

        if (missionTypeSelectorInventoryConfig.infinite.enabled) {
            inventory.setItem(missionTypeSelectorInventoryConfig.infinite.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.infinite.item));
        }

        if (missionTypeSelectorInventoryConfig.once.enabled) {
            inventory.setItem(missionTypeSelectorInventoryConfig.once.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.once.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = keviinTeams.getInventories().missionTypeSelectorGUI;

        if (event.getSlot() == missionTypeSelectorInventoryConfig.daily.item.slot && missionTypeSelectorInventoryConfig.daily.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().missionsCommand, new String[]{"Daily"});
        }

        if (event.getSlot() == missionTypeSelectorInventoryConfig.weekly.item.slot && missionTypeSelectorInventoryConfig.weekly.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().missionsCommand, new String[]{"Weekly"});
        }

        if (event.getSlot() == missionTypeSelectorInventoryConfig.infinite.item.slot && missionTypeSelectorInventoryConfig.infinite.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().missionsCommand, new String[]{"Infinite"});
        }

        if (event.getSlot() == missionTypeSelectorInventoryConfig.once.item.slot && missionTypeSelectorInventoryConfig.once.enabled) {
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().missionsCommand, new String[]{"Once"});
        }
    }
}
