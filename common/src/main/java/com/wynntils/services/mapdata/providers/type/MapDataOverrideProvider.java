/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.type;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface MapDataOverrideProvider {
    /**
     * Get the attributes that are overriden by this provider.
     *
     * @param mapFeature The map feature to get the attributes for.
     * @return The attributes that are overriden by this provider.
     */
    MapAttributes getOverrideAttributes(MapFeature mapFeature);

    /**
     * Get the feature ids that are overriden by this provider.
     *
     * @return The feature ids that are overriden by this provider.
     */
    Stream<String> getOverridenFeatureIds();

    /**
     * Get the category ids that are overriden by this provider.
     *
     * @return The category ids that are overriden by this provider.
     */
    Stream<String> getOverridenCategoryIds();

    void onChange(Consumer<MapDataProvidedType> callback);
}
