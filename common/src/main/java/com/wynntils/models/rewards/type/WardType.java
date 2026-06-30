/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.colors.CustomColor;

public enum WardType {
    PURPLE(CustomColor.fromHexString("9a21bf")),
    BLUE(CustomColor.fromHexString("6977c1")),
    RED(CustomColor.fromHexString("f02e2e")),
    YELLOW(CustomColor.fromHexString("e0bf4b")),
    GREEN(CustomColor.fromHexString("94b937")),
    ORANGE(CustomColor.fromHexString("db7242")),
    PINK(CustomColor.fromHexString("d56ea5"));

    private final CustomColor color;

    WardType(CustomColor color) {
        this.color = color;
    }

    public CustomColor getColor() {
        return color;
    }
}
