/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

public class WaypointPoi extends IconPoi {
    public WaypointPoi(MapLocation location) {
        super(location);
    }

    @Override
    public Texture getIcon() {
        return Texture.WAYPOINT;
    }

    @Override
    public String getName() {
        return "Waypoint";
    }
}
