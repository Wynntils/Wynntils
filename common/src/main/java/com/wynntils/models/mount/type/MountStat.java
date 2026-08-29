/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.EnumUtils;
import java.util.Optional;

public enum MountStat {
    SPEED("speed", 0),
    ACCELERATION("acceleration", 1),
    JUMP_HEIGHT("jumpHeight", 2),
    ALTITUDE("altitude", 3),
    ENERGY("energy", 4),
    HANDLING("handling", 5),
    TOUGHNESS("toughness", 6),
    BOOST("boost", 7),
    TRAINING("training", 8);

    private final String name;
    private final String key;
    private final int encodingId;

    MountStat(String key, int encodingId) {
        this.name = EnumUtils.toNiceString(name());
        this.key = key;
        this.encodingId = encodingId;
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

    public static MountStat fromEncodingId(int encodingId) {
        for (MountStat stat : values()) {
            if (stat.encodingId == encodingId) {
                return stat;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
