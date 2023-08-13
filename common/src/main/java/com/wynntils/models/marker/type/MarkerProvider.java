/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker.type;

import com.wynntils.services.map.pois.Poi;
import java.util.stream.Stream;

public interface MarkerProvider<T extends Poi> {
    Stream<MarkerInfo> getMarkerInfos();

    Stream<T> getPois();

    boolean isEnabled();
}
