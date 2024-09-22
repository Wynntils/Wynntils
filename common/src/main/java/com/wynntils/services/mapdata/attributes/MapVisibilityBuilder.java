/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import java.util.Optional;

public class MapVisibilityBuilder implements MapVisibility {
    private Float min = null;
    private Float max = null;
    private Float fade = null;

    public MapVisibilityBuilder withMin(float min) {
        this.min = min;
        return this;
    }

    public MapVisibilityBuilder withMax(float max) {
        this.max = max;
        return this;
    }

    public MapVisibilityBuilder withFade(float fade) {
        this.fade = fade;
        return this;
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
