/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.persisted.config.NullableConfig;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;

public final class EnumUtils {
    private static final Pattern NUM_AFTER_ALPHA_PATTERN = Pattern.compile("([a-z])(\\d)");

    public static List<? extends Enum<?>> getEnumConstants(Class<?> clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) clazz;
            return Arrays.stream(enumClazz.getEnumConstants()).toList();
        } else {
            return List.of();
        }
    }

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public static String toJsonFormat(Enum<?> enumValue) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, enumValue.name());
    }

    public static <E extends Enum<E>> E fromJsonFormat(Class<E> enumClazz, String jsonFormattedName) {
        // CaseFormat cannot do round-trip conversion of e.g. TIER_3, hence the
        // replaceAll
        // We have to account for CHEST_T1, which works fine with CaseFormat, hence the [a-z] regex
        String enumName = CaseFormat.LOWER_CAMEL.to(
                CaseFormat.UPPER_UNDERSCORE,
                NUM_AFTER_ALPHA_PATTERN.matcher(jsonFormattedName).replaceAll("$1_$2"));

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
            return toNiceString(enumValue.name());
        }
    }

    public static String toNiceString(String variantName) {
        return WordUtils.capitalizeFully(variantName.replace("_", " "));
    }

    private static boolean overridesToString(Enum<?> enumValue) {
        try {
            enumValue.getDeclaringClass().getDeclaredMethod("toString");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static final class EnumTypeAdapterFactory<E extends Enum<E>> implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().isEnum()) return null;

            Class<E> enumClazz = (Class<E>) type.getRawType();
            return (TypeAdapter<T>) new EnumTypeAdapter<>(enumClazz);
        }
    }

    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
        private final Class<T> enumClazz;
        private final boolean nullAllowed;

        private EnumTypeAdapter(Class<T> enumClazz) {
            this.enumClazz = enumClazz;
            nullAllowed = NullableConfig.class.isAssignableFrom(enumClazz);
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value != null) {
                out.value(EnumUtils.toJsonFormat(value));
            } else {
                if (!nullAllowed) {
                    WynntilsMod.warn("Writing null enum value to json for " + enumClazz.getSimpleName());
                }
                out.nullValue();
            }
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.STRING) {
                String jsonString = in.nextString();

                T value = EnumUtils.fromJsonFormat(enumClazz, jsonString);
                if (value == null) {
                    WynntilsMod.warn("Illegal enum value: " + jsonString + " for type " + enumClazz.getName());
                    return replacement();
                }

                return value;
            } else if (in.peek() == JsonToken.NULL) {
                in.nextNull();

                if (!nullAllowed) {
                    WynntilsMod.warn("Null enum value for type " + enumClazz.getName());
                    return replacement();
                }

                return null;
            } else {
                WynntilsMod.warn("Invalid json type " + in.peek() + " for enum " + enumClazz.getName());
                return replacement();
            }
        }

        private T replacement() {
            T firstValue = enumClazz.getEnumConstants()[0];
            WynntilsMod.warn("Will replace with first enum value: " + firstValue.name());
            return firstValue;
        }
    }
}
