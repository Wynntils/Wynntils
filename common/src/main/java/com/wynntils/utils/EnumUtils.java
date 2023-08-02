/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.base.CaseFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.text.WordUtils;

public final class EnumUtils {
    public static List<? extends Enum<?>> getEnumConstants(Class<?> clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) clazz;
            return Arrays.stream(enumClazz.getEnumConstants()).toList();
        } else {
            return List.of();
        }
    }

    public static String toJsonFormat(Enum<?> enumValue) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, enumValue.name());
    }

    public static <E extends Enum<E>> E fromJsonFormat(Class<E> enumClazz, String jsonFormattedName) {
        // CaseFormat cannot do round-trip conversion of e.g. TIER_3, hence the
        // replaceAll
        // We have to account for CHEST_T1, which works fine with CaseFormat, hence the [a-z] regex
        String enumName = CaseFormat.LOWER_CAMEL.to(
                CaseFormat.UPPER_UNDERSCORE, jsonFormattedName.replaceAll("([a-z])(\\d)", "$1_$2"));

        try {
            return Enum.valueOf(enumClazz, enumName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String toNiceString(Enum<?> enumValue) {
        if (overridesToString(enumValue)) {
            // If we override toString, use that
            return enumValue.toString();
        } else {
            return WordUtils.capitalizeFully(enumValue.name().replace("_", " "));
        }
    }

    private static boolean overridesToString(Enum<?> enumValue) {
        try {
            enumValue.getDeclaringClass().getDeclaredMethod("toString");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
