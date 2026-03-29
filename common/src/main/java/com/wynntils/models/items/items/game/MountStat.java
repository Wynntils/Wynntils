/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import java.util.Optional;

public enum MountStat {
    ACCELERATION("acceleration"),
    ALTITUDE("altitude"),
    ENERGY("energy"),
    HANDLING("handling"),
    POWERUP("powerup"),
    SPEED("speed"),
    TOUGHNESS("toughness"),
    TRAINING("training");

    private final String key;

    MountStat(String key) {
        this.key = key;
    }

    public static Optional<MountStat> fromKey(String key) {
        for (MountStat stat : values()) {
            if (stat.key.equalsIgnoreCase(key)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }
}
