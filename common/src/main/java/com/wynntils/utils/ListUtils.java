/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.List;

public final class ListUtils {
    public static <T> void swapPairwise(List<T> list) {
        for (int i = 0; i < list.size() - 1; i += 2) {
            T element = list.get(i);
            list.set(i, list.get(i + 1));
            list.set(i + 1, element);
        }
    }

    /**
     * Counts how many elements match between oldList (starting at oldStart)
     * and newList (starting at newStart), up to the end of either list.
     */
    public static <T> int countMatchingElements(List<T> oldList, int oldStart, List<T> newList, int newStart) {
        int max = Math.min(oldList.size() - oldStart, newList.size() - newStart);
        for (int i = 0; i < max; i++) {
            if (!oldList.get(oldStart + i).equals(newList.get(newStart + i))) {
                return i;
            }
        }
        return max;
    }
}
