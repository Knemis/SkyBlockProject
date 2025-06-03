package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.ConfirmationGUI; // TODO: Update to actual ConfirmationGUI class
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ConfirmableCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
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

            // player.openInventory(new ConfirmationGUI<>(() -> { // TODO: Uncomment when ConfirmationGUI is refactored
                // executeAfterConfirmation(user, team, arguments, SkyBlockProjectTeams);
            // }, SkyBlockProjectTeams).getInventory());
            player.sendMessage("Confirmation GUI needs to be reimplemented. Executing command directly."); // Placeholder
            executeAfterConfirmation(user, team, arguments, SkyBlockProjectTeams); // Execute directly for now
            return true;
        }

        executeAfterConfirmation(user, team, arguments, SkyBlockProjectTeams);
        return true;
    }

    protected abstract boolean isCommandValid(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams);

    protected abstract void executeAfterConfirmation(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams);
}