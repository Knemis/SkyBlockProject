package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.keviin.keviincore.CooldownProvider; // TODO: Replace CooldownProvider
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Command<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> { // TODO: Update Team and IridiumUser to actual classes

    public final @NotNull List<String> aliases;
    public final @NotNull String description;
    public final @NotNull String syntax;
    public final @NotNull String permission;
    public final long cooldownInSeconds;
    public final boolean enabled;
    @JsonIgnore
    private com.knemis.skyblock.skyblockcoreproject.teams.CooldownProvider<CommandSender> cooldownProvider; // TODO: Replace CooldownProvider

    public Command() {
        this.aliases = Collections.emptyList();
        this.description = "";
        this.syntax = "";
        this.permission = "";
        this.cooldownInSeconds = 0;
        this.enabled = true;
    }

    public Command(@NotNull List<String> aliases, @NotNull String description, @NotNull String syntax, @NotNull String permission, long cooldownInSeconds) {
        this.aliases = aliases;
        this.description = description;
        this.syntax = syntax;
        this.permission = permission;
        this.cooldownInSeconds = cooldownInSeconds;
        this.enabled = true;
    }


    public com.knemis.skyblock.skyblockcoreproject.teams.CooldownProvider<CommandSender> getCooldownProvider() { // TODO: Replace CooldownProvider
        if (cooldownProvider == null) {
            // this.cooldownProvider = new CooldownProvider<>(Duration.ofSeconds(cooldownInSeconds)); // TODO: Replace CooldownProvider
        }

        return cooldownProvider;
    }


    public boolean execute(CommandSender sender, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        if (!(sender instanceof Player)) {
            // sender.sendMessage(StringUtils.color(iridiumTeams.getMessages().mustBeAPlayer // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
            // );
            sender.sendMessage("This command can only be executed by a player."); // Placeholder
            return false;
        }
        // return execute(iridiumTeams.getUserManager().getUser((OfflinePlayer) sender), arguments, iridiumTeams); // TODO: Uncomment when UserManager is refactored
        return false; // Placeholder
    }

    public boolean execute(U user, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        // Optional<T> team = iridiumTeams.getTeamManager().getTeamViaID(user.getTeamID()); // TODO: Uncomment when TeamManager is refactored
        // if (!team.isPresent()) {
            // user.getPlayer().sendMessage(StringUtils.color(iridiumTeams.getMessages().dontHaveTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
            // );
            // return false;
        // }
        // return execute(user, team.get(), arguments, iridiumTeams); // TODO: Uncomment when TeamManager is refactored
        return false; // Placeholder
    }

    public boolean execute(U user, T team, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        throw new NotImplementedException();
    }

    public boolean hasPermission(CommandSender commandSender, IridiumTeams<T, U> iridiumTeams) {
        return commandSender.hasPermission(permission) || permission.equalsIgnoreCase("");
    }

    public boolean isOnCooldown(CommandSender commandSender, IridiumTeams<T, U> iridiumTeams) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        // U user = iridiumTeams.getUserManager().getUser(player); // TODO: Uncomment when UserManager is refactored
        // return getCooldownProvider().isOnCooldown(commandSender) && !user.isBypassing(); // TODO: Uncomment when CooldownProvider and UserManager are refactored
        return false; // Placeholder
    }

    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        if (commandSender instanceof Player) {
            // U user = iridiumTeams.getUserManager().getUser((OfflinePlayer) commandSender); // TODO: Uncomment when UserManager is refactored
            // Optional<T> team = iridiumTeams.getTeamManager().getTeamViaID(user.getTeamID()); // TODO: Uncomment when TeamManager and UserManager are refactored
            // if (team.isPresent()) {
                // return onTabComplete(user, team.get(), args, iridiumTeams);
            // }
        }
        return Collections.emptyList();
    }

    public List<String> onTabComplete(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Collections.emptyList();
    }

    @JsonIgnore
    public boolean isSuperSecretCommand(){
        return false;
    }

}
