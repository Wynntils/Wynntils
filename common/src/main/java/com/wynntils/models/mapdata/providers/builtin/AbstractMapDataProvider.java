/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.stream.Stream;

public abstract class AbstractMapDataProvider implements MapDataProvider {
    @Override
    public Stream<MapFeature> getFeatures() {
        return Stream.empty();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return Stream.empty();
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return Stream.empty();
    }
}
