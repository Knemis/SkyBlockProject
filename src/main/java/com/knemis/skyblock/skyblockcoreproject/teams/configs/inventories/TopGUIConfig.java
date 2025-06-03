package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Background; // TODO: Replace with actual Background class
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TopGUIConfig extends com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.SingleItemGUI { // TODO: Ensure SingleItemGUI is correctly referenced or imported
    public com.knemis.skyblock.skyblockcoreproject.teams.Item filler; // TODO: Replace with actual Item class

    public TopGUIConfig(int size, String title, com.knemis.skyblock.skyblockcoreproject.teams.Background background, com.knemis.skyblock.skyblockcoreproject.teams.Item item, com.knemis.skyblock.skyblockcoreproject.teams.Item filter) { // TODO: Replace with actual Background and Item classes
        super(size, title, background, item);
        this.filler = filter;
    }
}
