package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Background; // TODO: Replace with actual Background class
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SingleItemGUI extends com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI { // TODO: Ensure NoItemGUI is correctly referenced or imported
    /**
     * The item for the GUI
     */
    public com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class

    public SingleItemGUI(int size, String title, com.knemis.skyblock.skyblockcoreproject.teams.Background background, com.knemis.skyblock.skyblockcoreproject.teams.Item item) { // TODO: Replace with actual Background and Item classes
        this.size = size;
        this.title = title;
        this.background = background;
        this.item = item;
    }
}
