package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class MembersEnhancementData extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData { // TODO: Ensure EnhancementData is correctly referenced
    public int members;

    public MembersEnhancementData(int minLevel, int money, Map<String, Double> bankCosts, int members) {
        super(minLevel, money, bankCosts);
        this.members = members;
    }

    @Override
    public List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders() { // TODO: Replace Placeholder
        return Arrays.asList(
                new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("members", String.valueOf(members)) // TODO: Replace Placeholder
        );
    }
}
