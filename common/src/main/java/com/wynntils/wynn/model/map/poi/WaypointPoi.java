/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

public class WaypointPoi extends IconPoi {

    private final PointerPoi pointer;

    public WaypointPoi(MapLocation location) {
        super(location);
        pointer = new PointerPoi(location);
    }

    public PointerPoi getPointerPoi() {
        return pointer;
    }

    @Override
    public Texture getIcon() {
        return Texture.WAYPOINT;
    }

    @Override
    public String getName() {
        return "Waypoint";
    }

    public static class PointerPoi extends IconPoi {

        public PointerPoi(MapLocation location) {
            super(location);
        }

        @Override
        public Texture getIcon() {
            return Texture.POINTER;
        }

        @Override
        public String getName() {
            return "Waypoint";
        }
    }
}
