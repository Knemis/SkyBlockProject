package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keviin.keviincore.utils.Placeholder;
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
    public List<Placeholder> getPlaceholders() {
        return Collections.emptyList();
    }
}
