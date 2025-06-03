package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBlock;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSpawners;
import lombok.AllArgsConstructor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockPlaceListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (SkyBlockProjectTeams.getTeamManager().isBankItem(event.getItemInHand())) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());

        if (team.isPresent()) {
            if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BLOCK_PLACE)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotPlaceBlocks
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        } else {
            SkyBlockProjectTeams.getTeamManager().handleBlockPlaceOutsideTerritory(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorBlockPlace(BlockPlaceEvent event) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(event.getPlayer());
        XMaterial material = XMaterial.matchXMaterial(event.getBlock().getType());
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "PLACE", material.name(), 1);
        });
        SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), event.getBlock().getLocation()).ifPresent(team -> {
            TeamBlock teamBlock = SkyBlockProjectTeams.getTeamManager().getTeamBlock(team, material);
            teamBlock.setAmount(teamBlock.getAmount() + 1);

            if (event.getBlock().getState() instanceof CreatureSpawner) {
                CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();

                if(creatureSpawner.getSpawnedType() == null) return;

                TeamSpawners teamSpawners = SkyBlockProjectTeams.getTeamManager().getTeamSpawners(team, creatureSpawner.getSpawnedType());
                teamSpawners.setAmount(teamSpawners.getAmount() + 1);
            }
        });
    }
}
