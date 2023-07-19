/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.stream.Stream;

public interface MapDataProvider {
    Stream<MapFeature> getFeatures();

    Stream<MapFeatureCategory> getCategories();

    Stream<MapFeatureIcon> getIcons();
}
