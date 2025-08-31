/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.core.text.StyledText;

public interface BeaconMarkerKind {
    boolean matches(StyledText styledText);
}
