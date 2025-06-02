package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class SpawnerEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public int spawnCount;
    public int spawnMultiplier;

    public SpawnerEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, int spawnCount, int spawnMultiplier) {
        super(minLevel, money, bankCosts);
        this.spawnCount = spawnCount;
        this.spawnMultiplier = spawnMultiplier;
    }
}
