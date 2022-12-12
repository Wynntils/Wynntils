/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;
import java.util.function.Supplier;

public class WaypointPoi extends DynamicIconPoi {

    private final PointerPoi pointer;

    public WaypointPoi(Supplier<PoiLocation> locationSupplier) {
        super(locationSupplier);
        pointer = new PointerPoi(locationSupplier);
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

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.NORMAL;
    }

    public static class PointerPoi extends DynamicIconPoi {

        public PointerPoi(Supplier<PoiLocation> locationSupplier) {
            super(locationSupplier);
        }

        @Override
        public Texture getIcon() {
            return Texture.POINTER;
        }

        @Override
        public String getName() {
            return "Waypoint";
        }

        @Override
        public DisplayPriority getDisplayPriority() {
            return DisplayPriority.NORMAL;
        }
    }
}
