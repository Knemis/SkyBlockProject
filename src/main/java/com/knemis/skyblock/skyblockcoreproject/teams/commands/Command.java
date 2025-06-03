package com.knemis.skyblock.skyblockcoreproject.teams.commands;
import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.keviin.keviincore.CooldownProvider; // TODO: Replace CooldownProvider
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.CooldownProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Command<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> { // TODO: Update Team and IridiumUser to actual classes

    public final @NotNull List<String> aliases;
    public final @NotNull String description;
    public final @NotNull String syntax;
    public final @NotNull String permission;
    public final long cooldownInSeconds;
    public final boolean enabled;
    @JsonIgnore
    private com.knemis.skyblock.skyblockcoreproject.core.keviincore.CooldownProvider<CommandSender> cooldownProvider; // TODO: Replace CooldownProvider

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


    public com.knemis.skyblock.skyblockcoreproject.core.keviincore.CooldownProvider<CommandSender> getCooldownProvider() { // TODO: Replace CooldownProvider
        if (cooldownProvider == null) {
            // this.cooldownProvider = new CooldownProvider<>(Duration.ofSeconds(cooldownInSeconds)); // TODO: Replace CooldownProvider
        }

        return cooldownProvider;
    }


    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!(sender instanceof Player)) {
            // sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().mustBeAPlayer // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            // );
            sender.sendMessage("This command can only be executed by a player."); // Placeholder
            return false;
        }
        // return execute(SkyBlockProjectTeams.getUserManager().getUser((OfflinePlayer) sender), arguments, SkyBlockProjectTeams); // TODO: Uncomment when UserManager is refactored
        return false; // Placeholder
    }

    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()); // TODO: Uncomment when TeamManager is refactored
        // if (!team.isPresent()) {
            // user.getPlayer().sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            // );
            // return false;
        // }
        // return execute(user, team.get(), arguments, SkyBlockProjectTeams); // TODO: Uncomment when TeamManager is refactored
        return false; // Placeholder
    }

    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        throw new NotImplementedException();
    }

    public boolean hasPermission(CommandSender commandSender, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return commandSender.hasPermission(permission) || permission.equalsIgnoreCase("");
    }

    public boolean isOnCooldown(CommandSender commandSender, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        // U user = SkyBlockProjectTeams.getUserManager().getUser(player); // TODO: Uncomment when UserManager is refactored
        // return getCooldownProvider().isOnCooldown(commandSender) && !user.isBypassing(); // TODO: Uncomment when CooldownProvider and UserManager are refactored
        return false; // Placeholder
    }

    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (commandSender instanceof Player) {
            // U user = SkyBlockProjectTeams.getUserManager().getUser((OfflinePlayer) commandSender); // TODO: Uncomment when UserManager is refactored
            // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()); // TODO: Uncomment when TeamManager and UserManager are refactored
            // if (team.isPresent()) {
                // return onTabComplete(user, team.get(), args, SkyBlockProjectTeams);
            // }
        }
        return Collections.emptyList();
    }

    public List<String> onTabComplete(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Collections.emptyList();
    }

    @JsonIgnore
    public boolean isSuperSecretCommand(){
        return false;
    }

}
