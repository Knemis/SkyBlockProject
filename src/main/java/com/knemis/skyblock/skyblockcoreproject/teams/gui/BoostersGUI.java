package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamEnhancement;
import com.keviin.keviinteams.enhancements.Enhancement;
import com.keviin.keviinteams.enhancements.EnhancementData;
import com.keviin.keviinteams.enhancements.EnhancementType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BoostersGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final T team;
    private final keviinTeams<T, U> keviinTeams;
    private final Map<Integer, String> boosters = new HashMap<>();

    public BoostersGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().boostersGUI.background, player, keviinTeams.getInventories().backButton);
        this.team = team;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().boostersGUI;
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, Enhancement<?>> enhancementEntry : keviinTeams.getEnhancementList().entrySet()) {
            if (enhancementEntry.getValue().type != EnhancementType.BOOSTER) continue;
            boosters.put(enhancementEntry.getValue().item.slot, enhancementEntry.getKey());
            TeamEnhancement teamEnhancement = keviinTeams.getTeamManager().getTeamEnhancement(team, enhancementEntry.getKey());
            EnhancementData currentData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel());
            EnhancementData nextData = enhancementEntry.getValue().levels.get(teamEnhancement.getLevel() + 1);
            int seconds = Math.max((int) (teamEnhancement.getRemainingTime() % 60), 0);
            int minutes = Math.max((int) ((teamEnhancement.getRemainingTime() % 3600) / 60), 0);
            int hours = Math.max((int) (teamEnhancement.getRemainingTime() / 3600), 0);
            int currentLevel = teamEnhancement.isActive(enhancementEntry.getValue().type) ? teamEnhancement.getLevel() : 0;
            String nextLevel = nextData == null ? keviinTeams.getMessages().nullPlaceholder : String.valueOf(currentLevel + 1);
            String cost = nextData == null ? currentData == null ? keviinTeams.getMessages().nullPlaceholder : String.valueOf(currentData.money) : String.valueOf(nextData.money);
            String minLevel = nextData == null ? keviinTeams.getMessages().nullPlaceholder : String.valueOf(nextData.minLevel);
            List<Placeholder> placeholders = currentData == null ? new ArrayList<>() : new ArrayList<>(currentData.getPlaceholders());
            placeholders.addAll(Arrays.asList(
                    new Placeholder("timeremaining_hours", String.valueOf(hours)),
                    new Placeholder("timeremaining_minutes", String.valueOf(minutes)),
                    new Placeholder("timeremaining_seconds", String.valueOf(seconds)),
                    new Placeholder("current_level", String.valueOf(currentLevel)),
                    new Placeholder("minLevel", minLevel),
                    new Placeholder("next_level", nextLevel),
                    new Placeholder("cost", cost),
                    new Placeholder("vault_cost", cost)
            ));

            if(nextData != null) {
                for (Map.Entry<String, Double> bankItem : nextData.bankCosts.entrySet()) {
                    placeholders.add(new Placeholder(bankItem.getKey() + "_cost", formatPrice(bankItem.getValue())));
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
        keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().boostersCommand, new String[]{"buy", booster});
    }

    public String formatPrice(double value) {
        if (keviinTeams.getShop().abbreviatePrices) {
            return keviinTeams.getConfiguration().numberFormatter.format(value);
        }
        return String.valueOf(value);
    }
}
