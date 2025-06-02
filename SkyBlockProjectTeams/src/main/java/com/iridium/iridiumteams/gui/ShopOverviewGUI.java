package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.Shop;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShopOverviewGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {
    private final keviinTeams<T, U> keviinTeams;

    public ShopOverviewGUI(Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().shopOverviewGUI.background, player, keviinTeams.getInventories().backButton);
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().shopOverviewGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Shop.ShopCategory category : keviinTeams.getShop().categories.values()) {
            inventory.setItem(category.item.slot, ItemStackUtils.makeItem(category.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        for (Map.Entry<String, Shop.ShopCategory> category : keviinTeams.getShop().categories.entrySet()) {
            if (event.getSlot() != category.getValue().item.slot) continue;
            event.getWhoClicked().openInventory(new ShopCategoryGUI<>(category.getKey(), (Player) event.getWhoClicked(), 1, keviinTeams).getInventory());
            return;
        }
        super.onInventoryClick(event);
    }
}


