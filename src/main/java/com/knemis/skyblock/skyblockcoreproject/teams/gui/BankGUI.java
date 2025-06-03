package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank; // TODO: Update to actual TeamBank class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;

// TODO: Update Team and User to actual classes
public class BankGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> extends BackGUI {

    private final T team;
    private final SkyBlockTeams<T, U> skyblockTeams;
    // private Player player; // Player field is likely handled by BackGUI constructor if it takes Player

    public BankGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        super(skyblockTeams.getInventories().bankGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Ensure Background and Item types match super constructor
        // this.player = player; // Player field is likely handled by BackGUI constructor
        this.team = team;
        this.skyblockTeams = skyblockTeams;
    }

    @NotNull //TODO: Uncomment if super class method has it, or if this is an override ensure it matches
    @Override //TODO: Uncomment if super class method has it, or if this is an override ensure it matches
    public Inventory getInventory() {
        NoItemGUI noItemGUI = skyblockTeams.getInventories().bankGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // 'this' should be an InventoryHolder now
        // Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Bank GUI Title Placeholder"); // Placeholder for title
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        super.addContent(inventory); // Assumes BackGUI has this method and functionality

        // for (BankItem bankItem : skyblockTeams.getBankItemList()) { // TODO: Uncomment when getBankItemList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank teamBank = skyblockTeams.getTeamManager().getTeamBank(team, bankItem.getName()); // TODO: Uncomment when TeamManager and TeamBank are refactored
            // inventory.setItem(bankItem.getItem().slot, ItemStackUtils.makeItem(bankItem.getItem(), Collections.singletonList( // TODO: Ensure bankItem.getItem() is compatible with ItemStackUtils.makeItem
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", skyblockTeams.getConfiguration().numberFormatter.format(teamBank.getNumber())) // This Placeholder is teams specific
            // )));
        // }
    }

    @Override //TODO: Uncomment if super class method has it, or if this is an override ensure it matches
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event); // Assumes BackGUI has this method and functionality

        // Optional<BankItem> bankItem = skyblockTeams.getBankItemList().stream().filter(item -> item.getItem().slot == event.getSlot()).findFirst(); // TODO: Uncomment when getBankItemList and bankItem.getItem() are available
        // if (!bankItem.isPresent()) return;

        // switch (event.getClick()) { // TODO: Uncomment when bankItem is available
            // case LEFT:
                // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case RIGHT:
                // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_LEFT:
                // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
            // case SHIFT_RIGHT:
                // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)}); // TODO: Uncomment when CommandManager and Commands are refactored
                // break;
        // }

        addContent(event.getInventory());
    }
}
