package com.knemis.skyblock.skyblockcoreproject.teams.commands;


import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ConfirmableCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public final boolean requiresConfirmation;

    public ConfirmableCommand() {
        super();
        this.requiresConfirmation = true;
    }

    public ConfirmableCommand(@NotNull List<String> aliases, @NotNull String description, @NotNull String syntax,
                              @NotNull String permission, long cooldownInSeconds, boolean requiresConfirmation) {
        super(aliases, description, syntax, permission, cooldownInSeconds);
        this.requiresConfirmation = requiresConfirmation;
    }

    @Override
    public final boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        if (!isCommandValid(user, team, arguments, SkyBlockProjectTeams)) {
            return false;
        }

        if (requiresConfirmation) {
            Player player = user.getPlayer();

            player.openInventory(new ConfirmationGUI<>(() -> {
                executeAfterConfirmation(user, team, arguments, SkyBlockProjectTeams);
            }, SkyBlockProjectTeams).getInventory());
            return true;
        }

        executeAfterConfirmation(user, team, arguments, SkyBlockProjectTeams);
        return true;
    }

    protected abstract boolean isCommandValid(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams);

    protected abstract void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams);
}