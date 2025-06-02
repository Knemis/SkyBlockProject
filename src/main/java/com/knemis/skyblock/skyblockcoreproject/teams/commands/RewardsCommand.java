package com.knemis.skyblock.skyblockcoreproject.teams.commands;


import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.RewardsGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

@NoArgsConstructor
public class RewardsCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public RewardsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        player.openInventory(new RewardsGUI<>(team, player, SkyBlockProjectTeams).getInventory());
        return true;
    }

}
