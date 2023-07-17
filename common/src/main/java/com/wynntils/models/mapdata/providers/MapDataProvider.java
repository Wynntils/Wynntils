/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.MapFeature;
import java.util.Collection;

public interface MapDataProvider {
    Collection<MapFeature> getFeatures();
}
