/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.merge;

import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class MapAttributesMerger {
    public static MapAttributes mergeAttributes(List<MapAttributes> attributes) {
        Integer priority = getFirstOrNull(attributes, MapAttributes::getPriority);
        Integer level = getFirstOrNull(attributes, MapAttributes::getLevel);
        String label = getFirstOrNull(attributes, MapAttributes::getLabel);
        String description = getFirstOrNull(attributes, MapAttributes::getDescription);
        MapVisibilityImpl labelVisibility = mergeVisibilities(attributes, MapAttributes::getLabelVisibility);
        CustomColor labelColor = getFirstOrNull(attributes, MapAttributes::getLabelColor);
        TextShadow labelShadow = getFirstOrNull(attributes, MapAttributes::getLabelShadow);
        String icon = getFirstOrNull(attributes, MapAttributes::getIconId);
        MapVisibilityImpl iconVisibility = mergeVisibilities(attributes, MapAttributes::getIconVisibility);
        CustomColor iconColor = getFirstOrNull(attributes, MapAttributes::getIconColor);
        Boolean hasMarker = getFirstOrNull(attributes, MapAttributes::getHasMarker);
        MapMarkerOptionsImpl markerOptions = mergeMarkerOptions(attributes);
        CustomColor fillColor = getFirstOrNull(attributes, MapAttributes::getFillColor);
        CustomColor borderColor = getFirstOrNull(attributes, MapAttributes::getBorderColor);
        Float borderWidth = getFirstOrNull(attributes, MapAttributes::getBorderWidth);

        return new MapAttributesImpl(
                priority,
                level,
                label,
                description,
                labelVisibility,
                labelColor,
                labelShadow,
                icon,
                iconVisibility,
                iconColor,
                hasMarker,
                markerOptions,
                fillColor,
                borderColor,
                borderWidth);
    }

    private static <T, R> Optional<R> firstPresent(List<T> items, Function<T, Optional<R>> extractor) {
        return items.stream()
                .map(extractor)
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(Function.identity());
    }

    private static <T, R> R getFirstOrNull(List<T> items, Function<T, Optional<R>> extractor) {
        return firstPresent(items, extractor).orElse(null);
    }

    private static MapVisibilityImpl mergeVisibilities(
            List<MapAttributes> attributes, Function<MapAttributes, Optional<MapVisibility>> getter) {
        List<MapVisibility> visibilities = attributes.stream()
                .map(getter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (visibilities.isEmpty()) {
            return null;
        }

        Float min = getFirstOrNull(visibilities, MapVisibility::getMin);
        Float max = getFirstOrNull(visibilities, MapVisibility::getMax);
        Float fade = getFirstOrNull(visibilities, MapVisibility::getFade);

        return new MapVisibilityImpl(min, max, fade);
    }

    private static MapMarkerOptionsImpl mergeMarkerOptions(List<MapAttributes> attributes) {
        List<MapMarkerOptions> options = attributes.stream()
                .map(MapAttributes::getMarkerOptions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (options.isEmpty()) {
            return null;
        }

        Float minDistance = getFirstOrNull(options, MapMarkerOptions::getMinDistance);
        Float maxDistance = getFirstOrNull(options, MapMarkerOptions::getMaxDistance);
        Float fade = getFirstOrNull(options, MapMarkerOptions::getFade);
        CustomColor beaconColor = getFirstOrNull(options, MapMarkerOptions::getBeaconColor);
        Boolean hasLabel = getFirstOrNull(options, MapMarkerOptions::getHasLabel);
        Boolean hasDistanceLabel = getFirstOrNull(options, MapMarkerOptions::getHasDistanceLabel);
        Boolean hasIcon = getFirstOrNull(options, MapMarkerOptions::getHasIcon);

        return new MapMarkerOptionsImpl(
                minDistance, maxDistance, fade, beaconColor, hasLabel, hasDistanceLabel, hasIcon);
    }
}
