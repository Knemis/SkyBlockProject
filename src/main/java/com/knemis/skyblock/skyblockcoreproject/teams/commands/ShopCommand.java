package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopCategoryGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopOverviewGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ShopCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {

    public ShopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 0) {
            player.openInventory(new ShopOverviewGUI<>(player, SkyBlockProjectTeams).getInventory());
            return true;
        }

        Optional<String> categoryName = getCategoryName(String.join(" ", arguments), SkyBlockProjectTeams);

        if (!categoryName.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noShopCategory
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ShopCategoryGUI<>(categoryName.get(), player, 1, SkyBlockProjectTeams).getInventory());
        return true;
    }

    private Optional<String> getCategoryName(String name, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        for (String category : SkyBlockProjectTeams.getShop().categories.keySet()) {
            if (category.equalsIgnoreCase(name)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
