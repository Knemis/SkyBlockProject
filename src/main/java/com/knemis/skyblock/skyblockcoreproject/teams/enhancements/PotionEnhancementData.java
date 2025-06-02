package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import com.cryptomorin.xseries.XPotion;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class PotionEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public int strength;
    public XPotion potion;
    public List<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType> enhancementAffectsType; // TODO: Ensure EnhancementAffectsType is correctly referenced

    public PotionEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, List<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType> enhancementAffectsType, int strength, XPotion potion) { // TODO: Ensure EnhancementAffectsType is correctly referenced
        super(minLevel, money, bankCosts);
        this.strength = strength;
        this.potion = potion;
        this.enhancementAffectsType = enhancementAffectsType;
    }
}
