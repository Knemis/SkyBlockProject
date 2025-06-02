package com.knemis.skyblock.skyblockcoreproject.teams;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;

import java.util.List;
import java.util.Optional;

public interface PlaceholderBuilder<T> {
    List<Placeholder> getPlaceholders(T t);

    List<Placeholder> getPlaceholders(Optional<T> optional);
}
