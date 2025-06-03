
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
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
public class PlayerCraftListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorPlayerCraft(CraftItemEvent event) {
        int amount = event.isShiftClick() ? Arrays.stream(event.getInventory().getMatrix())
                .filter(Objects::nonNull)
                .map(ItemStack::getAmount)
                .sorted()
                .findFirst()
                .orElse(1) * event.getRecipe().getResult().getAmount() : event.getRecipe().getResult().getAmount();

        Player player = (Player) event.getWhoClicked();
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        XMaterial material = XMaterial.matchXMaterial(event.getRecipe().getResult().getType());

        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getWhoClicked().getLocation().getWorld(), "CRAFT", material.name(), amount);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (SkyBlockProjectTeams.getTeamManager().isBankItem(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
