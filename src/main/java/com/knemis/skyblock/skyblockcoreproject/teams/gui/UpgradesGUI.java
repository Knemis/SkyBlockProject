package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class UpgradesGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final T team;
    private final IridiumTeams<T, U> iridiumTeams;
    private final Map<Integer, String> upgrades = new HashMap<>();
    private Player player; // Added player field

    public UpgradesGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().upgradesGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().upgradesGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Upgrades GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        upgrades.clear();
        // for (Map.Entry<String, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<?>> enhancementEntry : iridiumTeams.getEnhancementList().entrySet()) { // TODO: Uncomment when getEnhancementList and Enhancement are available
            // if (enhancementEntry.getValue().type != com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE) continue; // TODO: Uncomment when EnhancementType is available
            // upgrades.put(enhancementEntry.getValue().item.slot, enhancementEntry.getKey()); // TODO: Uncomment when item is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement teamEnhancement = iridiumTeams.getTeamManager().getTeamEnhancement(team, enhancementEntry.getKey()); // TODO: Uncomment when TeamManager and TeamEnhancement are refactored
            // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData currentData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel()); // TODO: Uncomment when EnhancementData and teamEnhancement are available
            // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData nextData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel() + 1); // TODO: Uncomment when EnhancementData and teamEnhancement are available
            // int seconds = Math.max((int) (teamEnhancement.getRemainingTime() % 60), 0);
            // int minutes = Math.max((int) ((teamEnhancement.getRemainingTime() % 3600) / 60), 0);
            // int hours = Math.max((int) (teamEnhancement.getRemainingTime() / 3600), 0);
            // String nextLevel = nextData == null ? iridiumTeams.getMessages().nullPlaceholder : String.valueOf(teamEnhancement.getLevel() + 1);
            // String cost = nextData == null ? iridiumTeams.getMessages().nullPlaceholder : String.valueOf(nextData.money);
            // String minLevel = nextData == null ? iridiumTeams.getMessages().nullPlaceholder : String.valueOf(nextData.minLevel);
            // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = currentData == null ? new ArrayList<>() : new ArrayList<>(currentData.getPlaceholders()); // TODO: Replace Placeholder
            // placeholders.addAll(Arrays.asList(
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_hours", String.valueOf(hours)), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_minutes", String.valueOf(minutes)), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_seconds", String.valueOf(seconds)), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("current_level", String.valueOf(teamEnhancement.getLevel())), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("minLevel", minLevel), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("next_level", nextLevel), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("cost", cost), // TODO: Replace Placeholder
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_cost", cost) // TODO: Replace Placeholder
            // ));

            // if(nextData != null) {
                // for (Map.Entry<String, Double> bankItem : nextData.bankCosts.entrySet()) {
                    // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_cost", formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder
                // }
            // }

            // inventory.setItem(enhancementEntry.getValue().item.slot, ItemStackUtils.makeItem(enhancementEntry.getValue().item, placeholders)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        if (!upgrades.containsKey(event.getSlot())) return;
        String upgrade = upgrades.get(event.getSlot());
        // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().upgradesCommand, new String[]{"buy", upgrade}); // TODO: Uncomment when CommandManager and Commands are refactored
    }

    public String formatPrice(double value) {
        // if (iridiumTeams.getShop().abbreviatePrices) { // TODO: Uncomment when getShop is available
            // return iridiumTeams.getConfiguration().numberFormatter.format(value); // TODO: Uncomment when Configuration and NumberFormatter are refactored
        // }
        return String.valueOf(value); // Placeholder
    }
}
