package com.knemis.skyblock.skyblockcoreproject.teams.placeholders;

import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class ClipPlaceholderAPI<T extends Team, U extends keviinUser<T>> extends PlaceholderExpansion {

    private final keviinTeams<T, U> keviinTeams;
    private final Placeholders<T, U> placeholders;

    public ClipPlaceholderAPI(keviinTeams<T, U> keviinTeams) {
        this.keviinTeams = keviinTeams;
        this.placeholders = new Placeholders<>(keviinTeams);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return keviinTeams.getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return "Peaches_MLG";
    }

    @Override
    public String getVersion() {
        return keviinTeams.getDescription().getVersion();
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
