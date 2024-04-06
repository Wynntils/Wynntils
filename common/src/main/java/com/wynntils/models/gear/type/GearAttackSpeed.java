/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.WynntilsMod;
import java.util.Locale;

public enum GearAttackSpeed {
    SUPER_FAST("Super Fast Attack Speed", 3),
    VERY_FAST("Very Fast Attack Speed", 2),
    FAST("Fast Attack Speed", 1),
    NORMAL("Normal Attack Speed", 0),
    SLOW("Slow Attack Speed", -1),
    VERY_SLOW("Very Slow Attack Speed", -2),
    SUPER_SLOW("Super Slow Attack Speed", -3);

    private final String name;
    private final int offset;

    GearAttackSpeed(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public static GearAttackSpeed fromString(String str) {
        if (str == null) return null;
        try {
            return GearAttackSpeed.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            WynntilsMod.warn("Invalid gear attack speed: " + str);
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }
}
