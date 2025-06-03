package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.ChatType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ChatCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public ChatCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // Optional<ChatType> chatType = SkyBlockProjectTeams.getChatTypes().stream() // TODO: Uncomment when getChatTypes is available
                // .filter(type -> type.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])))
                // .findFirst();
        // if (!chatType.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownChatType // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%type%", args[0]))
            // );
            // return false;
        // }

        // String chat = WordUtils.capitalizeFully(chatType.get().getAliases().stream().max(Comparator.comparing(String::length)).orElse(args[0]));
        // user.setChatType(chat);
        // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().setChatType // TODO: Replace StringUtils.color
                // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // .replace("%type%", chat))
        // );
        player.sendMessage("Chat command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // return SkyBlockProjectTeams.getChatTypes().stream() // TODO: Uncomment when getChatTypes is available
                // .flatMap(chatTypes -> chatTypes.getAliases().stream())
                // .collect(Collectors.toList());
        return Collections.emptyList(); // Placeholder
    }
}
