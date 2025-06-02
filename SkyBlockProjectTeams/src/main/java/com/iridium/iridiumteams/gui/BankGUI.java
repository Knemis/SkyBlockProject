package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.bank.BankItem;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamBank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;

public class BankGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;

    public BankGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().bankGUI.background, player, keviinTeams.getInventories().backButton);
        this.team = team;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().bankGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (BankItem bankItem : keviinTeams.getBankItemList()) {
            TeamBank teamBank = keviinTeams.getTeamManager().getTeamBank(team, bankItem.getName());
            inventory.setItem(bankItem.getItem().slot, ItemStackUtils.makeItem(bankItem.getItem(), Collections.singletonList(
                    new Placeholder("amount", keviinTeams.getConfiguration().numberFormatter.format(teamBank.getNumber()))
            )));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        Optional<BankItem> bankItem = keviinTeams.getBankItemList().stream().filter(item -> item.getItem().slot == event.getSlot()).findFirst();
        if (!bankItem.isPresent()) return;

        switch (event.getClick()) {
            case LEFT:
                keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())});
                break;
            case RIGHT:
                keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(bankItem.get().getDefaultAmount())});
                break;
            case SHIFT_LEFT:
                keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().withdrawCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)});
                break;
            case SHIFT_RIGHT:
                keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().depositCommand, new String[]{bankItem.get().getName(), String.valueOf(Double.MAX_VALUE)});
                break;
        }

        addContent(event.getInventory());
    }
}
