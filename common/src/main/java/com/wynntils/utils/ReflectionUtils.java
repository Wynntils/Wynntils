/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtils {
    public static <T> List<T> createListWithType(Class<T> clazz) {
        return new ArrayList<>();
    }
}
