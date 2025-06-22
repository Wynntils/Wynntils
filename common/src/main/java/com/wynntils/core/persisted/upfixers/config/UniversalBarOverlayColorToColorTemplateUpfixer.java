/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Set;

public class UniversalBarOverlayColorToColorTemplateUpfixer implements Upfixer {
    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has("overlayGroups")) {
            JsonObject overlayGroups = configObject.getAsJsonObject("overlayGroups");
            // Get the universal bar ids from the overlay groups
            if (overlayGroups.has("customBarsOverlayFeature.groupedOverlay.customUniversalBarOverlays.ids")) {
                JsonArray ids = overlayGroups.getAsJsonArray(
                        "customBarsOverlayFeature.groupedOverlay.customUniversalBarOverlays.ids");

                // Replace each "color" config with the "colorTemplate" config that uses the "from_hex" function on the
                // original "color" value
                ids.forEach(id -> {
                    String barName =
                            "customBarsOverlayFeature.universalTexturedCustomBarOverlay" + id.getAsString() + ".color";
                    if (configObject.has(barName)) {
                        String value = configObject.get(barName).getAsString();
                        configObject.addProperty(barName + "Template", "from_hex(\"" + value + "\")");
                    }
                });
            }
        }

        return true;
    }
}
