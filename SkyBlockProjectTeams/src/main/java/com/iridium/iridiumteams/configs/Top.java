package com.keviin.keviinteams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviincore.Item;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.sorting.ExperienceTeamSort;
import com.keviin.keviinteams.sorting.ValueTeamSort;

import java.util.Collections;

public class Top<T extends Team> {
    public ValueTeamSort<T> valueTeamSort = new ValueTeamSort<>(new Item(XMaterial.DIAMOND, 18, 1, "&9&lSort By Value", Collections.emptyList()));
    public ExperienceTeamSort<T> experienceTeamSort = new ExperienceTeamSort<>(new Item(XMaterial.EXPERIENCE_BOTTLE, 27, 1, "&e&lSort By Experience", Collections.emptyList()));
}
