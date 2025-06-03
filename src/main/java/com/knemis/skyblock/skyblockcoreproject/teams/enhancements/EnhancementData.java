package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class EnhancementData {
    public int minLevel;
    public int money;
    public Map<String, Double> bankCosts;

    @JsonIgnore
    public List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders() { // TODO: Replace Placeholder
        return Collections.emptyList();
    }
}
