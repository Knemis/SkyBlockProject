package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MissionTypeSelectorInventoryConfig extends NoItemGUI {
    public MissionTypeItem daily;
    public MissionTypeItem weekly;
    public MissionTypeItem infinite;
    public MissionTypeItem once;


    public MissionTypeSelectorInventoryConfig(int size, String title, Background background, MissionTypeItem daily, MissionTypeItem weekly, MissionTypeItem infinite, MissionTypeItem once) {
        this.size = size;
        this.title = title;
        this.background = background;
        this.daily = daily;
        this.weekly = weekly;
        this.infinite = infinite;
        this.once = once;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class MissionTypeItem {
        public Item item;
        public boolean enabled;
    }
}
