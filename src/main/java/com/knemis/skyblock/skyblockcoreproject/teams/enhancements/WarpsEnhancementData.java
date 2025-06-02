package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class WarpsEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public int warps;

    public WarpsEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, int warps) {
        super(minLevel, money, bankCosts);
        this.warps = warps;
    }

    @Override
    public List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders() { // TODO: Replace Placeholder
        return Arrays.asList(
                new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("warps", String.valueOf(warps)) // TODO: Replace Placeholder
        );
    }
}
