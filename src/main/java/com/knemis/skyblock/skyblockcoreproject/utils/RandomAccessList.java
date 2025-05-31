package com.knemis.skyblock.skyblockcoreproject.utils; // Adjusted package

import lombok.AllArgsConstructor; // Keep if project uses Lombok

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor // Keep if project uses Lombok
public class RandomAccessList<E> {

    private final Map<E, Integer> underlyingList;

    public Optional<E> nextElement() {
        if (underlyingList.isEmpty()) return Optional.empty();
        int listSize = underlyingList.values().stream().mapToInt(Integer::intValue).sum();
        if (listSize == 0) return Optional.empty(); // Avoid ArithmeticException if sum is 0
        int randomIndex = ThreadLocalRandom.current().nextInt(listSize);
        int counter = 0;
        for (E e : underlyingList.keySet()) {
            if (randomIndex >= counter && randomIndex < counter + underlyingList.get(e)) {
                return Optional.of(e);
            }
            counter += underlyingList.get(e);
        }
        // This part should ideally not be reached if logic is correct and listSize > 0.
        // However, to be safe, especially if weights can be zero or negative (though map values are Integer, implying positive weights):
        // Fallback or log an error if this point is reached unexpectedly.
        // For now, returning empty() if loop completes without selection.
        return Optional.empty();
    }

}
