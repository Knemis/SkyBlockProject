package com.knemis.skyblock.skyblockcoreproject.teams.configs;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item; // Added import
import com.knemis.skyblock.skyblockcoreproject.teams.bank.ExperienceBankItem;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.MoneyBankItem;

import java.util.Arrays;

public class BankItems {
    public ExperienceBankItem experienceBankItem;
    public MoneyBankItem moneyBankItem;

    public BankItems() {
        this("Team", "&c");
    }

    public BankItems(String team, String color) {
        experienceBankItem = new ExperienceBankItem(100, new Item(XMaterial.EXPERIENCE_BOTTLE, 15, 1, color + "&l" + team + " Experience", Arrays.asList(
                "&7%amount% Experience",
                color + "&l[!] " + color + "Left click to withdraw",
                color + "&l[!] " + color + "Right click to deposit")
        ));
        moneyBankItem = new MoneyBankItem(1000, new Item(XMaterial.PAPER, 11, 1, color + "&l" + team + " Money", Arrays.asList(
                "&7$%amount%",
                color + "&l[!] " + color + "Left click to withdraw",
                color + "&l[!] " + color + "Right click to deposit")
        ));
    }
}
