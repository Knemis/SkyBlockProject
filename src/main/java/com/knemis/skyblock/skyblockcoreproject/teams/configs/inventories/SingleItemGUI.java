package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
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
