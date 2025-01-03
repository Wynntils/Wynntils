/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedKeysUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class WorldMarkersFeatureRenamesUpfixer extends RenamedKeysUpfixer {
    private static final List<Pair<String, String>> RENAMED_KEYS = List.of(
            Pair.of("worldMarkers.waypointBeamColor", "worldMarkersFeature.markerBeaconColor"),
            Pair.of("worldMarkers.backgroundOpacity", "worldMarkersFeature.textBackgroundOpacity"),
            Pair.of("worldMarkers.scale", "worldMarkersFeature.textRenderScale"),
            Pair.of("worldMarkers.topBoundingDistance", "worldMarkersFeature.screenMarginTop"),
            Pair.of("worldMarkers.bottomBoundingDistance", "worldMarkersFeature.screenMarginBottom"),
            Pair.of("worldMarkers.horizontalBoundingDistance", "worldMarkersFeature.screenMarginSides"));

    @Override
    protected List<Pair<String, String>> getRenamedKeys() {
        return RENAMED_KEYS;
    }
}
