/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;
import java.util.function.Function;

public abstract class DerivedAttributes implements MapAttributes {
    protected abstract <T> T getAttribute(Function<MapAttributes, T> getter);

    @Override
    public Optional<String> getLabel() {
        return getAttribute(MapAttributes::getLabel);
    }

    @Override
    public Optional<String> getIconId() {
        return getAttribute(MapAttributes::getIconId);
    }

    @Override
    public Optional<Integer> getPriority() {
        Optional<Integer> integer = getAttribute(MapAttributes::getPriority);
        return integer.isEmpty() ? Optional.of(0) : integer;
    }

    @Override
    public Optional<Integer> getLevel() {
        Optional<Integer> integer = getAttribute(MapAttributes::getLevel);
        return integer.isEmpty() ? Optional.of(0) : integer;
    }

    @Override
    public Optional<CustomColor> getLabelColor() {
        return getAttribute(MapAttributes::getLabelColor);
    }

    @Override
    public Optional<TextShadow> getLabelShadow() {
        return getAttribute(MapAttributes::getLabelShadow);
    }

    @Override
    public Optional<CustomColor> getIconColor() {
        return getAttribute(MapAttributes::getIconColor);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        return getAttribute(MapAttributes::getIconDecoration);
    }
}
