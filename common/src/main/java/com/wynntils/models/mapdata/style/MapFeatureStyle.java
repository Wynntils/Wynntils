/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import java.util.Optional;

public interface MapFeatureStyle {
    String getId();

    Optional<MapFeatureStyle> getParent();

    Optional<MapFeatureLabel> getLabel();

    Optional<MapFeatureIcon> getIcon();

    // 0-1000, 1000 is highest priority (drawn on top of everything else)
    Optional<Integer> getPriority();
}
