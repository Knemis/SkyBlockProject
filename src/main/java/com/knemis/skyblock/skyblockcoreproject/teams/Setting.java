package com.knemis.skyblock.skyblockcoreproject.teams;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Setting {
    private com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
    private String displayName;
    private String defaultValue;
    public boolean enabled;
    @JsonIgnore
    private List<String> values;

    public Setting(com.knemis.skyblock.skyblockcoreproject.teams.Item item, String displayName, String defaultValue) { // TODO: Replace with actual Item class
        this.item = item;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
        this.enabled = true;
        this.values = new ArrayList<>();
    }
}
