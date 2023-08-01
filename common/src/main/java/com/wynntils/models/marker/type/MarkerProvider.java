/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker.type;

import java.util.stream.Stream;

public interface MarkerProvider {
    Stream<MarkerInfo> getMarkerInfos();

    boolean isEnabled();
}
