/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.type;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import com.wynntils.utils.type.BoundingShape;
import java.util.List;
import java.util.Optional;

public interface MapFeature<A extends MapAttributes> extends MapDataProvidedType {
    // The id should be unique, and track the provenance of the feature
    String getFeatureId();

    String getCategoryId();

    Optional<A> getAttributes();

    boolean isVisible(BoundingShape boundingShape);

    List<String> getTags();
}
