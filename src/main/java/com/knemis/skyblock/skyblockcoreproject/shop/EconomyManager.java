package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * Manages interactions with the server's economy system via Vault.
 * This class provides static methods for economy operations.
 */
public class EconomyManager {

    private static Economy economy = null;
    private static Logger logger = Bukkit.getLogger(); // Default logger, replaced by plugin's logger in setupEconomy.

    /**
     * Sets up the economy system using Vault. This should be called once during plugin enabling.
     * It initializes the logger and attempts to hook into a Vault-compatible economy plugin.
     *
     * @param pluginInstance The instance of SkyBlockProject, used to get its logger.
     *                       If null, a default Bukkit logger will be used, and a warning will be issued.
     */
    public static void setupEconomy(SkyBlockProject pluginInstance) {
        if (pluginInstance != null && pluginInstance.getLogger() != null) {
            logger = pluginInstance.getLogger();
        } else {
            // This case should ideally not happen if called correctly from onEnable.
            logger.warning("[EconomyManager] Plugin instance or its logger was null during setup. Using default Bukkit logger. Vault hook might be problematic.");
        }

        if (economy != null) {
            logger.info("[EconomyManager] Economy provider '" + economy.getName() + "' already set up.");
            return;
        }

        // Check if Vault is available
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("[EconomyManager] Vault plugin not found! Economy features will be disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
            logger.info("[EconomyManager] Successfully hooked into Vault and set up Economy provider: " + economy.getName());
        } else {
            logger.severe("[EconomyManager] Vault provider not found! Make sure an economy plugin that hooks into Vault (like EssentialsX Economy) is installed. Economy features will be disabled.");
        }
    }

    /**
     * Checks if the economy system is available and properly hooked.
     *
     * @return True if an economy provider is hooked via Vault, false otherwise.
     */
    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    /**
     * Gets the balance of an offline player.
     *
     * @param player The player whose balance is to be checked. Must not be null.
     * @return The player's balance, or 0.0 if economy is not available or the player is null.
     */
    public static double getBalance(OfflinePlayer player) {
        if (!isEconomyAvailable()) {
            logger.warning("[EconomyManager] getBalance called but economy not available.");
            return 0.0;
        }
        if (player == null) {
            logger.warning("[EconomyManager] getBalance called with a null player.");
            return 0.0;
        }
        return economy.getBalance(player);
    }

    /**
     * Withdraws a specified amount from an offline player's balance.
     *
     * @param player The player from whom to withdraw. Must not be null.
     * @param amount The amount to withdraw. Must be a non-negative value.
     * @return True if the transaction was successful, false otherwise.
     */
    public static boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) {
            logger.warning("[EconomyManager] Withdraw failed: Economy not available.");
            return false;
        }
        if (player == null) {
            logger.warning("[EconomyManager] Withdraw failed: Player is null.");
            return false;
        }
        if (amount < 0) {
            logger.warning(String.format("[EconomyManager] Withdraw failed for %s: Negative amount %.2f specified.", player.getName(), amount));
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (response.transactionSuccess()) {
            // Detailed logging can be uncommented if needed for debugging, but generally, success doesn't need verbose logs.
            // logger.info(String.format("[EconomyManager] Successfully withdrew %.2f from %s (UUID: %s). New balance: %.2f",
            //        amount, player.getName(), player.getUniqueId(), response.balance));
            return true;
        } else {
            logger.warning(String.format("[EconomyManager] Failed to withdraw %.2f from %s (UUID: %s). Reason: %s. Current balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.errorMessage, response.balance));
            return false;
        }
    }

    /**
     * Deposits a specified amount into an offline player's balance.
     *
     * @param player The player to whom to deposit. Must not be null.
     * @param amount The amount to deposit. Must be a non-negative value.
     * @return True if the transaction was successful, false otherwise.
     */
    public static boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) {
            logger.warning("[EconomyManager] Deposit failed: Economy not available.");
            return false;
        }
        if (player == null) {
            logger.warning("[EconomyManager] Deposit failed: Player is null.");
            return false;
        }
         if (amount < 0) {
            logger.warning(String.format("[EconomyManager] Deposit failed for %s: Negative amount %.2f specified.", player.getName(), amount));
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        if (response.transactionSuccess()) {
            // Detailed logging can be uncommented if needed.
            // logger.info(String.format("[EconomyManager] Successfully deposited %.2f to %s (UUID: %s). New balance: %.2f",
            //        amount, player.getName(), player.getUniqueId(), response.balance));
            return true;
        } else {
            logger.warning(String.format("[EconomyManager] Failed to deposit %.2f to %s (UUID: %s). Reason: %s. Current balance: %.2f",
                    amount, player.getName(), player.getUniqueId(), response.errorMessage, response.balance));
            return false;
        }
    }
}
