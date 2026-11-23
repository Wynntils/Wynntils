/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.PrismStyle;
import java.util.List;
import java.util.Optional;

public abstract class AbstractMapLocationAttributes extends AbstractMapAttributes implements MapLocationAttributes {
    @Override
    public Optional<List<CustomColor>> getFillColors() {
        return Optional.empty();
    }

    @Override
    public Optional<List<CustomColor>> getBorderColors() {
        return Optional.empty();
    }

    @Override
    public final Optional<Float> getBorderWidth() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBorderRenderInWorld() {
        return Optional.empty();
    }

    @Override
    public Optional<CustomColor> getBorderWorldColor() {
        return Optional.empty();
    }

    @Override
    public Optional<Float> getBorderWorldWidth() {
        return Optional.empty();
    }

    @Override
    public Optional<PrismStyle> getBorderWorldStyle() {
        return Optional.empty();
    }
}
