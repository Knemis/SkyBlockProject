package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.ShopCategoryGUI;
import com.keviin.keviinteams.gui.ShopOverviewGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ShopCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    public ShopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 0) {
            player.openInventory(new ShopOverviewGUI<>(player, keviinTeams).getInventory());
            return true;
        }

        Optional<String> categoryName = getCategoryName(String.join(" ", arguments), keviinTeams);

        if (!categoryName.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noShopCategory
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ShopCategoryGUI<>(categoryName.get(), player, 1, keviinTeams).getInventory());
        return true;
    }

    private Optional<String> getCategoryName(String name, keviinTeams<T, U> keviinTeams) {
        for (String category : keviinTeams.getShop().categories.keySet()) {
            if (category.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
