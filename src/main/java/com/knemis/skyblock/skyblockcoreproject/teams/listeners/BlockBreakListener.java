package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBlock;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSpawners;
import lombok.AllArgsConstructor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockBreakListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());
        if (team.isPresent()) {

            if (!(event.getBlock().getState() instanceof CreatureSpawner) && !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BLOCK_BREAK)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotBreakBlocks
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }

            if (event.getBlock().getState() instanceof CreatureSpawner && !SkyBlockProjectTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.SPAWNERS)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotBreakSpawners
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }

        } else {
            SkyBlockProjectTeams.getTeamManager().handleBlockBreakOutsideTerritory(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockBreak(BlockBreakEvent event) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(event.getPlayer());
        XMaterial material = XMaterial.matchXMaterial(event.getBlock().getType());
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "MINE", material.name(), 1);
        });
        SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), event.getBlock().getLocation()).ifPresent(team -> {
            TeamBlock teamBlock = SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, material);
            teamBlock.setAmount(Math.max(0, teamBlock.getAmount() - 1));

            if (event.getBlock().getState() instanceof CreatureSpawner) {
                CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();
                TeamSpawners teamSpawners = SkyBlockProjectTeams.getTeamManager().getTeamSpawners(team, creatureSpawner.getSpawnedType());
                teamSpawners.setAmount(Math.max(0, teamSpawners.getAmount() - 1));
            }
        });
    }
}
