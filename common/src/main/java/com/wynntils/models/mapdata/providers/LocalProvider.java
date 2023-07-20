/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.providers.json.JsonProviderLoader;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureIcon;
import com.wynntils.models.mapdata.type.features.MapFeature;
import java.util.stream.Stream;

public class LocalProvider implements MapDataProvider {
    // per-account, per-character or shared
    // can be added just from disk, or downloaded from an url

    JsonProviderLoader providerLoader = new JsonProviderLoader();

    @Override
    public Stream<MapFeature> getFeatures() {
        return providerLoader.getProvider().getFeatures();
    }

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return providerLoader.getProvider().getCategories();
    }

    @Override
    public Stream<MapFeatureIcon> getIcons() {
        return providerLoader.getProvider().getIcons();
    }
}
