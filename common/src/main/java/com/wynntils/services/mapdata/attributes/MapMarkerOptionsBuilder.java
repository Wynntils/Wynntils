/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public class MapMarkerOptionsBuilder implements MapMarkerOptions {
    private Float minDistance = null;
    private Float maxDistance = null;
    private Float fade = null;
    private CustomColor beaconColor = null;
    private Boolean hasLabel = null;
    private Boolean hasDistance = null;
    private Boolean hasIcon = null;

    public MapMarkerOptionsBuilder withMinDistance(float minDistance) {
        this.minDistance = minDistance;
        return this;
    }

    public MapMarkerOptionsBuilder withMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public MapMarkerOptionsBuilder withFade(float fade) {
        this.fade = fade;
        return this;
    }

    public MapMarkerOptionsBuilder withBeaconColor(CustomColor beaconColor) {
        this.beaconColor = beaconColor;
        return this;
    }

    public MapMarkerOptionsBuilder withHasLabel(boolean hasLabel) {
        this.hasLabel = hasLabel;
        return this;
    }

    public MapMarkerOptionsBuilder withHasDistance(boolean hasDistance) {
        this.hasDistance = hasDistance;
        return this;
    }

    public MapMarkerOptionsBuilder withHasIcon(boolean hasIcon) {
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

    public MapMarkerOptionsImpl build() {
        return new MapMarkerOptionsImpl(minDistance, maxDistance, fade, beaconColor, hasLabel, hasDistance, hasIcon);
    }
}
