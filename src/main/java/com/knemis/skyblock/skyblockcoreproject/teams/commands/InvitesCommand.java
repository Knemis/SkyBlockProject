package com.knemis.skyblock.skyblockcoreproject.teams.commands;


import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class InvitesCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public InvitesCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        player.openInventory(new InvitesGUI<>(team, player, keviinTeams).getInventory());
        return true;
    }

}
