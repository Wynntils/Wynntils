/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.type;

import com.wynntils.models.elements.type.Powder;

public record PowderSpecialInfo(float charge, Powder powder) {
    // The default for when no powder special is active
    public static final PowderSpecialInfo EMPTY = new PowderSpecialInfo(0, Powder.EARTH);
    // When the powder special has just been used and is in the brief state where the bar is visible with no progress
    public static final PowderSpecialInfo NO_CHARGE = new PowderSpecialInfo(0, null);
}
