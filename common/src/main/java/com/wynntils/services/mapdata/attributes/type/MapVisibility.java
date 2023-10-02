/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

public enum MapVisibility {
    ALWAYS,
    NEVER

    // FIXME: This needs to be more complex, and contain (at least):
    // below zoom: first visible at, fully visible at
    // above zoom: fully visible at, last visible at
    // between zoom: both above
}
