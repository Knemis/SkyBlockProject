package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamBlock;
import com.keviin.keviinteams.database.TeamSpawners;
import lombok.AllArgsConstructor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockBreakListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());
        if (team.isPresent()) {

            if (!(event.getBlock().getState() instanceof CreatureSpawner) && !keviinTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BLOCK_BREAK)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotBreakBlocks
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }

            if (event.getBlock().getState() instanceof CreatureSpawner && !keviinTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.SPAWNERS)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotBreakSpawners
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }

        } else {
            keviinTeams.getTeamManager().handleBlockBreakOutsideTerritory(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockBreak(BlockBreakEvent event) {
        U user = keviinTeams.getUserManager().getUser(event.getPlayer());
        XMaterial material = XMaterial.matchXMaterial(event.getBlock().getType());
        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "MINE", material.name(), 1);
        });
        keviinTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), event.getBlock().getLocation()).ifPresent(team -> {
            TeamBlock teamBlock = keviinTeams.getTeamManager().getTeamBlock(team, material);
            teamBlock.setAmount(Math.max(0, teamBlock.getAmount() - 1));

            if (event.getBlock().getState() instanceof CreatureSpawner) {
                CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();
                TeamSpawners teamSpawners = keviinTeams.getTeamManager().getTeamSpawners(team, creatureSpawner.getSpawnedType());
                teamSpawners.setAmount(Math.max(0, teamSpawners.getAmount() - 1));
            }
        });
    }
}
