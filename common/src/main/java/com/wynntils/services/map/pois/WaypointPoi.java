/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
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
    public float getMinZoomForRender() {
        return -1f;
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
