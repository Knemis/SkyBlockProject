package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.GUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.InventoryUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ConfirmationGUI<T extends Team, U extends SkyBlockProjectUser<T>> implements GUI {

    private final @NotNull Runnable runnable;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public ConfirmationGUI(@NotNull Runnable runnable, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.runnable = runnable;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().confirmationGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().confirmationGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, SkyBlockProjectTeams.getInventories().confirmationGUI.background);

        inventory.setItem(SkyBlockProjectTeams.getInventories().confirmationGUI.no.slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().confirmationGUI.no));
        inventory.setItem(SkyBlockProjectTeams.getInventories().confirmationGUI.yes.slot, ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().confirmationGUI.yes));
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
        if (event.getSlot() == SkyBlockProjectTeams.getInventories().confirmationGUI.no.slot) {
            player.closeInventory();
        } else if (event.getSlot() == SkyBlockProjectTeams.getInventories().confirmationGUI.yes.slot) {
            runnable.run();
            player.closeInventory();
        }
    }
}
