/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.profile;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.utils.colors.CustomColor;
import java.lang.reflect.Type;

public record GuildProfile(String prefix, String name, CustomColor color) {
    public static class GuildProfileDeserializer implements JsonDeserializer<GuildProfile> {
        @Override
        public GuildProfile deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Temporary fix for Athena bug
            if (jsonObject.get("_id").isJsonNull() || jsonObject.get("prefix").isJsonNull()) {
                return null;
            }

            String name = jsonObject.getAsJsonPrimitive("_id").getAsString();
            String prefix = jsonObject.getAsJsonPrimitive("prefix").getAsString();

            // Color can be null, "", or a hex string
            String colorString = jsonObject.has("color")
                    ? jsonObject.getAsJsonPrimitive("color").getAsString()
                    : "";

            // If color is null or empty, use the name to generate a color
            CustomColor color = colorString.isBlank()
                    ? CustomColor.colorForStringHash(name)
                    : CustomColor.fromHexString(colorString);

            // Handle edge cases
            if (color == CustomColor.NONE) {
                color = CustomColor.colorForStringHash(name);
            }

            return new GuildProfile(prefix, name, color);
        }
    }
}
