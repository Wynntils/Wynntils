package com.wynntils.models.mapdata.providers;

import com.wynntils.models.mapdata.MapFeature;
import java.net.URI;
import java.util.Collection;

public class DynamicNetProvider implements MapDataProvider {
    private final URI uri;

    public DynamicNetProvider(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public Collection<MapFeature> getFeatures() {
        return null;
    }
}
