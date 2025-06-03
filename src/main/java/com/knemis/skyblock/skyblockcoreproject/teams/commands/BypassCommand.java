package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BypassCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public BypassCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        user.setBypassing(!user.isBypassing());
        // player.sendMessage(StringUtils.color((user.isBypassing() ? SkyBlockProjectTeams.getMessages().nowBypassing : SkyBlockProjectTeams.getMessages().noLongerBypassing) // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
        // ));
        if (user.isBypassing()) {
            player.sendMessage("You are now bypassing team restrictions."); // Placeholder
        } else {
            player.sendMessage("You are no longer bypassing team restrictions."); // Placeholder
        }
        return true;
    }

}
