package com.keviin.keviinteams.commands;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.ConfirmationGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ConfirmableCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
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
    public final boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        if (!isCommandValid(user, team, arguments, keviinTeams)) {
            return false;
        }

        if (requiresConfirmation) {
            Player player = user.getPlayer();

            player.openInventory(new ConfirmationGUI<>(() -> {
                executeAfterConfirmation(user, team, arguments, keviinTeams);
            }, keviinTeams).getInventory());
            return true;
        }

        executeAfterConfirmation(user, team, arguments, keviinTeams);
        return true;
    }

    protected abstract boolean isCommandValid(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams);

    protected abstract void executeAfterConfirmation(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams);
}