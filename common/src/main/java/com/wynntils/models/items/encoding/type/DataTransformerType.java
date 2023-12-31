/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum DataTransformerType {
    START_DATA_TRANSFORMER((byte) 0),
    TYPE_DATA_TRANSFORMER((byte) 1),
    NAME_DATA_TRANSFORMER((byte) 2),
    IDENTIFICATION_DATA_TRANSFORMER((byte) 3),
    POWDER_DATA_TRANSFORMER((byte) 4),
    REROLL_DATA_TRANSFORMER((byte) 5),
    SHINY_DATA_TRANSFORMER((byte) 6),
    CUSTOM_GEAR_TYPE_TRANSFORMER((byte) 7),
    DURABILITY_DATA_TRANSFORMER((byte) 8),
    REQUIREMENTS_DATA_TRANSFORMER((byte) 9),
    DAMAGE_DATA_TRANSFORMER((byte) 10),
    DEFENSE_DATA_TRANSFORMER((byte) 11),
    CUSTOM_IDENTIFICATION_DATA_TRANSFORMER((byte) 12),
    CUSTOM_CONSUMABLE_TYPE_DATA_TRANSFORMER((byte) 13),
    USES_DATA_TRANSFORMER((byte) 14),
    EFFECTS_DATA_TRANSFORMER((byte) 15),
    END_DATA_TRANSFORMER((byte) 255);

    private final byte id;

    DataTransformerType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
