/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// This is a special class of MapAttributes. It is used to combine multiple MapAttributes into one, provided
// by override providers. The order of priority as to which MapAttributes are used is determined by the original
// order of the MapAttributes passed to the constructor.
public final class OverrideMapAttributes implements MapAttributes {
    private final List<MapAttributes> attributes;

    private OverrideMapAttributes(List<MapAttributes> attributes) {
        this.attributes = attributes;
    }

    public static Optional<MapAttributes> from(List<MapAttributes> attributes) {
        return attributes == null || attributes.isEmpty()
                ? Optional.empty()
                : Optional.of(new OverrideMapAttributes(attributes));
    }

    public <T> Optional<T> resolveAttribute(Function<MapAttributes, Optional<T>> valueGetter) {
        for (MapAttributes attribute : attributes) {
            Optional<T> value = valueGetter.apply(attribute);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getPriority() {
        return resolveAttribute(MapAttributes::getPriority);
    }

    @Override
    public Optional<Integer> getLevel() {
        return resolveAttribute(MapAttributes::getLevel);
    }

    @Override
    public Optional<String> getLabel() {
        return resolveAttribute(MapAttributes::getLabel);
    }

    @Override
    public Optional<String> getSecondaryLabel() {
        return resolveAttribute(MapAttributes::getSecondaryLabel);
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return resolveAttribute(MapAttributes::getLabelVisibility);
    }

    @Override
    public Optional<CustomColor> getLabelColor() {
        return resolveAttribute(MapAttributes::getLabelColor);
    }

    @Override
    public Optional<TextShadow> getLabelShadow() {
        return resolveAttribute(MapAttributes::getLabelShadow);
    }

    @Override
    public Optional<String> getIconId() {
        return resolveAttribute(MapAttributes::getIconId);
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return resolveAttribute(MapAttributes::getIconVisibility);
    }

    @Override
    public Optional<CustomColor> getIconColor() {
        return resolveAttribute(MapAttributes::getIconColor);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        return resolveAttribute(MapAttributes::getIconDecoration);
    }

    @Override
    public Optional<Boolean> getHasMarker() {
        return resolveAttribute(MapAttributes::getHasMarker);
    }

    @Override
    public Optional<MapMarkerOptions> getMarkerOptions() {
        return resolveAttribute(MapAttributes::getMarkerOptions);
    }

    @Override
    public Optional<CustomColor> getFillColor() {
        return resolveAttribute(MapAttributes::getFillColor);
    }

    @Override
    public Optional<CustomColor> getBorderColor() {
        return resolveAttribute(MapAttributes::getBorderColor);
    }

    @Override
    public Optional<Float> getBorderWidth() {
        return resolveAttribute(MapAttributes::getBorderWidth);
    }
}
