package com.knemis.skyblock.skyblockcoreproject.teams.gui;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShopOverviewGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public ShopOverviewGUI(Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().shopOverviewGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().shopOverviewGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Shop.ShopCategory category : SkyBlockProjectTeams.getShop().categories.values()) {
            inventory.setItem(category.item.slot, ItemStackUtils.makeItem(category.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        for (Map.Entry<String, Shop.ShopCategory> category : SkyBlockProjectTeams.getShop().categories.entrySet()) {
            if (event.getSlot() != category.getValue().item.slot) continue;
            event.getWhoClicked().openInventory(new ShopCategoryGUI<>(category.getKey(), (Player) event.getWhoClicked(), 1, SkyBlockProjectTeams).getInventory());
            return;
        }
        super.onInventoryClick(event);
    }
}


