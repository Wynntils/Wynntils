/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.type;

import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface MapDataProvider {
    Stream<MapFeature> getFeatures();

    Stream<MapCategory> getCategories();

    Stream<MapIcon> getIcons();

    void onChange(Consumer<MapDataProvidedType> callback);

    void reloadData();
}
