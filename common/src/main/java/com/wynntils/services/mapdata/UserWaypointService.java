/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.mapdata.providers.builtin.WaypointsProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserWaypointService extends Service {
    @Persisted
    private final Storage<List<WaypointsProvider.WaypointLocation>> userWaypoints = new Storage<>(new ArrayList<>());

    public UserWaypointService() {
        super(List.of());
    }

    public List<WaypointsProvider.WaypointLocation> getUserWaypoints() {
        return Collections.unmodifiableList(userWaypoints.get());
    }

    public void addUserWaypoint(WaypointsProvider.WaypointLocation waypoint) {
        userWaypoints.get().add(waypoint);
        userWaypoints.touched();
        // FIXME: Add userWaypoints.updateWaypoints();
    }

    public void removeUserWaypoint(WaypointsProvider.WaypointLocation waypoint) {
        userWaypoints.get().remove(waypoint);
        userWaypoints.touched();
        // FIXME: Add userWaypoints.updateWaypoints();
    }
}
