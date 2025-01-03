/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public class MapMarkerOptionsImpl implements MapMarkerOptions {
    private final Float minDistance;
    private final Float maxDistance;
    private final Float fade;
    private final CustomColor beaconColor;
    private final Boolean hasLabel;
    private final Boolean hasDistance;
    private final Boolean hasIcon;

    public MapMarkerOptionsImpl(
            Float minDistance,
            Float maxDistance,
            Float fade,
            CustomColor beaconColor,
            Boolean hasLabel,
            Boolean hasDistance,
            Boolean hasIcon) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.fade = fade;
        this.beaconColor = beaconColor;
        this.hasLabel = hasLabel;
        this.hasDistance = hasDistance;
        this.hasIcon = hasIcon;
    }

    @Override
    public Optional<Float> getMinDistance() {
        return Optional.ofNullable(minDistance);
    }

    @Override
    public Optional<Float> getMaxDistance() {
        return Optional.ofNullable(maxDistance);
    }

    @Override
    public Optional<Float> getFade() {
        return Optional.ofNullable(fade);
    }

    @Override
    public Optional<CustomColor> getBeaconColor() {
        return Optional.ofNullable(beaconColor);
    }

    @Override
    public Optional<Boolean> getHasLabel() {
        return Optional.ofNullable(hasLabel);
    }

    @Override
    public Optional<Boolean> getHasDistanceLabel() {
        return Optional.ofNullable(hasDistance);
    }

    @Override
    public Optional<Boolean> getHasIcon() {
        return Optional.ofNullable(hasIcon);
    }
}
