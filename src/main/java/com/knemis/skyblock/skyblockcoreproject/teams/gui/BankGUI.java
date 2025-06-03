package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BankGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final T team;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private Player player; // Added player field as it's used in super/constructor

    public BankGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().bankGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added player field
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().bankGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Bank GUI Title Placeholder"); // Placeholder for title
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (BankItem bankItem : SkyBlockProjectTeams.getBankItemList()) { // TODO: Uncomment when getBankItemList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank = SkyBlockProjectTeams.getTeamManager().getTeamBank(team, bankItem.getName()); // TODO: Uncomment when TeamManager and TeamBank are refactored
            // inventory.setItem(bankItem.getItem().slot, ItemStackUtils.makeItem(bankItem.getItem(), Collections.singletonList( // TODO: Replace ItemStackUtils.makeItem and bankItem.getItem()
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", SkyBlockProjectTeams.getConfiguration().numberFormatter.format(teamBank.getNumber())) // TODO: Replace Placeholder, uncomment when Configuration and NumberFormatter are refactored
            // )));
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // Optional<BankItem> bankItem = SkyBlockProjectTeams.getBankItemList().stream().filter(item -> item.getItem().slot == event.getSlot()).findFirst(); // TODO: Uncomment when getBankItemList and bankItem.getItem() are available
        // if (!bankItem.isPresent()) return;

        // switch (event.getClick()) { // TODO: Uncomment when bankItem is available
            // case LEFT:
                // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case RIGHT:
                // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_LEFT:
                // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_RIGHT:
                // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
        // }

        addContent(event.getInventory());
    }
}
