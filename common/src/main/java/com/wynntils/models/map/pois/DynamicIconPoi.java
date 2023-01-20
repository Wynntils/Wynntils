/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.wynntils.models.map.PoiLocation;
import java.util.function.Supplier;

public abstract class DynamicIconPoi extends IconPoi {
    private final Supplier<PoiLocation> locationSupplier;

    protected DynamicIconPoi(Supplier<PoiLocation> locationSupplier) {
        this.locationSupplier = locationSupplier;
    }

    @Override
    public boolean hasStaticLocation() {
        return false;
    }

    @Override
    public PoiLocation getLocation() {
        return locationSupplier.get();
    }
}
