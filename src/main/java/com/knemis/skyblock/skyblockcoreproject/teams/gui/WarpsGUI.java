package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp;
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

public class WarpsGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final T team;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public WarpsGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().warpsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().warpsGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        AtomicInteger atomicInteger = new AtomicInteger(1);
        List<TeamWarp> teamWarps = SkyBlockProjectTeams.getTeamManager().getTeamWarps(team);
        for (TeamWarp teamWarp : teamWarps) {
            int slot = SkyBlockProjectTeams.getConfiguration().teamWarpSlots.get(atomicInteger.getAndIncrement());
            ItemStack itemStack = ItemStackUtils.makeItem(SkyBlockProjectTeams.getInventories().warpsGUI.item, Arrays.asList(
                    new Placeholder("island_name", team.getName()),
                    new Placeholder("warp_name", teamWarp.getName()),
                    new Placeholder("warp_description", teamWarp.getDescription() != null ? teamWarp.getDescription() : ""),
                    new Placeholder("warp_creator", Bukkit.getServer().getOfflinePlayer(teamWarp.getUser()).getName()),
                    new Placeholder("warp_create_time", teamWarp.getCreateTime().format(DateTimeFormatter.ofPattern(SkyBlockProjectTeams.getConfiguration().dateTimeFormat)))
            ));
            Material material = teamWarp.getIcon().parseMaterial();
            if (material != null) itemStack.setType(material);
            inventory.setItem(slot, itemStack);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        List<TeamWarp> teamWarps = SkyBlockProjectTeams.getTeamManager().getTeamWarps(team);
        for (Map.Entry<Integer, Integer> entrySet : SkyBlockProjectTeams.getConfiguration().teamWarpSlots.entrySet()) {
            if (entrySet.getValue() != event.getSlot()) continue;
            if (teamWarps.size() < entrySet.getKey()) continue;
            TeamWarp teamWarp = teamWarps.get(entrySet.getKey() - 1);
            switch (event.getClick()) {
                case LEFT:
                    SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().warpCommand, new String[]{teamWarp.getName()});
                    return;
                case RIGHT:
                    SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().deleteWarpCommand, new String[]{teamWarp.getName()});
            }
        }
    }
}
