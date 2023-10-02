/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.type;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import java.util.List;

public interface MapFeature {
    // Required. The id should be unique, and track the provenance of the feature
    String getFeatureId();

    // Required.
    String getCategoryId();

    // Optional
    MapAttributes getAttributes();

    // Optional
    List<String> getTags();
}
