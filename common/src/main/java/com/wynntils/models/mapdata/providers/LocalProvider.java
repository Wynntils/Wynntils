/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.type.MapFeature;
import java.util.Collection;

public class LocalProvider implements MapDataProvider {
    // per-account, per-character or shared
    // can be added just from disk, or downloaded from an url

    @Override
    public Collection<MapFeature> getFeatures() {
        return null;
    }
}
