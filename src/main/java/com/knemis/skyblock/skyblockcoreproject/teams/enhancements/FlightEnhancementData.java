package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class FlightEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced

    public List<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType> enhancementAffectsType; // TODO: Ensure EnhancementAffectsType is correctly referenced

    public FlightEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, List<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType> enhancementAffectsType) { // TODO: Ensure EnhancementAffectsType is correctly referenced
        super(minLevel, money, bankCosts);
        this.enhancementAffectsType = enhancementAffectsType;
    }
}
