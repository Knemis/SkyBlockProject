package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData;
import lombok.AllArgsConstructor;
import org.bukkit.Effect;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.Arrays;
import java.util.List;


@AllArgsConstructor
public class BlockGrowListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final List<XMaterial> ageIgnoreList = Arrays.asList(XMaterial.SUGAR_CANE, XMaterial.CACTUS, XMaterial.BAMBOO, XMaterial.CAVE_VINES);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockGrow(BlockGrowEvent event) {
        XMaterial material = XMaterial.matchXMaterial(event.getNewState().getType());
        SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            if (event.getNewState().getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) event.getNewState().getBlockData();

                if (ageable.getAge() >= ageable.getMaximumAge() || ageIgnoreList.contains(material)) {
                    SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "GROW", material.name(), 1);
                }

                Enhancement<FarmingEnhancementData> farmingEnhancement = SkyBlockProjectTeams.getEnhancements().farmingEnhancement;
                TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "farming");
                FarmingEnhancementData data = farmingEnhancement.levels.get(teamEnhancement.getLevel());
                if (teamEnhancement.isActive(farmingEnhancement.type) && data != null) {
                    ageable.setAge(Math.min(ageable.getAge() + data.farmingModifier, ageable.getMaximumAge()));
                    event.getNewState().setBlockData(ageable);
                    event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.BONE_MEAL_USE, 0);
                }

            } else {
                SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "GROW", material.name(), 1);
            }
        });

    }

    // Some stuff like bamboo growing comes under this for some reason
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockSpread(BlockSpreadEvent event) {
        monitorBlockGrow(event);
    }

}
