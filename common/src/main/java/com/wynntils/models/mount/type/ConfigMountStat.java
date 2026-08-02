/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import java.util.Optional;

public enum ConfigMountStat {
    ACCELERATION("acceleration", MountStat.ACCELERATION),
    ALTITUDE("altitude", MountStat.ALTITUDE),
    JUMP_HEIGHT("jumpHeight", MountStat.JUMP_HEIGHT),
    ENERGY("energy", MountStat.ENERGY),
    HANDLING("handling", MountStat.HANDLING),
    POTENTIAL("potential", null),
    BOOST("boost", MountStat.BOOST),
    SPEED("speed", MountStat.SPEED),
    TOUGHNESS("toughness", MountStat.TOUGHNESS),
    TRAINING("training", MountStat.TRAINING);

    private final String key;
    private final MountStat mountStat;

    ConfigMountStat(String key, MountStat mountStat) {
        this.key = key;
        this.mountStat = mountStat;
    }

    public static Optional<ConfigMountStat> fromKey(String key) {
        for (ConfigMountStat stat : values()) {
            if (stat.key.equalsIgnoreCase(key)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    public MountStat getMountStat() {
        return mountStat;
    }
}
