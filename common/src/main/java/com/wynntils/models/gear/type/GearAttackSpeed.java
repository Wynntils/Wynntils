/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.WynntilsMod;

public enum GearAttackSpeed {
    SUPER_FAST("Super Fast", "superFast", 0),
    VERY_FAST("Very Fast", "veryFast", 1),
    FAST("Fast", "fast", 2),
    NORMAL("Normal", "normal", 3),
    SLOW("Slow", "slow", 4),
    VERY_SLOW("Very Slow", "verySlow", 5),
    SUPER_SLOW("Super Slow", "superSlow", 6);

    private final String name;
    private final String apiName;
    private final int encodingId;

    GearAttackSpeed(String name, String apiName, int encodingId) {
        this.name = name;
        this.apiName = apiName;
        this.encodingId = encodingId;
    }

    public static GearAttackSpeed fromString(String str) {
        if (str == null) return null;

        for (GearAttackSpeed attackSpeed : values()) {
            if (attackSpeed.name.equals(str) || attackSpeed.apiName.equals(str)) {
                return attackSpeed;
            }
        }

        WynntilsMod.warn("Invalid gear attack speed: " + str);
        return null;
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

    public String getApiName() {
        return apiName;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
