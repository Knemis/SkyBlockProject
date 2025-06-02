package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;

public class BankGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final T team;
    private final IridiumTeams<T, U> iridiumTeams;
    private Player player; // Added player field as it's used in super/constructor

    public BankGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().bankGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added player field
        this.team = team;
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().bankGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Bank GUI Title Placeholder"); // Placeholder for title
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (BankItem bankItem : iridiumTeams.getBankItemList()) { // TODO: Uncomment when getBankItemList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank = iridiumTeams.getTeamManager().getTeamBank(team, bankItem.getName()); // TODO: Uncomment when TeamManager and TeamBank are refactored
            // inventory.setItem(bankItem.getItem().slot, ItemStackUtils.makeItem(bankItem.getItem(), Collections.singletonList( // TODO: Replace ItemStackUtils.makeItem and bankItem.getItem()
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", iridiumTeams.getConfiguration().numberFormatter.format(teamBank.getNumber())) // TODO: Replace Placeholder, uncomment when Configuration and NumberFormatter are refactored
            // )));
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // Optional<BankItem> bankItem = iridiumTeams.getBankItemList().stream().filter(item -> item.getItem().slot == event.getSlot()).findFirst(); // TODO: Uncomment when getBankItemList and bankItem.getItem() are available
        // if (!bankItem.isPresent()) return;

        // switch (event.getClick()) { // TODO: Uncomment when bankItem is available
            // case LEFT:
                // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case RIGHT:
                // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_LEFT:
                // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_RIGHT:
                // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
        // }

        addContent(event.getInventory());
    }
}
