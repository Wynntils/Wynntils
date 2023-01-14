/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

public record GearDamageRange(int low, int high) {
    public static final GearDamageRange NONE = new GearDamageRange(0, 0);
}
