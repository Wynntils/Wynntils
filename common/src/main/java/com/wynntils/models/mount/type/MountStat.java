/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.EnumUtils;
import java.util.Optional;

public enum MountStat {
    ACCELERATION,
    ALTITUDE,
    JUMP_HEIGHT,
    ENERGY,
    HANDLING,
    BOOST,
    SPEED,
    TOUGHNESS,
    TRAINING;

    private final String name;

    MountStat() {
        this.name = EnumUtils.toNiceString(name());
    }

    public static Optional<MountStat> fromName(String key) {
        for (MountStat stat : values()) {
            if (stat.name.equalsIgnoreCase(key)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    public String getName() {
        return name;
    }
}
