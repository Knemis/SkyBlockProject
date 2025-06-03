package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.Reward;
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission; // Will be replaced by specific import below
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData; // Will be replaced by specific import below
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType; // Will be replaced by specific import below
// Specific imports after refactoring (commented out for now as missions package is not yet moved)
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission;
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData;
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Missions {
    public Map<String, com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission> missions; // TODO: Update to actual Mission class

    public List<Integer> dailySlots = Arrays.asList(10, 12, 14, 16);
    public Map<String, List<String>> customMaterialLists = ImmutableMap.<String, List<String>>builder()
            .put("LOGS", Arrays.asList(
                    "OAK_LOG",
                    "BIRCH_LOG",
                    "SPRUCE_LOG",
                    "DARK_OAK_LOG",
                    "ACACIA_LOG",
                    "JUNGLE_LOG",
                    "CRIMSON_STEM",
                    "WARPED_STEM"
            ))
            .build();

    public Missions() {
        this("&c");
    }

    public Missions(String color) {
        missions = ImmutableMap.<String, com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission>builder() // TODO: Update to actual Mission class

                .put("farmer", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SUGAR_CANE, 1, color + "&lFarmer", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Grow 10 Sugarcane: %progress_1%/10",
                                        color + "&l* &7Grow 10 Wheat: %progress_2%/10",
                                        color + "&l* &7Grow 10 Carrots: %progress_3%/10",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Arrays.asList("GROW:SUGAR_CANE:10", "GROW:WHEAT:10", "GROW:CARROTS:10"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lFarmer Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Farmer mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("hunter", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.BONE, 1, color + "&lHunter", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Kill 10 Zombies: %progress_1%/10",
                                        color + "&l* &7Kill 10 Skeletons: %progress_2%/10",
                                        color + "&l* &7Kill 10 Creepers: %progress_3%/10",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Arrays.asList("KILL:ZOMBIE:10", "KILL:SKELETON:10", "KILL:CREEPER:10"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lHunter Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Hunter mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("baker", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.BREAD, 1, color + "&lBaker", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Bake 64 Bread: %progress_1%/64",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Collections.singletonList("CRAFT:BREAD:64"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lBaker Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Baker mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("miner", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.GOLD_ORE, 1, color + "&lMiner", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Mine 15 Iron Ores: %progress_1%/15",
                                        color + "&l* &7Mine 30 Coal Ores: %progress_2%/30",
                                        color + "&l* &7Mine 1 Diamond Ore: %progress_3%/1",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Arrays.asList("MINE:IRON_ORE:15", "MINE:COAL_ORE:30", "MINE:DIAMOND_ORE:1"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lMiner Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Miner mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("fisherman", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.FISHING_ROD, 1, color + "&lFisherman", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Catch 10 Fish: %progress_1%/10",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Collections.singletonList("FISH:ANY:10"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lFisherman Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Fisherman mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("blacksmith", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.IRON_INGOT, 1, color + "&lBlacksmith", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Smelt 30 Iron Ores: %progress_1%/30",
                                        color + "&l* &7Smelt 15 Gold Ores: %progress_2%/15",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )), Arrays.asList("SMELT:" + (XMaterial.supports(17) ? XMaterial.RAW_IRON.name() : XMaterial.IRON_ORE.name()) + ":30", "SMELT:" + (XMaterial.supports(17) ? XMaterial.RAW_GOLD.name() : XMaterial.GOLD_ORE.name()) + ":15"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lBlacksmith Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Blacksmith mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("brewer", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.POTION, 1, color + "&lPotion Brewer", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Brew 3 Speed II Potions: %progress_1%/3",
                                        color + "&l* &7Brew 3 Strength II Potions: %progress_2%/3",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000",
                                        "",
                                        color + "&l * &7Time Remaining: " + color + "%timeremaining_hours% hours %timeremaining_minutes% minutes and %timeremaining_seconds% seconds"
                                )
                        ), Arrays.asList("BREW:SPEED:2:3", "BREW:STRENGTH:2:3"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lPotionBrewer Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Potion Brewer mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        )).build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY)) // TODO: Update to actual MissionType class

                .put("mine_oak", new com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData>builder() // TODO: Update to actual Mission and MissionData classes
                        .put(1, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.OAK_LOG, 0, 1, color + "&lMine 10 Logs", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Mine 10 logs: %progress_1%/10",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )
                        ), Collections.singletonList("MINE:LOGS:10"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lPotionBrewer Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        ))
                        .put(2, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.OAK_LOG, 0, 1, color + "&lMine 100 Logs", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Mine 100 logs: %progress_1%/100",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )
                        ), Collections.singletonList("MINE:LOGS:100"), 1,  new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lPotionBrewer Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        ))
                        .put(3, new com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.OAK_LOG, 0, 1, color + "&lMine 1000 Logs", // TODO: Replace with actual Item class
                                Arrays.asList(
                                        "&7Complete Island Missions to gain rewards",
                                        "&7Which can be used to purchase Island Upgrades",
                                        "",
                                        color + "[!] &7Must be level 1 to complete this mission",
                                        "",
                                        color + "&lObjectives:",
                                        color + "&l* &7Mine 1000 logs: %progress_1%/1000",
                                        "",
                                        color + "&lRewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )
                        ), Collections.singletonList("MINE:LOGS:1000"), 1, new Reward(new Item(XMaterial.DIAMOND, 1, color + "&lPotionBrewer Reward",
                                Arrays.asList(
                                        color + "&l Rewards",
                                        color + "&l* &75 Island Crystals",
                                        color + "&l* &7$1000"
                                )), Collections.emptyList(), 1000, new HashMap<>(), 0, 10, XSound.ENTITY_PLAYER_LEVELUP),
                                "%prefix% &7Mission Completed!\n" +
                                        color + "&l* &7+3 Island Experience\n" +
                                        color + "&l* &7+5 Island Crystals\n" +
                                        color + "&l* &7+1000 Money\n" +
                                        "&7/is rewards to redeem rewards"
                        ))
                        .build(), com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.ONCE)) // TODO: Update to actual MissionType class
                .build();
    }

}
