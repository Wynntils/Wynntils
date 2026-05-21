/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import java.util.Arrays;
import java.util.List;

public enum TrialType {
    // Order these the same way like https://wynncraft.wiki.gg/wiki/Lootrunning#Trials
    UNKNOWN("Unknown"),
    FAILED("Failed"),

    ADRENALINE_JUNKIE("Adrenaline Junkie"),
    DYING_LIGHT("Dying Light"),
    MONOCHROMOKOPIA("Monochromokopia"),
    CHRONOTRIGGER("Chronotrigger"),
    ALL_IN("All In"),
    GAMBLING_BEAST("Gambling Beast"),
    HUBRIS("Hubris"),
    LIGHTS_OUT("Lights Out"),
    SIDE_HUSTLE("Side Hustle"),
    TREASURY_BILL("Treasury Bill"),
    ULTIMATE_SACRIFICE("Ultimate Sacrifice"),
    WARMTH_DEVOURER("Warmth Devourer");

    private final String name;

    TrialType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TrialType fromName(String name) {
        for (TrialType type : values()) {
            if (type == UNKNOWN || type == FAILED) continue;
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    public static List<TrialType> trialTypes() {
        return Arrays.stream(values())
                .filter(type -> type != UNKNOWN && type != FAILED)
                .toList();
    }
}
