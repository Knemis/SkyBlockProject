package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Background;
import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfirmationInventoryConfig extends NoItemGUI {
    /**
     * The yes item
     */
    public Item yes;
    /**
     * the no item
     */
    public Item no;

    public ConfirmationInventoryConfig(int size, String title, Background background, Item yes, Item no) {
        this.size = size;
        this.title = title;
        this.background = background;
        this.yes = yes;
        this.no = no;
    }
}
