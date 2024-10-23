/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import java.util.Optional;

public final class MapVisibilityImpl implements MapVisibility {
    private final Float min;
    private final Float max;
    private final Float fade;

    public MapVisibilityImpl(Float min, Float max, Float fade) {
        this.min = min;
        this.max = max;
        this.fade = fade;
    }

    public MapVisibilityImpl(MapVisibility visibility) {
        this(
                visibility.getMin().orElse(null),
                visibility.getMax().orElse(null),
                visibility.getFade().orElse(null));
    }

    @Override
    public Optional<Float> getMin() {
        return Optional.ofNullable(min);
    }

    @Override
    public Optional<Float> getMax() {
        return Optional.ofNullable(max);
    }

    @Override
    public Optional<Float> getFade() {
        return Optional.ofNullable(fade);
    }
}
