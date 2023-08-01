/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker.type;

import com.wynntils.utils.mc.type.Location;

public class StaticLocationSupplier implements LocationSupplier {
    private final Location location;

    public StaticLocationSupplier(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
