package com.keviin.keviinteams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
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
