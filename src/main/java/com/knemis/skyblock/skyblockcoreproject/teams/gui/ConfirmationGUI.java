package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.GUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.InventoryUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ConfirmationGUI<T extends Team, U extends User<T>> implements GUI {

    private final @NotNull Runnable runnable;
    private final SkyBlockTeams<T, U> skyblockTeams;

    public ConfirmationGUI(@NotNull Runnable runnable, SkyBlockTeams<T, U> skyblockTeams) {
        this.runnable = runnable;
        this.skyblockTeams = skyblockTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, skyblockTeams.getInventories().confirmationGUI.size, StringUtils.color(skyblockTeams.getInventories().confirmationGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, skyblockTeams.getInventories().confirmationGUI.background);

        inventory.setItem(skyblockTeams.getInventories().confirmationGUI.no.slot, ItemStackUtils.makeItem(skyblockTeams.getInventories().confirmationGUI.no));
        inventory.setItem(skyblockTeams.getInventories().confirmationGUI.yes.slot, ItemStackUtils.makeItem(skyblockTeams.getInventories().confirmationGUI.yes));
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
        if (event.getSlot() == skyblockTeams.getInventories().confirmationGUI.no.slot) {
            player.closeInventory();
        } else if (event.getSlot() == skyblockTeams.getInventories().confirmationGUI.yes.slot) {
            runnable.run();
            player.closeInventory();
        }
    }
}
