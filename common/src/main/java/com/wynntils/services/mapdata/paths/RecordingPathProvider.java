/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import java.util.stream.Stream;

public final class RecordingPathProvider extends BuiltInProvider {
    private MapPath path;

    @Override
    public String getProviderId() {
        return "path-recording";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return path == null ? Stream.empty() : Stream.of(path);
    }

    public void update(MapPath updated) {
        if (path != null) {
            notifyCallbacks(path);
        }

        path = updated;
        if (path != null) {
            notifyCallbacks(path);
        }
    }

    @Override
    public void reloadData() {}
}
