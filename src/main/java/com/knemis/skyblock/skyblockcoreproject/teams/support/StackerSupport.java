package com.knemis.skyblock.skyblockcoreproject.teams.support;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Map;

public interface StackerSupport<T extends Team> {
    int getExtraBlocks(T team, XMaterial material, List<Block> blocks);
    Map<XMaterial, Integer> getBlocksStacked(Chunk chunk, T team);
    boolean isStackedBlock(Block block);
    int getStackAmount(Block block);
    String supportProvider();
}
