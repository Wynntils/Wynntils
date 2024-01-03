/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum DataTransformerType {
    START_DATA_TRANSFORMER(0),
    TYPE_DATA_TRANSFORMER(1),
    NAME_DATA_TRANSFORMER(2),
    IDENTIFICATION_DATA_TRANSFORMER(3),
    POWDER_DATA_TRANSFORMER(4),
    REROLL_DATA_TRANSFORMER(5),
    SHINY_DATA_TRANSFORMER(6),
    CUSTOM_GEAR_TYPE_TRANSFORMER(7),
    DURABILITY_DATA_TRANSFORMER(8),
    REQUIREMENTS_DATA_TRANSFORMER(9),
    DAMAGE_DATA_TRANSFORMER(10),
    DEFENSE_DATA_TRANSFORMER(11),
    CUSTOM_IDENTIFICATION_DATA_TRANSFORMER(12),
    CUSTOM_CONSUMABLE_TYPE_DATA_TRANSFORMER(13),
    USES_DATA_TRANSFORMER(14),
    EFFECTS_DATA_TRANSFORMER(15),
    END_DATA_TRANSFORMER(255);

    private final byte id;

    DataTransformerType(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}
