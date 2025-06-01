package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // Assuming this can get island at location
import com.knemis.skyblock.skyblockcoreproject.utils.RandomAccessList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockFormListener implements Listener {

    private final SkyBlockProject plugin;
    // Stubbed: Enhancement system for generator ores is not yet integrated.
    // These maps would be populated from a config or an enhancement system.
    private final Map<Integer, RandomAccessList<XMaterial>> normalOreLevels = new HashMap<>();
    private final Map<Integer, RandomAccessList<XMaterial>> netherOreLevels = new HashMap<>();

    private final List<XMaterial> generatorMaterials = Arrays.asList(XMaterial.STONE, XMaterial.COBBLESTONE, XMaterial.BASALT);

    public BlockFormListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        // Initialize with default behavior (e.g., always cobblestone or stone)
        // as enhancement system is not present from the original Skyblock system.
        // Level 0 could represent default.
        Map<XMaterial, Integer> defaultOres = new HashMap<>();
        defaultOres.put(XMaterial.COBBLESTONE, 1);
        normalOreLevels.put(0, new RandomAccessList<>(defaultOres));

        Map<XMaterial, Integer> defaultNetherOres = new HashMap<>();
        // Basalt generators typically form Basalt or Blackstone in vanilla-like mechanics.
        // Netherrack is less common unless it's a specific "nether" generator.
        // Using BASALT as a default for basalt generators seems more intuitive.
        defaultNetherOres.put(XMaterial.BASALT, 1);
        netherOreLevels.put(0, new RandomAccessList<>(defaultNetherOres));

        // TODO: Load actual ore generation chances from SkyBlockProject's config if available,
        // or implement a similar enhancement system.
        // For example:
        // Map<XMaterial, Integer> level1Ores = new HashMap<>();
        // level1Ores.put(XMaterial.COAL_ORE, plugin.getConfig().getInt("generator.level1.coal_chance", 10));
        // level1Ores.put(XMaterial.COBBLESTONE, plugin.getConfig().getInt("generator.level1.cobble_chance", 90));
        // normalOreLevels.put(1, new RandomAccessList<>(level1Ores));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        BlockState newState = event.getNewState();
        XMaterial newMaterial = XMaterial.matchXMaterial(newState.getType());

        if (!generatorMaterials.contains(newMaterial)) return;

        // Adapt to get Island from SkyBlockProject's system
        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        if (islandDataHandler == null) {
             // plugin.getLogger().warning("[BlockFormListener] IslandDataHandler is null. Cannot process generator event.");
            return;
        }
        Island island = islandDataHandler.getIslandAt(newState.getLocation());

        if (island == null) return; // Not on an island or island system not found

        // Stubbed: Get upgrade level. Default to 0 if enhancement system not present.
        int upgradeLevel = 0; // TODO: Replace with actual island.getEnhancementLevel("generator") or similar

        boolean isBasaltGenerator = newMaterial == XMaterial.BASALT;
        // Determine if we should use nether or normal ores based on the block formed (e.g. basalt vs stone/cobble)
        // AND potentially the world environment if netherOnlyBasalt is true for basalt generators.

        Map<Integer, RandomAccessList<XMaterial>> relevantOreMap = normalOreLevels;
        if (isBasaltGenerator) {
            relevantOreMap = netherOreLevels;
        } else if (newState.getWorld().getEnvironment() == World.Environment.NETHER) {
            // If a normal generator (cobble/stone) is in the Nether, potentially use nether ores
            // This depends on desired game mechanics. The original code snippet didn't explicitly show
            // this distinction for normal generators in nether, only for basalt ones with netherOnlyGenerator.
            // For now, assume normal generators use normalOreLevels regardless of dimension,
            // unless a specific config/enhancement dictates otherwise.
            // If nether-specific ores for normal generators are desired, this logic needs expansion.
        }

        RandomAccessList<XMaterial> randomMaterialList = relevantOreMap.get(upgradeLevel);

        if (randomMaterialList == null) { // Fallback to default (level 0) if specific level not defined
            randomMaterialList = relevantOreMap.get(0);
        }
        if (randomMaterialList == null || randomMaterialList.nextElement().equals(Optional.empty())) {
            // This means level 0 for this generator type is not defined or is empty, which is a config error.
            // plugin.getLogger().warning("[BlockFormListener] No generator materials defined for level 0 for type: " + (isBasaltGenerator ? "BASALT/NETHER" : "NORMAL"));
            return;
        }

        Optional<XMaterial> xMaterialOptional = randomMaterialList.nextElement(); // Get a new random element
        if (!xMaterialOptional.isPresent()) return;

        Material resultingMaterial = xMaterialOptional.get().parseMaterial();
        if (resultingMaterial == null) return;

        // Config for nether-only basalt generation (original had netherOnlyGenerator)
        boolean netherOnlyBasaltConfig = plugin.getConfig().getBoolean("generator.nether-only-basalt", false);
        if (isBasaltGenerator && netherOnlyBasaltConfig && newState.getWorld().getEnvironment() != World.Environment.NETHER) {
            // If it's a basalt generator, and config says basalt only in nether, but we are not in nether,
            // then what should it form? Fallback to stone? Or cancel?
            // Original code seems to just 'return', meaning no change from original BASALT.
            // This might be desired, or you might want to force it to STONE or COBBLESTONE.
            // For now, let's keep it simple: if it's basalt, nether-only, and not in nether, let it form basalt.
            // The check above was `if (isBasaltGenerator && netherOnlyBasalt && ... ) return;`
            // This needs to be carefully considered. If `netherOnlyBasalt` means "only generate nether ores in nether",
            // then the current logic of selecting `netherOreLevels` for basalt generators is fine.
            // If it means "basalt generators *only work* in the nether", then we should prevent custom ore generation outside nether.
            // Let's assume the latter meaning for this check:
            if (newState.getWorld().getEnvironment() != World.Environment.NETHER) {
                 // It's a basalt generator outside the Nether, and config restricts nether ores to Nether.
                 // Fallback to a default overworld block like stone or allow it to form basalt as per original event.
                 // For now, let's allow it to form its natural state (Basalt) if this condition is met.
                 // So, if resultingMaterial was from netherOreLevels, but we are not in nether, what to do?
                 // The original code's `if (isBasaltGenerator && netherOnlyGenerator && ... ) return;` implies it would just form basalt.
                 // The current code structure will try to set it to something from `netherOreLevels`.
                 // Let's adjust: if it's a basalt generator, config restricts to nether, and we're not in nether, set to STONE.
                newState.setType(Material.STONE); // Fallback for basalt generator outside nether if restricted
                return; // And don't use the list.
            }
        }


        // Original logic: if (material == Material.COBBLESTONE && newMaterial == XMaterial.STONE) material = Material.STONE;
        // This means if cobble is chosen by RAL but the formed block was stone, keep it stone.
        if (resultingMaterial == Material.COBBLESTONE && newMaterial == XMaterial.STONE) {
            // This ensures that if a stone block forms (e.g. water + lava) and the generator
            // rolls cobblestone, it still becomes stone.
            newState.setType(Material.STONE);
        } else {
            newState.setType(resultingMaterial);
        }
    }
}
