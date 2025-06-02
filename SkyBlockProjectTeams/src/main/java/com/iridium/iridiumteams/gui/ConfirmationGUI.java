package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.GUI;
import com.keviin.keviincore.utils.InventoryUtils;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ConfirmationGUI<T extends Team, U extends keviinUser<T>> implements GUI {

    private final @NotNull Runnable runnable;
    private final keviinTeams<T, U> keviinTeams;

    public ConfirmationGUI(@NotNull Runnable runnable, keviinTeams<T, U> keviinTeams) {
        this.runnable = runnable;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, keviinTeams.getInventories().confirmationGUI.size, StringUtils.color(keviinTeams.getInventories().confirmationGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, keviinTeams.getInventories().confirmationGUI.background);

        inventory.setItem(keviinTeams.getInventories().confirmationGUI.no.slot, ItemStackUtils.makeItem(keviinTeams.getInventories().confirmationGUI.no));
        inventory.setItem(keviinTeams.getInventories().confirmationGUI.yes.slot, ItemStackUtils.makeItem(keviinTeams.getInventories().confirmationGUI.yes));
    }

    /**
     * Called when there is a click in this GUI.
     * Cancelled automatically.
     *
     * @param event The InventoryClickEvent provided by Bukkit
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getSlot() == keviinTeams.getInventories().confirmationGUI.no.slot) {
            player.closeInventory();
        } else if (event.getSlot() == keviinTeams.getInventories().confirmationGUI.yes.slot) {
            runnable.run();
            player.closeInventory();
        }
    }
}
