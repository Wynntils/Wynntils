/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import java.util.Optional;
import java.util.function.Function;

public abstract class DerivedMapVisibility implements MapVisibility {
    protected abstract Optional<Float> getValue(Function<MapVisibility, Optional<Float>> getter);

    @Override
    public Optional<Float> getMin() {
        return getValue(MapVisibility::getMin);
    }

    @Override
    public Optional<Float> getMax() {
        return getValue(MapVisibility::getMax);
    }

    @Override
    public Optional<Float> getFade() {
        return getValue(MapVisibility::getFade);
    }
}
