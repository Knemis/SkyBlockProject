package com.knemis.skyblock.skyblockcoreproject.teams;

import com.cryptomorin.xseries.XSound;
// import com.keviin.keviincore.Item;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Reward {

    public com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
    public List<String> commands;
    public double money;
    public Map<String, Double> bankRewards;
    public int experience;
    public int teamExperience;
    public XSound sound;

}
