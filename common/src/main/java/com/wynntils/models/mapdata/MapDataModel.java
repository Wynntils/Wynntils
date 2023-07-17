/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Model;
import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.WaypointProvider;
import com.wynntils.models.mapdata.type.MapFeature;
import java.util.List;
import java.util.stream.Stream;

public class MapDataModel extends Model {
    private final List<MapDataProvider> providers = List.of(new WaypointProvider());

    public MapDataModel() {
        super(List.of());
    }

    public Stream<MapFeature> getFeatures() {
        Stream<MapFeature> allFeatures = providers.stream().flatMap(MapDataProvider::getFeatures);
        return allFeatures;
    }
}
