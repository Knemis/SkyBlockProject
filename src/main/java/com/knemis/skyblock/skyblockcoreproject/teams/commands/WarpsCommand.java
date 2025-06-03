package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.WarpsGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class WarpsCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public WarpsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        player.openInventory(new WarpsGUI<>(team, player, skyblockTeams).getInventory());
        return true;
    }

}
