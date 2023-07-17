/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.MapFeature;
import java.util.Collection;

public class BuiltInProvider implements MapDataProvider {
    // can be of many types...

    @Override
    public Collection<MapFeature> getFeatures() {
        return null;
    }
}
