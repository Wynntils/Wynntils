/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import java.util.Optional;

public interface MapFeatureAttributes {
    Optional<String> getLabel();

    Optional<MapIcon> getIcon();

    Optional<MapFeatureAttributes> getParent();

    Optional<MapFeatureLabelStyle> getLabelStyle();

    Optional<MapFeatureIconStyle> getIconStyle();

    // 0-1000, 1000 is highest priority (drawn on top of everything else)
    Optional<Integer> getPriority();


    /// =====

    MapFeatureAttributes EMPTY = null;


    MapFeatureAttributes overriddenBy(MapFeatureAttributes style);
}
