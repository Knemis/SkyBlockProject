package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BlockValuesTypeSelectorGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final IridiumTeams<T, U> iridiumTeams;
    private final String teamArg;
    private Player player; // Added player field

    public BlockValuesTypeSelectorGUI(String teamArg, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().blockValuesTypeSelectorGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.iridiumTeams = iridiumTeams;
        this.teamArg = teamArg;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().blockValuesTypeSelectorGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "BlockValuesTypeSelector GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = iridiumTeams.getInventories().blockValuesTypeSelectorGUI;
        // if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // inventory.setItem(blockValuesTypeSelectorInventoryConfig.blocks.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.blocks.item)); // TODO: Replace ItemStackUtils.makeItem
        // }

        // if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // inventory.setItem(blockValuesTypeSelectorInventoryConfig.spawners.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.spawners.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method
        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = iridiumTeams.getInventories().blockValuesTypeSelectorGUI;

        // if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.blocks.item.slot && blockValuesTypeSelectorInventoryConfig.blocks.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().blockValueCommand, new String[]{"blocks", teamArg}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }

        // if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.spawners.item.slot && blockValuesTypeSelectorInventoryConfig.spawners.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().blockValueCommand, new String[]{"spawners", teamArg}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }
    }
}
