/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.compass.type;

import java.util.stream.Stream;

public interface CompassProvider {
    Stream<CompassInfo> getCompassInfos();

    boolean isEnabled();
}
