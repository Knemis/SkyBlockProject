package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BypassCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public BypassCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        user.setBypassing(!user.isBypassing());
        player.sendMessage(StringUtils.color((user.isBypassing() ? skyblockTeams.getMessages().nowBypassing : skyblockTeams.getMessages().noLongerBypassing)
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
        ));
        // if (user.isBypassing()) {
            // player.sendMessage("You are now bypassing team restrictions."); // Placeholder
        // } else {
            // player.sendMessage("You are no longer bypassing team restrictions."); // Placeholder
        // }
        return true;
    }

}
