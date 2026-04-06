/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import java.util.Optional;

public enum MountStat {
    ACCELERATION("acceleration", true),
    ALTITUDE("altitude", true),
    JUMP_HEIGHT("jumpHeight", true),
    ENERGY("energy", true),
    HANDLING("handling", true),
    POTENTIAL("potential", false),
    BOOST("boost", true),
    SPEED("speed", true),
    TOUGHNESS("toughness", true),
    TRAINING("training", true);

    private final String key;
    private final boolean capped;

    MountStat(String key, boolean capped) {
        this.key = key;
        this.capped = capped;
    }

    public static Optional<MountStat> fromKey(String key) {
        for (MountStat stat : values()) {
            if (stat.key.equalsIgnoreCase(key)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    public boolean isCapped() {
        return capped;
    }
}
