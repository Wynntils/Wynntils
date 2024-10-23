/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features;

import com.wynntils.services.mapdata.providers.json.MapLocationImpl;
import com.wynntils.services.mapdata.providers.json.MapLocationAttributesImpl;
import com.wynntils.utils.mc.type.Location;
import java.util.Locale;

public final class WaypointLocation extends MapLocationImpl {
    public WaypointLocation(Location location, String label, String subcategory, MapLocationAttributesImpl attributes) {
        super(
                "waypoint" + "-" + label.toLowerCase(Locale.ROOT).replaceAll("\\s", "-") + "-" + location.hashCode(),
                "wynntils:personal:waypoint" + (subcategory.isEmpty() ? "" : ":" + subcategory),
                attributes,
                location);
    }
}
