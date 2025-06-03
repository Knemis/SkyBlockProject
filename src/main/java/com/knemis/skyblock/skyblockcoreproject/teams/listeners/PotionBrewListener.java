
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType; // Added import

@AllArgsConstructor
public class PotionBrewListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorPotionBrew(BrewEvent event) {
        Bukkit.getScheduler().runTask(SkyBlockProjectTeams, () -> SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            for (int i = 0; i < 3; i++) {
                ItemStack itemStack = event.getContents().getItem(i);
                if (itemStack != null && itemStack.getItemMeta() instanceof PotionMeta) {
                    PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

                    // Replace deprecated PotionMeta.getBasePotionData()
                    PotionType potionType = potionMeta.getBasePotionType();
                    String baseTypeName = potionType.name(); // This might be, e.g., SPEED or SPEED_II
                    int level = 1;

                    // Check if the PotionType name itself indicates it's a level II potion
                    if (baseTypeName.endsWith("_II")) {
                        level = 2;
                        // Get the base name, e.g., "SPEED_II" becomes "SPEED"
                        baseTypeName = baseTypeName.substring(0, baseTypeName.length() - 3);
                    }
                    // Note: The original getBasePotionData().isUpgraded() was a boolean for Tier II.
                    // This logic relies on PotionType enum name conventions (e.g., SPEED_II)
                    // for determining Level II, which is a common pattern.

                    String missionKey = baseTypeName + ":" + level;
                    SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "BREW", missionKey, 1);
                }
            }
        }));
    }

}
