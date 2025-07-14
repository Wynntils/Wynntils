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
    private static final String OVERLAY_GROUPS_KEY = "overlayGroups";
    private static final String UNIVERSAL_BAR_IDS_KEY =
            "customBarsOverlayFeature.groupedOverlay.customUniversalBarOverlays.ids";
    private static final String UNIVERSAL_BAR_COLOR_KEY =
            "customBarsOverlayFeature.universalTexturedCustomBarOverlay%s.color";
    private static final String UNIVERSAL_BAR_COLOR_TEMPLATE_KEY =
            "customBarsOverlayFeature.universalTexturedCustomBarOverlay%s.colorTemplate";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(OVERLAY_GROUPS_KEY)) {
            JsonObject overlayGroups = configObject.getAsJsonObject(OVERLAY_GROUPS_KEY);
            // Get the universal bar ids from the overlay groups
            if (overlayGroups.has(UNIVERSAL_BAR_IDS_KEY)) {
                JsonArray ids = overlayGroups.getAsJsonArray(UNIVERSAL_BAR_IDS_KEY);

                // Replace each "color" config with the "colorTemplate" config that uses the "from_hex" function on the
                // original "color" value
                ids.forEach(id -> {
                    String barColorKey = String.format(UNIVERSAL_BAR_COLOR_KEY, id.getAsString());
                    if (configObject.has(barColorKey)) {
                        String value = configObject.get(barColorKey).getAsString();
                        String barColorTemplateKey = String.format(UNIVERSAL_BAR_COLOR_TEMPLATE_KEY, id.getAsString());
                        configObject.addProperty(barColorTemplateKey, "from_hex(\"" + value + "\")");
                    }
                });
            }
        }

        return true;
    }
}
