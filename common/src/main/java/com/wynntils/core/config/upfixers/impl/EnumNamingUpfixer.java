/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CustomColor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;

public class EnumNamingUpfixer implements ConfigUpfixer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .registerTypeAdapterFactory(new EnumConverterFactory())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder<?>> configHolders) {
        for (ConfigHolder<?> config : configHolders) {
            String jsonName = config.getJsonName();
            if (!configObject.has(jsonName)) continue;

            JsonElement origJson = configObject.get(jsonName);
            Object value = GSON.fromJson(origJson, config.getType());
            JsonElement newJson = Managers.Json.GSON.toJsonTree(value, config.getType());

            if (!(newJson.toString().equals(origJson.toString()))) {
                configObject.add(jsonName, newJson);
            }
        }

        return true;
    }

    private static final class EnumConverterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().isEnum()) return null;

            Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) type.getRawType();
            return new EnumConverter<>(enumClazz);
        }
    }

    private static final class EnumConverter<T> extends TypeAdapter<T> {
        private final Class<? extends Enum<?>> enumClazz;

        private EnumConverter(Class<? extends Enum<?>> enumClazz) {
            this.enumClazz = enumClazz;
        }

        @Override
        public void write(JsonWriter out, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.STRING) {
                in.nextNull();
                return null;
            }

            String jsonString = in.nextString();

            Enum<?> value;
            try {
                // The double casting is needed, or javac will complain...
                value = Enum.valueOf((Class<Enum>) (Type) enumClazz, jsonString);
            } catch (IllegalArgumentException e) {
                // Maybe it is already converted?
                value = EnumUtils.fromJsonFormat(enumClazz, jsonString);
            }
            if (value == null) {
                WynntilsMod.warn("Illegal enum value: " + jsonString + " for type " + enumClazz.getName());
                return replacement();
            }

            return (T) value;
        }

        private T replacement() {
            Enum<?> firstValue = enumClazz.getEnumConstants()[0];
            WynntilsMod.warn("Will replace with first enum value: " + firstValue.name());
            return (T) firstValue;
        }
    }
}
