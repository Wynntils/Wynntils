/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.stream.Stream;

public abstract class BuiltInProvider implements MapDataProvider {
    @Override
    public Stream<MapFeature> getFeatures() {
        return Stream.empty();
    }

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return Stream.empty();
    }

    @Override
    public Stream<MapFeatureIcon> getIcons() {
        return Stream.empty();
    }
}
