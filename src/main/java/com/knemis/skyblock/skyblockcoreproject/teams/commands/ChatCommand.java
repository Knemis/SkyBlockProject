package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.ChatType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ChatCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public ChatCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<ChatType> chatType = SkyBlockProjectTeams.getChatTypes().stream()
                .filter(type -> type.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])))
                .findFirst();
        if (!chatType.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownChatType
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%type%", args[0]))
            );
            return false;
        }

        String chat = WordUtils.capitalizeFully(chatType.get().getAliases().stream().max(Comparator.comparing(String::length)).orElse(args[0]));
        user.setChatType(chat);
        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setChatType
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%type%", chat))
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return SkyBlockProjectTeams.getChatTypes().stream()
                .flatMap(chatTypes -> chatTypes.getAliases().stream())
                .collect(Collectors.toList());
    }
}
