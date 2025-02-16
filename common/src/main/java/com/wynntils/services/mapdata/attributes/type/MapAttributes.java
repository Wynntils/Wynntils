/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

/**
 * Defines the possible attributes that can be defined. These can be contributed to
 * either directly in a map feature, or by any level of the category hierarchy.
 * To contribute a value, implementor should return a non-empty Optional value.
 * If the value is empty, the search will continue for another implementation,
 * i.e. the value is inherited.
 */
public interface MapAttributes {
    // region Feature (common) Attributes

    // 1-1000, 1000 is the highest priority (drawn on top of everything else)
    Optional<Integer> getPriority();

    // The minimum combat level for which this feature is suitable for
    // 0 means no information is available, or level is not applicable
    // 1 means suitable for all levels
    Optional<Integer> getLevel();

    // endregion

    // region Label Attributes

    // If this is the empty string (""), then no label will be displayed
    Optional<String> getLabel();

    // If this is the empty string (""), then no secondary label will be displayed
    Optional<String> getSecondaryLabel();

    // The visibility of the label on the map
    Optional<MapVisibility> getLabelVisibility();

    // The color of the label on the map, and in-world
    Optional<CustomColor> getLabelColor();

    // The shadow of the label on the map, and in-world
    Optional<TextShadow> getLabelShadow();

    // endregion

    // region Icon Attributes

    // If this is MapFeatureIcon.NO_ICON_ID ("none"), then no icon will be displayed
    Optional<String> getIconId();

    // The visibility of the icon on the map
    Optional<MapVisibility> getIconVisibility();

    // The color of the icon on the map, and in-world
    Optional<CustomColor> getIconColor();

    // The decoration of the icon on the map, and in-world
    Optional<MapDecoration> getIconDecoration();

    // endregion

    // region MapLocation Marker Attributes

    // Whether the marker is enabled on the map
    // (the marker may still not be visible, depending on the marker options)
    Optional<Boolean> getHasMarker();

    // The options of the marker in the world
    Optional<MapMarkerOptions> getMarkerOptions();

    // endregion

    // region Area & Border Attributes

    // The color of the area on the map
    Optional<CustomColor> getFillColor();

    // The color of the border on the map
    Optional<CustomColor> getBorderColor();

    // The width of the border on the map
    Optional<Float> getBorderWidth();

    // endregion
}
