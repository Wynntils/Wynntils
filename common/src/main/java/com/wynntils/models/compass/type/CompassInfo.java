/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.compass.type;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;

public record CompassInfo(LocationSupplier locationSupplier, Texture texture, CustomColor color) {
    public Location location() {
        return locationSupplier.getLocation();
    }
}
