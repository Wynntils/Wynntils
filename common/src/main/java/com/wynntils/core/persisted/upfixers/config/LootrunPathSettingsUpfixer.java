/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.providers.json.JsonOverrideProvider;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import java.util.Locale;
import java.util.Set;

public final class LootrunPathSettingsUpfixer implements Upfixer {
    private static final String LEGACY_PREFIX = "lootrunFeature.";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.keySet().stream().noneMatch(key -> key.startsWith(LEGACY_PREFIX))) return true;

        try {
            CustomColor activeColor = readColor(configObject, "activePathColor");
            CustomColor recordingColor = readColor(configObject, "recordingPathColor");
            if (configObject.has(LEGACY_PREFIX + "rainbowLootRun")
                    && configObject.get(LEGACY_PREFIX + "rainbowLootRun").getAsBoolean()) {
                activeColor = WynncraftShaderColor.RAINBOW.color;
            }

            String icon = readIcon(configObject);
            saveOverride(PathCategoriesProvider.PATH_CATEGORY, activeColor, icon);
            saveOverride(PathCategoriesProvider.RECORDING_CATEGORY, recordingColor, icon);
            configObject.entrySet().removeIf(entry -> entry.getKey().startsWith(LEGACY_PREFIX));

            return true;
        } catch (RuntimeException e) {
            WynntilsMod.warn("Could not migrate lootrun rendering settings; leaving old config intact", e);

            return false;
        }
    }

    private static CustomColor readColor(JsonObject configObject, String key) {
        return Managers.Json.GSON.fromJson(configObject.get(LEGACY_PREFIX + key), CustomColor.class);
    }

    private static String readIcon(JsonObject configObject) {
        if (!configObject.has(LEGACY_PREFIX + "pathType")) return null;

        return switch (configObject
                .get(LEGACY_PREFIX + "pathType")
                .getAsString()
                .toUpperCase(Locale.ROOT)) {
            case "TEXTURED" -> PathCategoriesProvider.ARROW_ICON;
            case "LINE" -> MapIcon.NO_ICON_ID;
            default -> null;
        };
    }

    private static void saveOverride(String categoryId, CustomColor worldColor, String icon) {
        if (worldColor == null && icon == null) return;

        JsonOverrideProvider provider = new JsonOverrideProvider(
                "map_path_override:" + categoryId,
                new MapAttributesBuilder()
                        .setPathColor(worldColor)
                        .setPathIcon(icon)
                        .asPathAttributes()
                        .build(),
                Set.of(),
                Set.of(categoryId));
        if (Services.MapData.getOverrideProvider(provider.getProviderId()) == null) {
            Services.MapData.addOverrideProvider(provider);
        }
    }
}
