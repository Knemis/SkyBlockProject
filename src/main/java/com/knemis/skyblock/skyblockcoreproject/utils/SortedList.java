package com.knemis.skyblock.skyblockcoreproject.utils; // Adjusted package

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortedList<T> extends ArrayList<T> {
    private final Comparator<T> comparator;

    public SortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T t) {
        int index = Collections.binarySearch(this, t, this.comparator);
        if (index < 0) {
            index = ~index; // Bitwise complement finds the insertion point
        }
        super.add(index, t);
        return true;
    }

    // It's good practice to override other relevant methods if their behavior
    // might be affected by the sorted nature, or if they could break the sort order.
    // For example, addAll, set. However, for initial integration, just 'add' is fine
    // as per the provided code.
    // public boolean addAll(Collection<? extends T> c) {
    //     boolean changed = false;
    //     for (T t : c) {
    //         if (add(t)) { // uses the overridden add(T t)
    //             changed = true;
    //         }
    //     }
    //     return changed;
    // }
    //
    // public T set(int index, T element) {
    //     throw new UnsupportedOperationException("Setting an element at a specific index " +
    //                                             "is not supported in SortedList as it may break sort order. " +
    //                                             "Remove and add instead.");
    // }
    //
    // public void add(int index, T element) {
    //     throw new UnsupportedOperationException("Adding an element at a specific index " +
    //                                             "is not supported in SortedList as it may break sort order. " +
    //                                             "Use add(T element) instead.");
    // }
}
