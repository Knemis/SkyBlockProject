package com.knemis.skyblock.skyblockcoreproject.teams.enhancements;

// import com.keviin.keviincore.Item; // TODO: Replace with actual Item class
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class Enhancement<T extends com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData> { // TODO: Ensure EnhancementData is correctly referenced
    public boolean enabled;
    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType type; // TODO: Ensure EnhancementType is correctly referenced
    public com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
    public Map<Integer, T> levels;

}