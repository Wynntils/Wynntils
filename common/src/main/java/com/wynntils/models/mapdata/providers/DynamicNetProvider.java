/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.style.MapIcon;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.MapFeature;
import java.net.URI;
import java.util.stream.Stream;

public class DynamicNetProvider implements MapDataProvider {
    private final URI uri;

    public DynamicNetProvider(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return null;
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return null;
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return null;
    }
}
