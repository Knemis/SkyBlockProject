package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.MembersGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class MembersCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public MembersCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        player.openInventory(new MembersGUI<>(team, player, keviinTeams).getInventory());
        return true;
    }

}
