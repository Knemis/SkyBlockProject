package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // TODO: Replace with actual Item class
import com.knemis.skyblock.skyblockcoreproject.teams.Setting;

import java.util.Arrays;

public class Settings {

    public Setting teamJoining;
    public Setting teamValue;
    public Setting mobSpawning;
    public Setting leafDecay;
    public Setting iceForm;
    public Setting fireSpread;
    public Setting cropTrample;
    public Setting weather;
    public Setting time;
    public Setting entityGrief;
    public Setting tntDamage;
    public Setting visiting;

    public Settings() {
        this("Team", "&c");
    }

    public Settings(String team, String color) {
        teamJoining = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.GUNPOWDER, 10, 1, color + team + " Type", Arrays.asList("&7Set your " + team + " joining method.", "", "" + color + "&lValue", "&7%value%")), "JoinType", "Private"); // TODO: Replace with actual Item class
        teamValue = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SUNFLOWER, 11, 1, color + team + " Value Visibility", Arrays.asList("&7Set your " + team + " value.", "", "" + color + "&lValue", "&7%value%")), "ValueVisibility", "Public"); // TODO: Replace with actual Item class
        mobSpawning = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.SPAWNER, 12, 1, color + team + " Mob Spawning", Arrays.asList("&7Control Mob Spawning on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "MobSpawning", "Enabled"); // TODO: Replace with actual Item class
        leafDecay = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.JUNGLE_LEAVES, 13, 1, color + team + " Leaf Decay", Arrays.asList("&7Control Leaf Decay on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "LeafDecay", "Disabled"); // TODO: Replace with actual Item class
        iceForm = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.ICE, 14, 1, color + team + " Ice Form", Arrays.asList("&7Control Ice Forming on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "IceForm", "Disabled"); // TODO: Replace with actual Item class
        fireSpread = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.FLINT_AND_STEEL, 15, 1, color + team + " Fire Spread", Arrays.asList("&7Control Fire Spread on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "FireSpread", "Disabled"); // TODO: Replace with actual Item class
        cropTrample = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.WHEAT_SEEDS, 16, 1, color + team + " Crop Trample", Arrays.asList("&7Control Trampling Crops on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "CropTrample", "Enabled"); // TODO: Replace with actual Item class
        weather = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.BLAZE_POWDER, 19, 1, color + team + " Weather", Arrays.asList("&7Control Weather on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "Weather", "Server"); // TODO: Replace with actual Item class
        time = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.CLOCK, 20, 1, color + team + " Time", Arrays.asList("&7Control Time on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "Time", "Server"); // TODO: Replace with actual Item class
        entityGrief = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.ENDER_PEARL, 21, 1, color + team + " Entity Grief", Arrays.asList("&7Control Entity Grief on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "EntityGrief", "Disabled"); // TODO: Replace with actual Item class
        tntDamage = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.TNT, 22, 1, color + team + " TnT Damage", Arrays.asList("&7Control TnT Damage on your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "TnTDamage", "Enabled"); // TODO: Replace with actual Item class
        visiting = new Setting(new com.knemis.skyblock.skyblockcoreproject.teams.Item(XMaterial.BEACON, 23, 1, color + team + " Visiting", Arrays.asList("&7Control if people can visit your " + team + ".", "", "" + color + "&lValue", "&7%value%")), "Visiting", "Enabled"); // TODO: Replace with actual Item class
    }

}
