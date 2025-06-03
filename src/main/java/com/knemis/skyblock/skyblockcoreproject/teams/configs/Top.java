package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.sorting.ExperienceTeamSort;
import com.knemis.skyblock.skyblockcoreproject.teams.sorting.ValueTeamSort;

import java.util.Collections;

public class Top<T extends Team> {
    public ValueTeamSort<T> valueTeamSort = new ValueTeamSort<>(new Item(XMaterial.DIAMOND, 18, 1, "&9&lSort By Value", Collections.emptyList()));
    public ExperienceTeamSort<T> experienceTeamSort = new ExperienceTeamSort<>(new Item(XMaterial.EXPERIENCE_BOTTLE, 27, 1, "&e&lSort By Experience", Collections.emptyList()));
}
