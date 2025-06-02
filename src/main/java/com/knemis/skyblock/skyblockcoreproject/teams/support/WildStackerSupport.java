package com.knemis.skyblock.skyblockcoreproject.teams.support;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class WildStackerSupport<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements StackerSupport<T>, SpawnerSupport<T> {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public WildStackerSupport(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @Override
    public String supportProvider() {
        return "WildStacker";
    }

    @Override
    public boolean isStackedBlock(Block block) {
        return WildStackerAPI.getWildStacker().getSystemManager().isStackedBarrel(block);
    }

    @Override
    public boolean isStackedSpawner(Block block) {
        return WildStackerAPI.getWildStacker().getSystemManager().isStackedSpawner(block);
    }

    private StackedBarrel getStackedBlock(Block block) {
        return WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrel(block);
    }

    private StackedSpawner getStackedSpawner(Block block) {
        return WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawner(block.getLocation());
    }

    private List<StackedBarrel> getStackedBarrels(List<Block> blocks) {
        List<StackedBarrel> stackedBarrels = new ArrayList<>(Collections.emptyList());
        for (Block block : blocks) {
            stackedBarrels.add(getStackedBlock(block));
        }
        return stackedBarrels;
    }

    private List<StackedSpawner> getStackedSpawners(List<CreatureSpawner> spawners) {
        List<StackedSpawner> stackedSpawners = new ArrayList<>(Collections.emptyList());
        for (CreatureSpawner spawner : spawners) {
            stackedSpawners.add(getStackedSpawner(spawner.getBlock()));
        }
        return stackedSpawners;
    }

    @Override
    public int getStackAmount(Block block) {
        return WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrel(block).getStackAmount();
    }

    @Override
    public int getStackAmount(CreatureSpawner spawner) {
        return getStackedSpawner(spawner.getBlock()).getStackAmount();
    }

    @Override
    public int getSpawnAmount(CreatureSpawner spawner) {
        return getStackAmount(spawner) * WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawner(spawner).getSpawner().getSpawnCount();
    }

    @Override
    public Map<XMaterial, Integer> getBlocksStacked(Chunk chunk, T team) {
        HashMap<XMaterial, Integer> hashMap = new HashMap<>();

        WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrels(chunk).forEach(stackedBarrel -> {
            if (!SkyBlockProjectTeams.getTeamManager().isInTeam(team, stackedBarrel.getLocation())) return;

            XMaterial xMaterial = XMaterial.matchXMaterial(stackedBarrel.getType());
            hashMap.put(xMaterial, hashMap.getOrDefault(xMaterial, 0) + stackedBarrel.getStackAmount());
        });

        return hashMap;
    }

    @Override
    public List<CreatureSpawner> getSpawnersStacked(Chunk chunk) {
        return WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawners(chunk).stream().map(StackedSpawner::getSpawner).collect(Collectors.toList());
    }

    @Override
    public int getExtraBlocks(T team, XMaterial material, List<Block> blocks) {

        int stackedBlocks = 0;
        for (StackedBarrel stackedBarrel : getStackedBarrels(blocks)) {
            if (!SkyBlockProjectTeams.getTeamManager().isInTeam(team, stackedBarrel.getLocation())) continue;
            if (material != XMaterial.matchXMaterial(stackedBarrel.getType())) continue;
            stackedBlocks += stackedBarrel.getStackAmount();
        }

        return stackedBlocks;
    }

    @Override
    public int getExtraSpawners(T team, EntityType entityType, List<CreatureSpawner> spawners) {

        int stackedSpawners = 0;
        for (StackedSpawner stackedSpawner : getStackedSpawners(spawners)) {
            if (!SkyBlockProjectTeams.getTeamManager().isInTeam(team, stackedSpawner.getLocation())) continue;
            if (stackedSpawner.getSpawnedType() != entityType) continue;
            stackedSpawners += stackedSpawner.getStackAmount();
        }

        return stackedSpawners;
    }
}