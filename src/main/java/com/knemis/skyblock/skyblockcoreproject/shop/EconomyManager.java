package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public class EconomyManager {

    private static Economy economy = null;
    private static Logger logger = null; // Initialized in setupEconomy or retrieved statically

    // Call this from SkyBlockProject.enableVault() after Vault is confirmed to be hooked.
    public static void setupEconomy(SkyBlockProject pluginInstance) {
        if (pluginInstance == null) {
            // Fallback or critical error if plugin instance cannot be obtained
            logger = Bukkit.getLogger(); // Or Logger.getLogger(EconomyManager.class.getName())
            logger.severe("[EconomyManager] Plugin instance was null during setup. Vault hook might be problematic or logging will be generic.");
        } else {
            logger = pluginInstance.getLogger();
        }

        if (economy != null) {
            logger.info("[EconomyManager] Economy already set up.");
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
            logger.info("[EconomyManager] Successfully hooked into Vault and set up Economy provider: " + economy.getName());
        } else {
            logger.warning("[EconomyManager] Vault provider not found! Economy features will be disabled.");
        }
    }
    
    private static Logger getLogger() {
        if (logger == null) {
            // Attempt to get logger from SkyBlockProject if it has a static instance getter
            // This is a common pattern but not guaranteed.
            try {
                SkyBlockProject plugin = SkyBlockProject.getPlugin(SkyBlockProject.class);
                if (plugin != null) {
                    logger = plugin.getLogger();
                } else {
                    logger = Bukkit.getLogger(); // Fallback
                    logger.warning("[EconomyManager] Could not get SkyBlockProject logger statically, using Bukkit.getLogger().");
                }
            } catch (Exception e) {
                logger = Bukkit.getLogger(); // Fallback
                 logger.warning("[EconomyManager] Exception while trying to get SkyBlockProject logger, using Bukkit.getLogger(). Error: " + e.getMessage());
            }
        }
        return logger;
    }


    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    public static double getBalance(OfflinePlayer player) {
        if (!isEconomyAvailable() || player == null) {
            getLogger().warning("[EconomyManager] getBalance called but economy not available or player null. Player: " + (player != null ? player.getName() : "null"));
            return 0.0;
        }
        return economy.getBalance(player);
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable() || player == null) {
            getLogger().warning(String.format("[EconomyManager] Withdraw failed for %s (amount: %.2f): Economy not available or player null.",
                    (player != null ? player.getName() : "null_player"), amount));
            return false;
        }
        if (amount < 0) {
            getLogger().warning(String.format("[EconomyManager] Withdraw failed for %s: Negative amount %.2f specified.", player.getName(), amount));
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (response.transactionSuccess()) {
            getLogger().info(String.format("[EconomyManager] Successfully withdrew %.2f from %s (UUID: %s). New balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.balance));
            return true;
        } else {
            getLogger().warning(String.format("[EconomyManager] Failed to withdraw %.2f from %s (UUID: %s). Reason: %s. Current balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.errorMessage, response.balance));
            return false;
        }
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable() || player == null) {
             getLogger().warning(String.format("[EconomyManager] Deposit failed for %s (amount: %.2f): Economy not available or player null.",
                    (player != null ? player.getName() : "null_player"), amount));
            return false;
        }
         if (amount < 0) {
            getLogger().warning(String.format("[EconomyManager] Deposit failed for %s: Negative amount %.2f specified.", player.getName(), amount));
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        if (response.transactionSuccess()) {
            getLogger().info(String.format("[EconomyManager] Successfully deposited %.2f to %s (UUID: %s). New balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.balance));
            return true;
        } else {
            getLogger().warning(String.format("[EconomyManager] Failed to deposit %.2f to %s (UUID: %s). Reason: %s. Current balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.errorMessage, response.balance));
            return false;
        }
    }
}
