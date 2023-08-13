/*
 * Copyright © Wynntils 2023.
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
}
