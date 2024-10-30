/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public abstract class AbstractMapPathAttributes extends AbstractMapAttributes implements MapPathAttributes {
    @Override
    public final Optional<String> getIconId() {
        return Optional.empty();
    }

    @Override
    public final Optional<MapVisibility> getIconVisibility() {
        return Optional.empty();
    }

    @Override
    public final Optional<CustomColor> getIconColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<MapDecoration> getIconDecoration() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getHasMarker() {
        return Optional.empty();
    }

    @Override
    public final Optional<MapMarkerOptions> getMarkerOptions() {
        return Optional.empty();
    }

    @Override
    public final Optional<CustomColor> getFillColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<CustomColor> getBorderColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<Float> getBorderWidth() {
        return Optional.empty();
    }
}
