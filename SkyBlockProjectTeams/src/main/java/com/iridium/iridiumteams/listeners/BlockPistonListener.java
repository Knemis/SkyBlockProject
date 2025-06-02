package com.keviin.keviinteams.listeners;

import com.google.common.collect.ImmutableMap;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class BlockPistonListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    private static final Map<BlockFace, int[]> offsets = ImmutableMap.<BlockFace, int[]>builder()
            .put(BlockFace.EAST, new int[]{1, 0, 0})
            .put(BlockFace.WEST, new int[]{-1, 0, 0})
            .put(BlockFace.UP, new int[]{0, 1, 0})
            .put(BlockFace.DOWN, new int[]{0, -1, 0})
            .put(BlockFace.SOUTH, new int[]{0, 0, 1})
            .put(BlockFace.NORTH, new int[]{0, 0, -1})
            .build();

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());
        int currentTeam = team.map(T::getId).orElse(0);
        for (Block block : event.getBlocks()) {
            int[] offset = offsets.get(event.getDirection());
            Optional<T> newTeam = keviinTeams.getTeamManager().getTeamViaLocation(block.getLocation().add(offset[0], offset[1], offset[2]), team);
            if (newTeam.map(T::getId).orElse(0) != currentTeam) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());
        int currentTeam = team.map(T::getId).orElse(0);
        for (Block block : event.getBlocks()) {
            Optional<T> newTeam = keviinTeams.getTeamManager().getTeamViaLocation(block.getLocation(), team);
            if (newTeam.map(T::getId).orElse(0) != currentTeam) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
