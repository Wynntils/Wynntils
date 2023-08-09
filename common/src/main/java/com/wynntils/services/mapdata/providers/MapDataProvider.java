/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers;

import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import java.util.stream.Stream;

public interface MapDataProvider {
    Stream<MapFeature> getFeatures();

    Stream<MapCategory> getCategories();

    Stream<MapIcon> getIcons();
}
