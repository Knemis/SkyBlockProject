package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class ExperienceEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public double experienceModifier;

    public ExperienceEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, double experienceModifier) {
        super(minLevel, money, bankCosts);
        this.experienceModifier = experienceModifier;
    }
}
