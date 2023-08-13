/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker.type;

import com.wynntils.utils.mc.type.Location;
import java.util.function.Supplier;

public class DynamicLocationSupplier implements LocationSupplier {
    private final Supplier<Location> supplier;

    public DynamicLocationSupplier(Supplier<Location> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Location getLocation() {
        return supplier.get();
    }
}
