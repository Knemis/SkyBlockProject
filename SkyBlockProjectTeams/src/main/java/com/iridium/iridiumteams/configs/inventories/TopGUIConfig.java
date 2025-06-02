package com.keviin.keviinteams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TopGUIConfig extends SingleItemGUI{
    public Item filler;

    public TopGUIConfig(int size, String title, Background background, Item item, Item filter) {
        super(size, title, background, item);
        this.filler = filter;
    }
}
