package com.knemis.skyblock.skyblockcoreproject.teams;

import com.keviin.keviincore.utils.Placeholder;

import java.util.List;
import java.util.Optional;

public interface PlaceholderBuilder<T> {
    List<Placeholder> getPlaceholders(T t);

    List<Placeholder> getPlaceholders(Optional<T> optional);
}
