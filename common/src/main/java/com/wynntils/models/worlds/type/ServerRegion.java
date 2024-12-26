/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

public enum ServerRegion {
    WC,
    NA,
    SA,
    AF,
    EU,
    AS,
    AU;

    public static ServerRegion fromString(String text) {
        for (ServerRegion type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }

        return WC;
    }
}
