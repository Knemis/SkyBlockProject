package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.InvitesGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class InvitesCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public InvitesCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        player.openInventory(new InvitesGUI<>(team, player, skyblockTeams).getInventory());
        // player.sendMessage("Invites GUI needs to be reimplemented."); // Placeholder
        return true;
    }

}
