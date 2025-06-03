package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopCategoryGUI; // TODO: Update to actual ShopCategoryGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.ShopOverviewGUI; // TODO: Update to actual ShopOverviewGUI class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ShopCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes

    public ShopCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (arguments.length == 0) { // TODO: Uncomment when ShopOverviewGUI is refactored
            // player.openInventory(new ShopOverviewGUI<>(player, SkyBlockProjectTeams).getInventory());
            // return true;
        // }

        // Optional<String> categoryName = getCategoryName(String.join(" ", arguments), SkyBlockProjectTeams); // TODO: Uncomment when getCategoryName is refactored

        // if (!categoryName.isPresent()) { // TODO: Uncomment when categoryName is available
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noShopCategory // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // player.openInventory(new ShopCategoryGUI<>(categoryName.get(), player, 1, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when ShopCategoryGUI and categoryName are available
        player.sendMessage("Shop command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    private Optional<String> getCategoryName(String name, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // for (String category : SkyBlockProjectTeams.getShop().categories.keySet()) { // TODO: Uncomment when getShop is available
            // if (category.equalsIgnoreCase(name)) {
                // return Optional.of(category);
            // }
        // }
        return Optional.empty(); // Placeholder
    }
}
