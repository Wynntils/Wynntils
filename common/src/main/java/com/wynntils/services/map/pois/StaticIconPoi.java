/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.utils.mc.type.PoiLocation;

public abstract class StaticIconPoi extends IconPoi {
    protected final PoiLocation location;

    protected StaticIconPoi(PoiLocation location) {
        this.location = location;
    }

    @Override
    public boolean hasStaticLocation() {
        return true;
    }

    @Override
    public PoiLocation getLocation() {
        return location;
    }
}
