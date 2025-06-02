package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Background;
import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TopGUIConfig extends SingleItemGUI{
    public Item filler;

    public TopGUIConfig(int size, String title, Background background, Item item, Item filter) {
        super(size, title, background, item);
        this.filler = filter;
    }
}
