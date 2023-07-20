/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type.attributes;

public enum MapFeatureVisibility {
    ALWAYS,
    NEVER

    // FIXME: This needs to be more complex, and contain (at least):
    // always
    // never
    // below zoom: first visible at, fully visible at
    // above zoom: fully visible at, last visible at
    // between zoom: both above
}
