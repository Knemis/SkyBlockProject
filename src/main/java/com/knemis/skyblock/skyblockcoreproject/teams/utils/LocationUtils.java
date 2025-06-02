package com.knemis.skyblock.skyblockcoreproject.teams.utils;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviincore.KeviinCore;
import com.keviin.keviincore.multiversion.MultiVersion;
import com.keviin.keviinteams.keviinTeams;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocationUtils {
    @Setter
    private static boolean isSafeTesting = true;

    private static final List<Material> unsafeBlocks = Stream.of(
            XMaterial.END_PORTAL,
            XMaterial.WATER,
            XMaterial.LAVA
    ).map(XMaterial::parseMaterial).collect(Collectors.toList());

    public static boolean isSafe(@NotNull Location location, keviinTeams<?, ?> keviinTeams) {
        if (KeviinCore.isTesting()) {
            boolean safe = isSafeTesting;
            isSafeTesting = true;
            return safe;
        }
        Block block = location.getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();
        Block below = location.clone().subtract(0, 1, 0).getBlock();
        MultiVersion multiVersion = keviinTeams.getMultiVersion();
        return multiVersion.isPassable(block) && multiVersion.isPassable(above) && !multiVersion.isPassable(below) && !unsafeBlocks.contains(below.getType()) && !unsafeBlocks.contains(block.getType()) && !unsafeBlocks.contains(above.getType());
    }

    public static int getMinHeight(World world) {
        return XMaterial.getVersion() >= 17 ? world.getMinHeight() : 0;
    }
}
