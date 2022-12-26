/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

public abstract class StaticIconPoi extends IconPoi {
    PoiLocation location;

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
