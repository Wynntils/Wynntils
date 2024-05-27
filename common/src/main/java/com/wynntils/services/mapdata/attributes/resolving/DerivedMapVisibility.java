/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import java.util.Optional;
import java.util.function.Function;

public abstract class DerivedMapVisibility implements MapVisibility {
    protected abstract Optional<Float> getVisibilityFor(Function<MapVisibility, Optional<Float>> getter);

    @Override
    public Optional<Float> getMin() {
        return getVisibilityFor(MapVisibility::getMin);
    }

    @Override
    public Optional<Float> getMax() {
        return getVisibilityFor(MapVisibility::getMax);
    }

    @Override
    public Optional<Float> getFade() {
        return getVisibilityFor(MapVisibility::getFade);
    }
}
