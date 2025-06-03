package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Background; // TODO: Replace with actual Background class
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class InventoryConfig extends com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI { // TODO: Ensure NoItemGUI is correctly referenced or imported
    /**
     * A HashMap of the items in the GUI with the string representing the command to be executed
     */
    public Map<String, com.knemis.skyblock.skyblockcoreproject.teams.Item> items; // TODO: Replace with actual Item class

    public InventoryConfig(int size, String title, com.knemis.skyblock.skyblockcoreproject.teams.Background background, Map<String, com.knemis.skyblock.skyblockcoreproject.teams.Item> items) { // TODO: Replace with actual Background and Item classes
        this.size = size;
        this.title = title;
        this.background = background;
        this.items = items;
    }
}
