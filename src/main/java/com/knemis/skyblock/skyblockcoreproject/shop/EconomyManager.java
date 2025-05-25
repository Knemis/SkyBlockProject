package com.knemis.skyblock.skyblockcoreproject.shop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private static Economy economy;

    public static void setupEconomy() {
        if (economy != null) return;

        RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
        }
    }

    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    public static double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}
