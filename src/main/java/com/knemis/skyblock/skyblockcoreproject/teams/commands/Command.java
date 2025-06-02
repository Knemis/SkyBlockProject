package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knemis.skyblock.skyblockcoreproject.secondcore.CooldownProvider;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Command<T extends Team, U extends SkyBlockProjectTeamsUser<T>> {

    public final @NotNull List<String> aliases;
    public final @NotNull String description;
    public final @NotNull String syntax;
    public final @NotNull String permission;
    public final long cooldownInSeconds;
    public final boolean enabled;
    @JsonIgnore
    private CooldownProvider<CommandSender> cooldownProvider;

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


    public CooldownProvider<CommandSender> getCooldownProvider() {
        if (cooldownProvider == null) {
            this.cooldownProvider = new CooldownProvider<>(Duration.ofSeconds(cooldownInSeconds));
        }

        return cooldownProvider;
    }


    public boolean execute(CommandSender sender, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().mustBeAPlayer
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
            return false;
        }
        return execute(SkyBlockProjectTeams.getUserManager().getUser((OfflinePlayer) sender), arguments, SkyBlockProjectTeams);
    }

    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
        if (!team.isPresent()) {
            user.getPlayer().sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
            return false;
        }
        return execute(user, team.get(), arguments, SkyBlockProjectTeams);
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
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        return getCooldownProvider().isOnCooldown(commandSender) && !user.isBypassing();
    }

    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (commandSender instanceof Player) {
            U user = SkyBlockProjectTeams.getUserManager().getUser((OfflinePlayer) commandSender);
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
            if (team.isPresent()) {
                return onTabComplete(user, team.get(), args, SkyBlockProjectTeams);
            }
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
