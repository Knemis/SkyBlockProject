package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // This is for core.keviincore.utils.Placeholder
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement; // TODO: Update to actual TeamEnhancement class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement; // TODO: Update to actual Enhancement class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData; // TODO: Update to actual EnhancementData class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType; // TODO: Update to actual EnhancementType class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO: Update Team and User to actual classes
public class BoostersGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> extends BackGUI {

    private final T team;
    private final SkyBlockTeams<T, U> skyblockTeams;
    private final Map<Integer, String> boosters = new HashMap<>();
    // private Player player; // Player field is likely handled by BackGUI constructor

    public BoostersGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        super(skyblockTeams.getInventories().boostersGUI.background, player, skyblockTeams.getInventories().backButton);
        // this.player = player;
        this.team = team;
        this.skyblockTeams = skyblockTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = skyblockTeams.getInventories().boostersGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<?>> enhancementEntry : skyblockTeams.getEnhancementList().entrySet()) { // TODO: Ensure Enhancement class is correctly referenced/imported
            if (enhancementEntry.getValue().type != com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.BOOSTER) continue; // TODO: Ensure EnhancementType is correctly referenced/imported
            boosters.put(enhancementEntry.getValue().item.slot, enhancementEntry.getKey());
            com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement teamEnhancement = skyblockTeams.getTeamManager().getTeamEnhancement(team, enhancementEntry.getKey()); // TODO: Ensure TeamEnhancement is correctly referenced/imported
            com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData currentData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel()); // TODO: Ensure EnhancementData is correctly referenced/imported
            com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData nextData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel() + 1);
            int seconds = Math.max((int) (teamEnhancement.getRemainingTime() % 60), 0);
            int minutes = Math.max((int) ((teamEnhancement.getRemainingTime() % 3600) / 60), 0);
            int hours = Math.max((int) (teamEnhancement.getRemainingTime() / 3600), 0);
            int currentLevel = teamEnhancement.isActive(enhancementEntry.getValue().type) ? teamEnhancement.getLevel() : 0;
            String nextLevel = nextData == null ? skyblockTeams.getMessages().nullPlaceholder : String.valueOf(currentLevel + 1);
            String cost = nextData == null ? currentData == null ? skyblockTeams.getMessages().nullPlaceholder : String.valueOf(currentData.money) : String.valueOf(nextData.money);
            String minLevel = nextData == null ? skyblockTeams.getMessages().nullPlaceholder : String.valueOf(nextData.minLevel);
            List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = currentData == null ? new ArrayList<>() : new ArrayList<>(currentData.getPlaceholders()); // This is teams.Placeholder
            placeholders.addAll(Arrays.asList(
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_hours", String.valueOf(hours)),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_minutes", String.valueOf(minutes)),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_seconds", String.valueOf(seconds)),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("current_level", String.valueOf(currentLevel)),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("minLevel", minLevel),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("next_level", nextLevel),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("cost", cost),
                    new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_cost", cost)
            ));

            if(nextData != null) {
                for (Map.Entry<String, Double> bankItem : nextData.bankCosts.entrySet()) {
                    placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_cost", formatPrice(bankItem.getValue())));
                }
            }

            inventory.setItem(enhancementEntry.getValue().item.slot, ItemStackUtils.makeItem(enhancementEntry.getValue().item, placeholders));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        if (!boosters.containsKey(event.getSlot())) return;
        String booster = boosters.get(event.getSlot());
        skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().boostersCommand, new String[]{"buy", booster});
    }

    public String formatPrice(double value) {
        // if (skyblockTeams.getShop().abbreviatePrices) { // TODO: Uncomment when getShop is available
            // return skyblockTeams.getConfiguration().numberFormatter.format(value); // TODO: Uncomment when Configuration and NumberFormatter are refactored
        // }
        return String.valueOf(value); // Placeholder
    }
}
