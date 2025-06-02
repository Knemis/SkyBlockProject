package com.knemis.skyblock.skyblockcoreproject.teams;

// import com.keviin.keviincore.utils.Placeholder;

import java.util.List;
import java.util.Optional;

public interface PlaceholderBuilder<T> {
    List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders(T t); // TODO: Replace with actual Placeholder class

    List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getPlaceholders(Optional<T> optional); // TODO: Replace with actual Placeholder class
}
