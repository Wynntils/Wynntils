/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

public enum ConsumableEffect {
    HEAL("§c❤", 0),
    MANA("§b✺", 1),
    DURATION("Seconds", 2);

    private final String suffix;
    private final int id;

    ConsumableEffect(String suffix, int id) {
        this.suffix = suffix;
        this.id = id;
    }

    public static ConsumableEffect fromString(String type) {
        for (ConsumableEffect value : values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }

    public static ConsumableEffect fromId(int id) {
        for (ConsumableEffect value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        return null;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getId() {
        return id;
    }
}
