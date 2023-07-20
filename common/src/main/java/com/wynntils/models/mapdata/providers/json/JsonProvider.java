/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.List;
import java.util.stream.Stream;

public class JsonProvider implements MapDataProvider {
    private List<MapFeature> features;
    private List<MapFeatureCategory> categories;
    private List<MapFeatureIcon> icons;

    @Override
    public Stream<MapFeature> getFeatures() {
        return features.stream();
    }

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return categories.stream();
    }

    @Override
    public Stream<MapFeatureIcon> getIcons() {
        return icons.stream();
    }
}
