package com.keviin.keviinteams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class InventoryConfig extends NoItemGUI {
    /**
     * A HashMap of the items in the GUI with the string representing the command to be executed
     */
    public Map<String, Item> items;

    public InventoryConfig(int size, String title, Background background, Map<String, Item> items) {
        this.size = size;
        this.title = title;
        this.background = background;
        this.items = items;
    }
}
