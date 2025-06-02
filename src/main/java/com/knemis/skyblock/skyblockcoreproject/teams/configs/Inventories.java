package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
// import com.keviin.keviincore.Background; // TODO: Replace with actual Background class
// import com.keviin.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.*; // Will be refactored later
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType; // TODO: Update to actual MissionType class

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class Inventories {
    @JsonIgnore
    private final com.knemis.skyblock.skyblockcoreproject.teams.Background background1 = new com.knemis.skyblock.skyblockcoreproject.teams.Background(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.Item>builder().build()); // TODO: Replace with actual Background and Item classes
    @JsonIgnore
    private final com.knemis.skyblock.skyblockcoreproject.teams.Background background2 = new com.knemis.skyblock.skyblockcoreproject.teams.Background(ImmutableMap.<Integer, com.knemis.skyblock.skyblockcoreproject.teams.Item>builder().put(9, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(10, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(11, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(12, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(13, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(14, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(15, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(16, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).put(17, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, 1, " ", Collections.emptyList())).build()); // TODO: Replace with actual Background and Item classes
    public com.knemis.skyblock.skyblockcoreproject.teams.Item backButton; // TODO: Replace with actual Item class
    public SingleItemGUI rewardsGUI;
    public SingleItemGUI membersGUI;
    public SingleItemGUI invitesGUI;
    public SingleItemGUI trustsGUI;
    public NoItemGUI ranksGUI;
    public NoItemGUI permissionsGUI;
    public NoItemGUI settingsGUI;
    public NoItemGUI bankGUI;
    public Map<com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType, NoItemGUI> missionGUI; // TODO: Update to actual MissionType class
    public TopGUIConfig topGUI;
    public NoItemGUI boostersGUI;
    public NoItemGUI upgradesGUI;
    public NoItemGUI shopOverviewGUI;
    public NoItemGUI shopCategoryGUI;

    public SingleItemGUI warpsGUI;

    public MissionTypeSelectorInventoryConfig missionTypeSelectorGUI;
    public ConfirmationInventoryConfig confirmationGUI;
    public com.knemis.skyblock.skyblockcoreproject.teams.Item nextPage; // TODO: Replace with actual Item class
    public com.knemis.skyblock.skyblockcoreproject.teams.Item previousPage; // TODO: Replace with actual Item class
    public NoItemGUI blockValueGUI;
    public NoItemGUI spawnerValueGUI;
    public BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorGUI;

    public Inventories() {
        this("Team", "&c");
    }

    public Inventories(String team, String color) {
        backButton = new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.NETHER_STAR, -5, 1, "&c&lBack", Collections.emptyList()); // TODO: Replace with actual Item class
        rewardsGUI = new SingleItemGUI(54, "&7" + team + " Rewards", background1, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SUNFLOWER, 53, 1, color + "&lClaim All!", Collections.emptyList())); // TODO: Replace with actual Item class

        membersGUI = new SingleItemGUI(0, "&7" + team + " Members", background1, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.PLAYER_HEAD, 0, "%player_name%", 1, color + "&l%player_name%", Arrays.asList( // TODO: Replace with actual Item class
                color + "Joined: &7%player_join%",
                color + "Rank: &7%player_rank%",
                "",
                color + "&l[!] &7Right Click to promote",
                color + "&l[!] &7Left click to demote/kick"
        )));

        invitesGUI = new SingleItemGUI(0, "&7" + team + " Invites", background1, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.PLAYER_HEAD, 0, "%player_name%", 1, color + "&l%player_name%", Arrays.asList( // TODO: Replace with actual Item class
                color + "Invited: &7%invite_time%",
                "",
                color + "&l[!] &7Left click to uninvite"
        )));

        trustsGUI = new SingleItemGUI(0, "&7" + team + " Trusts", background1, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.PLAYER_HEAD, 0, "%player_name%", 1, color + "&l%player_name%", Arrays.asList( // TODO: Replace with actual Item class
                color + "Trusted At: &7%trusted_time%",
                color + "Trusted By: &7%truster%",
                "",
                color + "&l[!] &7Left click to un-trust"
        )));
        ranksGUI = new NoItemGUI(27, "&7" + team + " Permissions", background1);
        permissionsGUI = new NoItemGUI(54, "&7" + team + " Permissions", background1);
        settingsGUI = new NoItemGUI(36, "&7" + team + " Settings", background1);
        bankGUI = new NoItemGUI(27, "&7" + team + " Bank", background2);
        missionGUI = new ImmutableMap.Builder<com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType, NoItemGUI>() // TODO: Update to actual MissionType class
                .put(com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.DAILY, new NoItemGUI(27, "&7Daily " + team + " Missions", background2)) // TODO: Update to actual MissionType class
                .put(com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.WEEKLY, new NoItemGUI(27, "&7Weekly " + team + " Missions", background2)) // TODO: Update to actual MissionType class
                .put(com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.INFINITE, new NoItemGUI(27, "&7" + team + " Missions", background2)) // TODO: Update to actual MissionType class
                .put(com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType.ONCE, new NoItemGUI(45, "&7" + team + " Missions", background1)) // TODO: Update to actual MissionType class
                .build();
        boostersGUI = new NoItemGUI(27, "&7" + team + " Boosters", background2);
        upgradesGUI = new NoItemGUI(27, "&7" + team + " Upgrades", background2);
        shopOverviewGUI = new NoItemGUI(36, "&7Island Shop", background1);
        shopCategoryGUI = new NoItemGUI(0, "&7Island Shop | %category_name%", background1);
        warpsGUI = new SingleItemGUI(27, "&7" + team + " Warps", background2, new com.knemis.skyblock.skyblockcoreproject.teams.Item( // TODO: Replace with actual Item class
                XMaterial.GREEN_STAINED_GLASS_PANE, 1, "&b&l%warp_name%",
                Arrays.asList(
                        color + "Description: &7%warp_description%",
                        color + "Created By: &7%warp_creator%",
                        color + "Date: &7%warp_create_time%",
                        "",
                        "&b&l[!] &bLeft Click to Teleport",
                        "&b&l[!] &bRight Click to Delete"
                )));
        topGUI = new TopGUIConfig(54, "&7Top " + team, background1, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.PLAYER_HEAD, 0, "%" + team.toLowerCase() + "_owner%", 1, color + "&l" + team + " Owner: &f%" + team.toLowerCase() + "_owner%", Arrays.asList( // TODO: Replace with actual Item class
                "",
                color + team + " Name: &7%" + team.toLowerCase() + "_name%",
                color + team + " Value: &7%" + team.toLowerCase() + "_value% (#%" + team.toLowerCase() + "_value_rank%)",
                color + team + " Experience: &7%" + team.toLowerCase() + "_experience% (#%" + team.toLowerCase() + "_experience_rank%)",
                color + "Netherite Blocks: &7%" + team.toLowerCase() + "_netherite_block_amount%",
                color + "Emerald Blocks: &7%" + team.toLowerCase() + "_emerald_block_amount%",
                color + "Diamond Blocks: &7%" + team.toLowerCase() + "_diamond_block_amount%",
                color + "Gold Blocks: &7%" + team.toLowerCase() + "_gold_block_amount%",
                color + "Iron Blocks: &7%" + team.toLowerCase() + "_iron_block_amount%",
                color + "Hopper Blocks: &7%" + team.toLowerCase() + "_hopper_amount%",
                color + "Beacon Blocks: &7%" + team.toLowerCase() + "_beacon_amount%"
        )), new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.BARRIER, 1, " ", Collections.emptyList())); // TODO: Replace with actual Item class

        missionTypeSelectorGUI = new MissionTypeSelectorInventoryConfig(27, "&7" + team + " Missions", background2,
                new MissionTypeSelectorInventoryConfig.MissionTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.IRON_INGOT, 11, 1, color + "&lDaily Missions", Collections.emptyList()), true), // TODO: Replace with actual Item class
                new MissionTypeSelectorInventoryConfig.MissionTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.GOLD_INGOT, 13, 1, color + "&lWeekly Missions", Collections.emptyList()), true), // TODO: Replace with actual Item class
                new MissionTypeSelectorInventoryConfig.MissionTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.IRON_INGOT, 11, 1, color + "&lInstant Missions", Collections.emptyList()), false), // TODO: Replace with actual Item class
                new MissionTypeSelectorInventoryConfig.MissionTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.DIAMOND, 15, 1, color + "&lOne Time Missions", Collections.emptyList()), true) // TODO: Replace with actual Item class
        );

        confirmationGUI = new ConfirmationInventoryConfig(27, "&7Are you sure?", background2, new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.GREEN_STAINED_GLASS_PANE, 15, 1, "&a&lYes", Collections.emptyList()), new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.RED_STAINED_GLASS_PANE, 11, 1, color + "&lNo", Collections.emptyList())); // TODO: Replace with actual Item class

        nextPage = new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.LIME_STAINED_GLASS_PANE, 1, "&a&lNext Page", Collections.emptyList()); // TODO: Replace with actual Item class
        previousPage = new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.RED_STAINED_GLASS_PANE, 1, color + "&lPrevious Page", Collections.emptyList()); // TODO: Replace with actual Item class

        blockValueGUI = new NoItemGUI(54, "&7Block Values - Page %page% of %max_pages%", background1);

        spawnerValueGUI = new NoItemGUI(54, "&7Spawner Values - Page %page% of %max_pages%", background1);

        blockValuesTypeSelectorGUI = new BlockValuesTypeSelectorInventoryConfig(27, "&7" + team + " Block Values", background2,
                new BlockValuesTypeSelectorInventoryConfig.BlockTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.DIAMOND_BLOCK, 11, 1, color + "&lBlocks", Collections.emptyList()), true), // TODO: Replace with actual Item class
                new BlockValuesTypeSelectorInventoryConfig.BlockTypeItem(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SPAWNER, 15, 1, color + "&lSpawners", Collections.emptyList()), true) // TODO: Replace with actual Item class
        );
    }
}
