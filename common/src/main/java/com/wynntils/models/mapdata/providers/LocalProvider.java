/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.stream.Stream;

public class LocalProvider implements MapDataProvider {
    // per-account, per-character or shared
    // can be added just from disk, or downloaded from an url

    @Override
    public Stream<MapFeature> getFeatures() {
        return null;
    }

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return null;
    }

    @Override
    public Stream<MapFeatureIcon> getIcons() {
        return null;
    }
}
