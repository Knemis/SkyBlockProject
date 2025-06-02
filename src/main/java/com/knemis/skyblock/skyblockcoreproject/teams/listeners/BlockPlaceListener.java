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
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockPlaceListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (keviinTeams.getTeamManager().isBankItem(event.getItemInHand())) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());

        if (team.isPresent()) {
            if (!keviinTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BLOCK_PLACE)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotPlaceBlocks
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        } else {
            keviinTeams.getTeamManager().handleBlockPlaceOutsideTerritory(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockPlace(BlockPlaceEvent event) {
        U user = keviinTeams.getUserManager().getUser(event.getPlayer());
        XMaterial material = XMaterial.matchXMaterial(event.getBlock().getType());
        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "PLACE", material.name(), 1);
        });
        keviinTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), event.getBlock().getLocation()).ifPresent(team -> {
            TeamBlock teamBlock = keviinTeams.getTeamManager().getTeamBlock(team, material);
            teamBlock.setAmount(teamBlock.getAmount() + 1);

            if (event.getBlock().getState() instanceof CreatureSpawner) {
                CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();

                if(creatureSpawner.getSpawnedType() == null) return;

                TeamSpawners teamSpawners = keviinTeams.getTeamManager().getTeamSpawners(team, creatureSpawner.getSpawnedType());
                teamSpawners.setAmount(teamSpawners.getAmount() + 1);
            }
        });
    }
}
