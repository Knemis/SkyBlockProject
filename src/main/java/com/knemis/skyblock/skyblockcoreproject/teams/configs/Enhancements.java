package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.common.collect.ImmutableMap;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.*; // Will be replaced by specific imports below
// Specific imports after refactoring (commented out for now as enhancements package is not yet moved)
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Enhancements {

    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData> farmingEnhancement; // TODO: Update to actual classes
    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData> spawnerEnhancement; // TODO: Update to actual classes
    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData> experienceEnhancement; // TODO: Update to actual classes
    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData> flightEnhancement; // TODO: Update to actual classes
    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData> membersEnhancement; // TODO: Update to actual classes

    public com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData> warpsEnhancement; // TODO: Update to actual classes
    public Map<String, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData>> potionEnhancements; // TODO: Update to actual classes

    public Enhancements() {
        this("&c");
    }

    public Enhancements(String color) {
        farmingEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.BOOSTER, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.WHEAT, 10, 1, color + "&lFarming Booster", Arrays.asList( // TODO: Replace with actual Item class
                "&7Increase the speed at which crops grow.",
                "",
                color + "&lInformation:",
                color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours, %timeremaining_minutes% minutes and %timeremaining_seconds% seconds",
                color + "&l * &7Current Level: %current_level%",
                color + "&l * &7Booster Cost: $%vault_cost%",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData>() // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData(5, 10000, new HashMap<>(), 1)) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData(10, 10000, new HashMap<>(), 2)) // TODO: Update to actual class
                .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FarmingEnhancementData(15, 10000, new HashMap<>(), 3)) // TODO: Update to actual class
                .build());

        spawnerEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.BOOSTER, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SPAWNER, 12, 1, color + "&lSpawner Booster", Arrays.asList( // TODO: Replace with actual Item class
                "&7Increase your spawner speeds.",
                "",
                color + "&lInformation:",
                color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours, %timeremaining_minutes% minutes and %timeremaining_seconds% seconds",
                color + "&l * &7Current Level: %current_level%",
                color + "&l * &7Booster Cost: $%vault_cost%",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData>() // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData(5, 10000, new HashMap<>(), 6, 0)) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData(10, 10000, new HashMap<>(), 8, 0)) // TODO: Update to actual class
                .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.SpawnerEnhancementData(15, 10000, new HashMap<>(), 10, 0)) // TODO: Update to actual class
                .build());

        experienceEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.BOOSTER, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.EXPERIENCE_BOTTLE, 14, 1, color + "&lExperience Booster", Arrays.asList( // TODO: Replace with actual Item class
                "&7Increase how much experience you get.",
                "",
                color + "&lInformation:",
                color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours, %timeremaining_minutes% minutes and %timeremaining_seconds% seconds",
                color + "&l * &7Current Level: %current_level%",
                color + "&l * &7Booster Cost: $%vault_cost%",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData>() // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData(5, 10000, new HashMap<>(), 1.5)) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData(10, 10000, new HashMap<>(), 2)) // TODO: Update to actual class
                .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData(15, 10000, new HashMap<>(), 2.5)) // TODO: Update to actual class
                .build());

        flightEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.BOOSTER, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.FEATHER, 16, 1, color + "&lFlight Booster", Arrays.asList( // TODO: Replace with actual Item class
                "&7Gain access to fly.",
                "",
                color + "&lInformation:",
                color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours, %timeremaining_minutes% minutes and %timeremaining_seconds% seconds",
                color + "&l * &7Current Level: %current_level%",
                color + "&l * &7Booster Cost: $%vault_cost%",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData>() // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData(5, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.MEMBERS_IN_TERRITORY))) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData(10, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.MEMBERS_ANYWHERE))) // TODO: Update to actual class
                .build());

        membersEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.PLAYER_HEAD, 11, "Peaches_MLG", 1, color + "&lMember Upgrade", Arrays.asList( // TODO: Replace with actual Item class
                "&7Need more members? Buy this",
                "&7upgrade to increase your member count.",
                "",
                color + "&lInformation:",
                color + "&l * &7Current Level: " + color + "%current_level%",
                color + "&l * &7Current Members: " + color + "%members% Members",
                color + "&l * &7Upgrade Cost: " + color + "%vault_cost%",
                color + "&lLevels:",
                color + "&l * &7Level 1: " + color + "5 Members",
                color + "&l * &7Level 2: " + color + "10 Members",
                color + "&l * &7Level 3: " + color + "15 Members",
                color + "&l * &7Level 4: " + color + "20 Members",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData>() // TODO: Update to actual class
                .put(0, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData(5, 10000, new HashMap<>(), 5)) // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData(5, 10000, new HashMap<>(), 10)) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData(10, 10000, new HashMap<>(), 15)) // TODO: Update to actual class
                .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData(15, 10000, new HashMap<>(), 20)) // TODO: Update to actual class
                .build());

        warpsEnhancement = new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>(true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.END_PORTAL_FRAME, 13, 1, color + "&lWarps Upgrade", Arrays.asList( // TODO: Replace with actual Item class
                "&7Need more members? Buy this",
                "&7upgrade to increase your member count.",
                "",
                color + "&lInformation:",
                color + "&l * &7Current Level: " + color + "%current_level%",
                color + "&l * &7Current Warps: " + color + "%warps% Warps",
                color + "&l * &7Upgrade Cost: " + color + "%vault_cost%",
                color + "&lLevels:",
                color + "&l * &7Level 1: " + color + "1 Warps",
                color + "&l * &7Level 2: " + color + "3 Warps",
                color + "&l * &7Level 3: " + color + "5 Warps",
                color + "&l * &7Level 4: " + color + "7 Warps",
                color + "&l * &7Level 5: " + color + "9 Warps",
                "",
                color + "[!] &7Must be level %minLevel% to purchase",
                color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
        )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData>() // TODO: Update to actual class
                .put(0, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData(5, 10000, new HashMap<>(), 1)) // TODO: Update to actual class
                .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData(5, 10000, new HashMap<>(), 3)) // TODO: Update to actual class
                .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData(10, 10000, new HashMap<>(), 5)) // TODO: Update to actual class
                .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData(15, 10000, new HashMap<>(), 7)) // TODO: Update to actual class
                .put(4, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.WarpsEnhancementData(15, 10000, new HashMap<>(), 9)) // TODO: Update to actual class
                .build());

        potionEnhancements = new ImmutableMap.Builder<String, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData>>() // TODO: Update to actual classes
                .put("haste", new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>( // TODO: Update to actual class
                        true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.DIAMOND_PICKAXE, 10, 1, color + "&lHaste Booster", Arrays.asList( // TODO: Replace with actual Item class
                        "&7Gain a Haste Potion Effect.",
                        "",
                        color + "&lInformation:",
                        color + "&l * &7Current Level: %current_level%",
                        color + "&l * &7Upgrade Cost: $%vault_cost%",
                        "",
                        color + "[!] &7Must be level %minLevel% to purchase",
                        color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
                )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData>() // TODO: Update to actual class
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(5, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 1, XPotion.HASTE)) // TODO: Update to actual class
                        .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(10, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 2, XPotion.HASTE)) // TODO: Update to actual class
                        .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(15, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 3, XPotion.HASTE)) // TODO: Update to actual class
                        .build()
                ))
                .put("speed", new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>( // TODO: Update to actual class
                        true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SUGAR, 12, 1, color + "&lSpeed Booster", Arrays.asList( // TODO: Replace with actual Item class
                        "&7Gain a Speed Potion Effect.",
                        "",
                        color + "&lInformation:",
                        color + "&l * &7Current Level: %current_level%",
                        color + "&l * &7Upgrade Cost: $%vault_cost%",
                        "",
                        color + "[!] &7Must be level %minLevel% to purchase",
                        color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
                )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData>() // TODO: Update to actual class
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(5, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 1, XPotion.SPEED)) // TODO: Update to actual class
                        .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(10, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 2, XPotion.SPEED)) // TODO: Update to actual class
                        .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(15, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 3, XPotion.SPEED)) // TODO: Update to actual class
                        .build()
                ))
                .put("jump", new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<>( // TODO: Update to actual class
                        true, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementType.UPGRADE, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.FEATHER, 14, 1, color + "&lJump Booster", Arrays.asList( // TODO: Replace with actual Item class
                        "&7Gain a Jump Boost Potion Effect.",
                        "",
                        color + "&lInformation:",
                        color + "&l * &7Current Level: %current_level%",
                        color + "&l * &7Upgrade Cost: $%vault_cost%",
                        "",
                        color + "[!] &7Must be level %minLevel% to purchase",
                        color + "&l[!] " + color + "Left Click to Purchase Level %next_level%."
                )), new ImmutableMap.Builder<Integer, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData>() // TODO: Update to actual class
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(5, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 1, XPotion.JUMP_BOOST)) // TODO: Update to actual class
                        .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(10, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 2, XPotion.JUMP_BOOST)) // TODO: Update to actual class
                        .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData(15, 10000, new HashMap<>(), Collections.singletonList(com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType.VISITORS), 3, XPotion.JUMP_BOOST)) // TODO: Update to actual class
                        .build()
                ))
                .build();
    }
}
