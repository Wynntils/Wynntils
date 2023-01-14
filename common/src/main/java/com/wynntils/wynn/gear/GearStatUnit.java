/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

public enum GearStatUnit {
    RAW(null),
    PERCENT("%"),
    PER_3_S("/3s"),
    PER_5_S("/5s"),
    TIER(" tier");

    GearStatUnit(String string) {}
}
