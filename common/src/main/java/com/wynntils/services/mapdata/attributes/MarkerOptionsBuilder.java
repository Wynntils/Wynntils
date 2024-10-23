/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.services.mapdata.attributes.type.MarkerOptions;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public class MarkerOptionsBuilder implements MarkerOptions {
    private Float minDistance = null;
    private Float maxDistance = null;
    private Float fade = null;
    private CustomColor beaconColor = null;
    private Boolean hasLabel = null;
    private Boolean hasDistance = null;
    private Boolean hasIcon = null;

    public MarkerOptionsBuilder withMinDistance(float minDistance) {
        this.minDistance = minDistance;
        return this;
    }

    public MarkerOptionsBuilder withMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public MarkerOptionsBuilder withFade(float fade) {
        this.fade = fade;
        return this;
    }

    public MarkerOptionsBuilder withBeaconColor(CustomColor beaconColor) {
        this.beaconColor = beaconColor;
        return this;
    }

    public MarkerOptionsBuilder withHasLabel(boolean hasLabel) {
        this.hasLabel = hasLabel;
        return this;
    }

    public MarkerOptionsBuilder withHasDistance(boolean hasDistance) {
        this.hasDistance = hasDistance;
        return this;
    }

    public MarkerOptionsBuilder withHasIcon(boolean hasIcon) {
        this.hasIcon = hasIcon;
        return this;
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
