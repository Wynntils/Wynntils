/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.utils.colors.CustomColor;

public enum ItemWeightSource {
    NONE(CustomColor.NONE),
    WYNNPOOL(CustomColor.fromInt(0xffc457)),
    NORI(CustomColor.fromInt(0x67ccf5)),
    ALL(CustomColor.NONE);

    private final CustomColor color;

    ItemWeightSource(CustomColor color) {
        this.color = color;
    }

    public boolean isSingleSource() {
        return this == WYNNPOOL || this == NORI;
    }

    public CustomColor getColor() {
        return color;
    }
}
