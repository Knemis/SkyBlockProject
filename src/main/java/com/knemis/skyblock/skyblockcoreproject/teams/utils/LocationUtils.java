package com.knemis.skyblock.skyblockcoreproject.teams.utils;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.secondcore.SkyBlockProjectSecondCore;
import com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.common.multiversion.MultiVersion;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
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

    public static boolean isSafe(@NotNull Location location, SkyBlockProjectTeams<?, ?> SkyBlockProjectTeams) {
        if (SkyBlockProjectSecondCore.isTesting()) {
            boolean safe = isSafeTesting;
            isSafeTesting = true;
            return safe;
        }
        Block block = location.getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();
        Block below = location.clone().subtract(0, 1, 0).getBlock();
        MultiVersion multiVersion = SkyBlockProjectTeams.getMultiVersion();
        return multiVersion.isPassable(block) && multiVersion.isPassable(above) && !multiVersion.isPassable(below) && !unsafeBlocks.contains(below.getType()) && !unsafeBlocks.contains(block.getType()) && !unsafeBlocks.contains(above.getType());
    }

    public static int getMinHeight(World world) {
        return XMaterial.getVersion() >= 17 ? world.getMinHeight() : 0;
    }
}
