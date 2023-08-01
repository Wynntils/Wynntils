/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.compass;

import com.wynntils.models.compass.type.CompassInfo;
import com.wynntils.models.compass.type.CompassProvider;
import com.wynntils.models.compass.type.LocationSupplier;
import com.wynntils.models.compass.type.StaticLocationSupplier;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class UserWaypointCompassProvider implements CompassProvider {
    private final Set<CompassInfo> compassInfoSet = new LinkedHashSet<>();

    public void addLocation(Location location, Texture texture, CustomColor color) {
        addLocation(new CompassInfo(new StaticLocationSupplier(location), texture, color));
    }

    public void addLocation(Location location, Texture texture) {
        addLocation(new CompassInfo(new StaticLocationSupplier(location), texture, CommonColors.WHITE));
    }

    public void addLocation(Location location) {
        addLocation(new CompassInfo(new StaticLocationSupplier(location), Texture.WAYPOINT, CommonColors.WHITE));
    }

    public void addLocation(LocationSupplier locationSupplier) {
        compassInfoSet.add(new CompassInfo(locationSupplier, Texture.WAYPOINT, CommonColors.WHITE));
    }

    public void addLocation(CompassInfo compassInfo) {
        compassInfoSet.add(compassInfo);
    }

    public void removeLocation(Location location) {
        compassInfoSet.removeIf(info -> info.location().equals(location));
    }

    @Override
    public Stream<CompassInfo> getCompassInfos() {
        return compassInfoSet.stream();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
