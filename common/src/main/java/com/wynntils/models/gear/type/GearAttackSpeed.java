/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.WynntilsMod;
import java.util.Locale;

public enum GearAttackSpeed {
    SUPER_FAST("Super Fast Attack Speed", 0),
    VERY_FAST("Very Fast Attack Speed", 1),
    FAST("Fast Attack Speed", 2),
    NORMAL("Normal Attack Speed", 3),
    SLOW("Slow Attack Speed", 4),
    VERY_SLOW("Very Slow Attack Speed", 5),
    SUPER_SLOW("Super Slow Attack Speed", 6);

    private final String name;
    private final int encodingId;

    GearAttackSpeed(String name, int encodingId) {
        this.name = name;
        this.encodingId = encodingId;
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

    public static GearAttackSpeed fromEncodingId(int id) {
        for (GearAttackSpeed attackSpeed : values()) {
            if (attackSpeed.encodingId == id) {
                return attackSpeed;
            }
        }
        WynntilsMod.warn("Invalid gear attack speed encoding id: " + id);
        return null;
    }

    public String getName() {
        return name;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
