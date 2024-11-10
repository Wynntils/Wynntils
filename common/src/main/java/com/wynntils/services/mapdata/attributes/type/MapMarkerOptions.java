/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public interface MapMarkerOptions {
    // The marker starts to fade in when the player is within the outer radius (- fade),
    // and starts to fade out when approaching the inner radius (- fade)

    // The inner radius for the marker visibility circle
    // If inside the inner radius, the marker starts to fade out
    Optional<Float> getMinDistance();

    // The outer radius for the marker visibility circle
    // If outside the outer radius, the marker is invisible
    Optional<Float> getMaxDistance();

    // The fade distance for the marker visibility circles
    Optional<Float> getFade();

    // The color of the beacon beam
    // If empty, or alpha is 0, no beacon beam will be rendered
    Optional<CustomColor> getBeaconColor();

    // Whether to render the label
    Optional<Boolean> getHasLabel();

    // Whether to render the distance (below the label)
    Optional<Boolean> getHasDistanceLabel();

    // Whether to render the icon
    Optional<Boolean> getHasIcon();
}
