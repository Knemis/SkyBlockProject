package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.sorting.ExperienceTeamSort; // TODO: Update to actual ExperienceTeamSort class
// import com.knemis.skyblock.skyblockcoreproject.teams.sorting.ValueTeamSort; // TODO: Update to actual ValueTeamSort class

import java.util.Collections;

public class Top<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team> { // TODO: Update Team to actual class
    public com.knemis.skyblock.skyblockcoreproject.teams.sorting.ValueTeamSort<T> valueTeamSort = new com.knemis.skyblock.skyblockcoreproject.teams.sorting.ValueTeamSort<>(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.DIAMOND, 18, 1, "&9&lSort By Value", Collections.emptyList())); // TODO: Replace with actual ValueTeamSort and Item classes
    public com.knemis.skyblock.skyblockcoreproject.teams.sorting.ExperienceTeamSort<T> experienceTeamSort = new com.knemis.skyblock.skyblockcoreproject.teams.sorting.ExperienceTeamSort<>(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.EXPERIENCE_BOTTLE, 27, 1, "&e&lSort By Experience", Collections.emptyList())); // TODO: Replace with actual ExperienceTeamSort and Item classes
}
