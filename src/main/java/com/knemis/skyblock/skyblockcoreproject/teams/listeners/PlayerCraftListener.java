
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
public class PlayerCraftListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorPlayerCraft(CraftItemEvent event) {
        int amount = event.isShiftClick() ? Arrays.stream(event.getInventory().getMatrix())
                .filter(Objects::nonNull)
                .map(ItemStack::getAmount)
                .sorted()
                .findFirst()
                .orElse(1) * event.getRecipe().getResult().getAmount() : event.getRecipe().getResult().getAmount();

        Player player = (Player) event.getWhoClicked();
        U user = keviinTeams.getUserManager().getUser(player);
        XMaterial material = XMaterial.matchXMaterial(event.getRecipe().getResult().getType());

        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, event.getWhoClicked().getLocation().getWorld(), "CRAFT", material.name(), amount);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (keviinTeams.getTeamManager().isBankItem(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
