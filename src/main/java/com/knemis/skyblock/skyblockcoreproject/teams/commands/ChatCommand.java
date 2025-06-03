package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.ChatType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ChatCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public ChatCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        Optional<ChatType> chatType = skyblockTeams.getChatTypes().stream()
                .filter(type -> type.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])))
                .findFirst();
        if (!chatType.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().unknownChatType
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%type%", args[0]))
            );
            return false;
        }

        String chat = WordUtils.capitalizeFully(chatType.get().getAliases().stream().max(Comparator.comparing(String::length)).orElse(args[0]));
        user.setChatType(chat);
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().setChatType
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%type%", chat))
        );
        // player.sendMessage("Chat command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return skyblockTeams.getChatTypes().stream()
                .flatMap(ct -> ct.getAliases().stream()) // Corrected variable name from chatTypes to ct
                .collect(Collectors.toList());
        // return Collections.emptyList(); // Placeholder
    }
}
