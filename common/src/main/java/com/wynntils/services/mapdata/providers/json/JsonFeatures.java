/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.features.impl.MapAreaImpl;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record JsonFeatures(List<MapLocationImpl> locations, List<MapAreaImpl> areas, List<MapPathImpl> paths) {
    public Stream<MapFeature> stream() {
        return Stream.of(locations, areas, paths).filter(Objects::nonNull).flatMap(List::stream);
    }

    public boolean validate() {
        return locations.stream().allMatch(MapLocationImpl::validate)
                && areas.stream().allMatch(MapAreaImpl::validate)
                && paths.stream().allMatch(MapPathImpl::validate);
    }
}
