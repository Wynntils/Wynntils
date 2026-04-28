/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.RenamedPrefixesUpfixer;
import com.wynntils.overlays.minimap.CoordinatesOverlay;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;

public class CoordinateOverlayToCoordinatesOverlayUpfixer extends RenamedPrefixesUpfixer {
    private static final String COORDINATE_OVERLAY_REPLACE_DIRECTION_OBJECT_NAME =
            "minimapFeature.coordinateOverlay.replaceDirection";
    private static final String COORDINATE_OVERLAY_COMPASS_DIRECTION_YPOS_OBJECT_NAME =
            "minimapFeature.coordinatesOverlay.compassDirectionYPos";
    private static final List<Pair<String, String>> RENAMED_PREFIXES =
            List.of(Pair.of("minimapFeature.coordinateOverlay.", "minimapFeature.coordinatesOverlay."));

    @Override
    protected List<Pair<String, String>> getRenamedPrefixes() {
        return RENAMED_PREFIXES;
    }

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(COORDINATE_OVERLAY_REPLACE_DIRECTION_OBJECT_NAME)) {
            JsonPrimitive configValue =
                    configObject.getAsJsonPrimitive(COORDINATE_OVERLAY_REPLACE_DIRECTION_OBJECT_NAME);

            if (!configValue.isBoolean()) return true;

            boolean configValueBoolean = configValue.getAsBoolean();

            if (configValueBoolean) {
                configObject.addProperty(
                        COORDINATE_OVERLAY_COMPASS_DIRECTION_YPOS_OBJECT_NAME,
                        EnumUtils.toJsonFormat(CoordinatesOverlay.CompassDirectionYPos.Y_POS));
            } else {
                configObject.addProperty(
                        COORDINATE_OVERLAY_COMPASS_DIRECTION_YPOS_OBJECT_NAME,
                        EnumUtils.toJsonFormat(CoordinatesOverlay.CompassDirectionYPos.DIRECTION));
            }
        }

        return super.apply(configObject, persisteds);
    }
}
