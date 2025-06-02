package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class FarmingEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public int farmingModifier;

    public FarmingEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, int farmingModifier) {
        super(minLevel, money, bankCosts);
        this.farmingModifier = farmingModifier;
    }
}
