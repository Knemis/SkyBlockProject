package com.keviin.keviinteams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamWarp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WarpsGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;

    public WarpsGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().warpsGUI.background, player, keviinTeams.getInventories().backButton);
        this.team = team;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().warpsGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        AtomicInteger atomicInteger = new AtomicInteger(1);
        List<TeamWarp> teamWarps = keviinTeams.getTeamManager().getTeamWarps(team);
        for (TeamWarp teamWarp : teamWarps) {
            int slot = keviinTeams.getConfiguration().teamWarpSlots.get(atomicInteger.getAndIncrement());
            ItemStack itemStack = ItemStackUtils.makeItem(keviinTeams.getInventories().warpsGUI.item, Arrays.asList(
                    new Placeholder("island_name", team.getName()),
                    new Placeholder("warp_name", teamWarp.getName()),
                    new Placeholder("warp_description", teamWarp.getDescription() != null ? teamWarp.getDescription() : ""),
                    new Placeholder("warp_creator", Bukkit.getServer().getOfflinePlayer(teamWarp.getUser()).getName()),
                    new Placeholder("warp_create_time", teamWarp.getCreateTime().format(DateTimeFormatter.ofPattern(keviinTeams.getConfiguration().dateTimeFormat)))
            ));
            Material material = teamWarp.getIcon().parseMaterial();
            if (material != null) itemStack.setType(material);
            inventory.setItem(slot, itemStack);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        List<TeamWarp> teamWarps = keviinTeams.getTeamManager().getTeamWarps(team);
        for (Map.Entry<Integer, Integer> entrySet : keviinTeams.getConfiguration().teamWarpSlots.entrySet()) {
            if (entrySet.getValue() != event.getSlot()) continue;
            if (teamWarps.size() < entrySet.getKey()) continue;
            TeamWarp teamWarp = teamWarps.get(entrySet.getKey() - 1);
            switch (event.getClick()) {
                case LEFT:
                    keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().warpCommand, new String[]{teamWarp.getName()});
                    return;
                case RIGHT:
                    keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().deleteWarpCommand, new String[]{teamWarp.getName()});
            }
        }
    }
}
