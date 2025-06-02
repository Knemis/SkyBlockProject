package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

// import com.keviin.keviincore.Background; // TODO: Replace with actual Background class
// import com.keviin.keviincore.Item; // TODO: Replace with actual Item class
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MissionTypeSelectorInventoryConfig extends com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI { // TODO: Ensure NoItemGUI is correctly referenced or imported
    public MissionTypeItem daily;
    public MissionTypeItem weekly;
    public MissionTypeItem infinite;
    public MissionTypeItem once;


    public MissionTypeSelectorInventoryConfig(int size, String title, com.knemis.skyblock.skyblockcoreproject.teams.Background background, MissionTypeItem daily, MissionTypeItem weekly, MissionTypeItem infinite, MissionTypeItem once) { // TODO: Replace with actual Background class
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
        public com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
        public boolean enabled;
    }
}
