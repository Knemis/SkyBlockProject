package com.knemis.skyblock.skyblockcoreproject.teams.placeholders;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class ClipPlaceholderAPI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends PlaceholderExpansion {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final Placeholders<T, U> placeholders;

    public ClipPlaceholderAPI(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.placeholders = new Placeholders<>(SkyBlockProjectTeams);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return SkyBlockProjectTeams.getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return "Peaches_MLG";
    }

    @Override
    public String getVersion() {
        return SkyBlockProjectTeams.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholderKey) {
        List<Placeholder> placeholderList = placeholders.getPlaceholders(player);

        for (Placeholder placeholder : placeholderList) {
            if (formatPlaceholderKey(placeholder.getKey()).equalsIgnoreCase(placeholderKey)) return placeholder.getValue();
        }

        return null;
    }

    public int getPlaceholderCount(){
        return placeholders.getDefaultPlaceholders().size();
    }

    private String formatPlaceholderKey(String key){
        return key.replace("%", "");
    }
}
