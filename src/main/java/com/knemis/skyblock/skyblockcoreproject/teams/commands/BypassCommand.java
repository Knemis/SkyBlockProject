package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BypassCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public BypassCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        user.setBypassing(!user.isBypassing());
        player.sendMessage(StringUtils.color((user.isBypassing() ? keviinTeams.getMessages().nowBypassing : keviinTeams.getMessages().noLongerBypassing)
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
        ));
        return true;
    }

}
