/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.type;

import com.wynntils.services.mapdata.features.type.MapIcon;
import com.wynntils.services.mapdata.features.type.MapCategory;
import com.wynntils.services.mapdata.features.type.MapDataProvidedType;
import com.wynntils.services.mapdata.features.type.MapFeature;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface MapDataProvider {
    Stream<MapFeature> getFeatures();

    Stream<MapCategory> getCategories();

    Stream<MapIcon> getIcons();

    void onChange(Consumer<MapDataProvidedType> callback);

    void reloadData();
}
