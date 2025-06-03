package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopCategoryGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopOverviewGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ShopCommand<T extends Team, U extends User<T>> extends Command<T, U> {

    public ShopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 0) {
            player.openInventory(new ShopOverviewGUI<>(player, skyblockTeams).getInventory());
            return true;
        }

        Optional<String> categoryName = getCategoryName(String.join(" ", arguments), skyblockTeams);

        if (!categoryName.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noShopCategory
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ShopCategoryGUI<>(categoryName.get(), player, 1, skyblockTeams).getInventory());
        // player.sendMessage("Shop command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    private Optional<String> getCategoryName(String name, SkyBlockTeams<T, U> skyblockTeams) {
        for (String category : skyblockTeams.getShop().categories.keySet()) { // TODO: Ensure getShop is functional
            if (category.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty(); // Placeholder
    }
}
