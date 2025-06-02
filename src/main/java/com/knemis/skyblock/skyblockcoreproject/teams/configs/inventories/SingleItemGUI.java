package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Background;
import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SingleItemGUI extends NoItemGUI {
    /**
     * The item for the GUI
     */
    public Item item;

    public SingleItemGUI(int size, String title, Background background, Item item) {
        this.size = size;
        this.title = title;
        this.background = background;
        this.item = item;
    }
}
