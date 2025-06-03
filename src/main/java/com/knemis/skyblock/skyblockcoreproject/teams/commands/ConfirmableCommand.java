package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ConfirmationGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ConfirmableCommand<T extends Team, U extends User<T>> extends Command<T, U> {
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
    public final boolean execute(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        if (!isCommandValid(user, team, arguments, skyblockTeams)) {
            return false;
        }

        if (requiresConfirmation) {
            Player player = user.getPlayer();

            player.openInventory(new ConfirmationGUI<>(() -> {
                executeAfterConfirmation(user, team, arguments, skyblockTeams);
            }, skyblockTeams).getInventory());
            // player.sendMessage("Confirmation GUI needs to be reimplemented. Executing command directly."); // Placeholder
            // executeAfterConfirmation(user, team, arguments, skyblockTeams); // Execute directly for now
            return true;
        }

        executeAfterConfirmation(user, team, arguments, skyblockTeams);
        return true;
    }

    protected abstract boolean isCommandValid(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams);

    protected abstract void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams);
}