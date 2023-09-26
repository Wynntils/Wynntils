/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker.type;

import com.wynntils.services.map.pois.Poi;
import java.util.stream.Stream;

public interface MarkerProvider<T extends Poi> {
    /**
     * Returns a stream of all the marker infos for this provider.
     * The stream needs to be thread safe.
     * @return the stream of marker infos
     */
    Stream<MarkerInfo> getMarkerInfos();

    /**
     * Returns a stream of all the pois for this provider.
     * The stream needs to be thread safe.
     * @return the stream of pois
     */
    Stream<T> getPois();

    /**
     * Returns whether this provider is enabled.
     * @return whether this provider is enabled
     */
    boolean isEnabled();
}
