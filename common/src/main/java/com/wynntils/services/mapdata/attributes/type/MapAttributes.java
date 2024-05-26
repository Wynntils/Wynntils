/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

public interface MapAttributes {
    // If this is the empty string (""), then no label will be displayed
    // empty means inherit
    Optional<String> getLabel();

    // If this is MapFeatureIcon.NO_ICON_ID ("none"), then no icon will be displayed
    // empty means inherit
    Optional<String> getIconId();

    // 1-1000, 1000 is the highest priority (drawn on top of everything else)
    // empty means no value specified; inherit
    Optional<Integer> getPriority();

    // the minimum combat level for which this feature is suitable for
    // 0 means suitable for all levels
    // -1 means no information is available, or level is not applicable
    // empty means inherit
    Optional<Integer> getLevel();

    Optional<MapVisibility> getLabelVisibility();

    Optional<CustomColor> getLabelColor();

    Optional<TextShadow> getLabelShadow();

    Optional<MapVisibility> getIconVisibility();

    Optional<CustomColor> getIconColor();

    Optional<MapDecoration> getIconDecoration();
}
