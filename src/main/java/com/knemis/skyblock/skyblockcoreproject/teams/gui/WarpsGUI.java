package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp; // TODO: Update to actual TeamWarp class
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

public class WarpsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final T team;
    private final IridiumTeams<T, U> iridiumTeams;
    private Player player; // Added player field

    public WarpsGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().warpsGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().warpsGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Warps GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        AtomicInteger atomicInteger = new AtomicInteger(1);
        // List<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp> teamWarps = iridiumTeams.getTeamManager().getTeamWarps(team); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // for (com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp teamWarp : teamWarps) { // TODO: Uncomment when teamWarps is available
            // int slot = iridiumTeams.getConfiguration().teamWarpSlots.get(atomicInteger.getAndIncrement()); // TODO: Uncomment when Configuration is refactored
            // ItemStack itemStack = ItemStackUtils.makeItem(iridiumTeams.getInventories().warpsGUI.item, Arrays.asList( // TODO: Replace ItemStackUtils.makeItem
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("island_name", team.getName()), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("warp_name", teamWarp.getName()), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("warp_description", teamWarp.getDescription() != null ? teamWarp.getDescription() : ""), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("warp_creator", Bukkit.getServer().getOfflinePlayer(teamWarp.getUser()).getName()), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("warp_create_time", teamWarp.getCreateTime().format(DateTimeFormatter.ofPattern(iridiumTeams.getConfiguration().dateTimeFormat))) // TODO: Replace Placeholder, uncomment when Configuration is refactored
            // ));
            // Material material = teamWarp.getIcon().parseMaterial();
            // if (material != null) itemStack.setType(material);
            // inventory.setItem(slot, itemStack);
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // List<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp> teamWarps = iridiumTeams.getTeamManager().getTeamWarps(team); // TODO: Uncomment when TeamManager and TeamWarp are refactored
        // for (Map.Entry<Integer, Integer> entrySet : iridiumTeams.getConfiguration().teamWarpSlots.entrySet()) { // TODO: Uncomment when Configuration is refactored
            // if (entrySet.getValue() != event.getSlot()) continue;
            // if (teamWarps.size() < entrySet.getKey()) continue; // TODO: Uncomment when teamWarps is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamWarp teamWarp = teamWarps.get(entrySet.getKey() - 1); // TODO: Uncomment when teamWarps is available
            // switch (event.getClick()) {
                // case LEFT:
                    // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().warpCommand, new String[]{teamWarp.getName()}); // TODO: Uncomment when CommandManager and Commands are refactored
                    // return;
                // case RIGHT:
                    // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().deleteWarpCommand, new String[]{teamWarp.getName()}); // TODO: Uncomment when CommandManager and Commands are refactored
            // }
        // }
    }
}
