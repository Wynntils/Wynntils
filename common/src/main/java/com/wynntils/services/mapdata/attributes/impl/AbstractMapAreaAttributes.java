/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public abstract class AbstractMapAreaAttributes extends AbstractMapAttributes implements MapAreaAttributes {
    @Override
    public final Optional<String> getIconId() {
        return Optional.empty();
    }

    @Override
    public final Optional<CustomColor> getIconColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<MapVisibility> getIconVisibility() {
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
}
