package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.admin.AdminShopGUIManager;
// import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils; // Assuming this exists - Not used directly after refactor
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminShopCommand implements CommandExecutor {

    private final SkyBlockProject plugin;
    private final AdminShopGUIManager adminShopGUIManager;

    /**
     * Constructor for the AdminShopCommand.
     * @param plugin The main SkyBlockProject plugin instance.
     * @param adminShopGUIManager The AdminShopGUIManager instance to interact with the shop.
     */
    public AdminShopCommand(SkyBlockProject plugin, AdminShopGUIManager adminShopGUIManager) {
        this.plugin = plugin;
        this.adminShopGUIManager = adminShopGUIManager;
    }

    /**
     * Executes the given command, returning its success.
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle console trying to use /adminshop reload
        if (!(sender instanceof Player)) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // Check console permission if we had a concept of console permissions, otherwise allow.
                // For simplicity, console reload is typically an admin action.
                adminShopGUIManager.reloadConfig(); // reloadConfig() already logs.
                sender.sendMessage(Component.text("AdminShop configuration reloaded from console.", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("This command can only be run by a player, or as '/" + label + " reload' from console.", NamedTextColor.RED));
            }
            return true; // Command was handled (or attempted)
        }

        Player player = (Player) sender;

        // Base permission to use the shop command at all
        if (!player.hasPermission("skyblock.adminshop.use")) {
            // Using AdminShopGUIManager to get messages ensures consistency
            player.sendMessage(adminShopGUIManager.getMessage("no_permission_command", null)); // Add "no_permission_command" to adminshop.yml
            return true;
        }

        // No arguments: Open the main shop GUI
        if (args.length == 0) {
            adminShopGUIManager.openMainShopGUI(player);
            return true;
        }

        // Reload sub-command
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("skyblock.adminshop.reload")) {
                player.sendMessage(adminShopGUIManager.getMessage("no_permission_reload", null)); // Add "no_permission_reload" to adminshop.yml
                return true;
            }
            adminShopGUIManager.reloadConfig(); // reloadConfig() in GUIManager should log to console.
            player.sendMessage(adminShopGUIManager.getMessage("config_reloaded_player", null)); // Add "config_reloaded_player"
            return true;
        }

        // Future sub-commands like /adminshop open <category> [player] could be added here.
        // Example:
        // if (args.length >= 2 && args[0].equalsIgnoreCase("open")) {
        //    // Handle opening a specific category, potentially for another player if permissions allow.
        //    // Requires more permission checks and logic.
        // }

        // Invalid arguments or sub-command not recognized
        player.sendMessage(Component.text("Usage: /" + label + " [reload]", NamedTextColor.RED)); // Basic usage message
        return true; // Return true because we handled the command by showing usage.
    }
}
