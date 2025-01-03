/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public abstract class AbstractMapLocationAttributes extends AbstractMapAttributes implements MapLocationAttributes {
    @Override
    public final Optional<CustomColor> getFillColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<CustomColor> getBorderColor() {
        return Optional.empty();
    }

    @Override
    public final Optional<Float> getBorderWidth() {
        return Optional.empty();
    }
}
