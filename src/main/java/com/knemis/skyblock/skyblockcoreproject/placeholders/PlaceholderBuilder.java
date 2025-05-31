package com.knemis.skyblock.skyblockcoreproject.placeholders;

import com.knemis.skyblock.skyblockcoreproject.utils.Placeholder; // Our new Placeholder class
import java.util.List;
import java.util.Optional;

public interface PlaceholderBuilder<T> {
    List<Placeholder> getPlaceholders(T object);
    List<Placeholder> getPlaceholders(Optional<T> optionalObject);
}
