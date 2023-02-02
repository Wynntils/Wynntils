/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

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

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }
}
