/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.EnumUtils;
import java.util.Optional;

public enum MountStat {
    ACCELERATION("acceleration"),
    ALTITUDE("altitude"),
    JUMP_HEIGHT("jumpHeight"),
    ENERGY("energy"),
    HANDLING("handling"),
    BOOST("boost"),
    SPEED("speed"),
    TOUGHNESS("toughness"),
    TRAINING("training");

    private final String name;
    private final String key;

    MountStat(String key) {
        this.name = EnumUtils.toNiceString(name());
        this.key = key;
    }

    public static Optional<MountStat> fromName(String name) {
        for (MountStat stat : values()) {
            if (stat.name.equalsIgnoreCase(name)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    public static Optional<MountStat> fromKey(String key) {
        for (MountStat stat : values()) {
            if (stat.key.equalsIgnoreCase(key)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    public String getName() {
        return name;
    }
}
