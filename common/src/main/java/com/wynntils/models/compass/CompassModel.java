/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.compass;

import com.wynntils.core.components.Model;
import com.wynntils.models.compass.type.CompassInfo;
import com.wynntils.models.compass.type.CompassProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CompassModel extends Model {
    public static final UserWaypointCompassProvider USER_WAYPOINTS_PROVIDER = new UserWaypointCompassProvider();

    private final List<CompassProvider> compassProviders = new ArrayList<>();

    public CompassModel() {
        super(List.of());

        registerCompassProvider(USER_WAYPOINTS_PROVIDER);
    }

    public void registerCompassProvider(CompassProvider provider) {
        compassProviders.add(provider);
    }

    public Stream<CompassInfo> getAllCompassInfos() {
        return compassProviders.stream().filter(CompassProvider::isEnabled).flatMap(CompassProvider::getCompassInfos);
    }
}
