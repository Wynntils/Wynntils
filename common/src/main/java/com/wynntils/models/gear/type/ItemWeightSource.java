/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public enum ItemWeightSource {
    NONE(CustomColor.NONE),
    WYNNPOOL(CommonColors.WYNNCRAFT_ORANGE),
    NORI(CommonColors.WYNNCRAFT_AQUA),
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
