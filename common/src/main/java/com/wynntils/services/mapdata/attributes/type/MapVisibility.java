/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.services.mapdata.attributes.MapVisibilityBuilder;
import java.util.Optional;

public interface MapVisibility {
    Optional<Float> getMin();

    Optional<Float> getMax();

    Optional<Float> getFade();

    static MapVisibilityBuilder builder() {
        return new MapVisibilityBuilder();
    }
}
