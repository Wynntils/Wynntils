/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.colors.CustomColor;

public enum RuneType {
    AZ(CustomColor.fromInt(0xb0f3fc)),
    NII(CustomColor.fromInt(0xfdb9fe)),
    UTH(CustomColor.fromInt(0xbacefb)),
    TOL(CustomColor.fromInt(0xb8fcb3)),
    EK(CustomColor.fromInt(0xffd39e));

    private CustomColor color;

    RuneType(CustomColor color) {
        this.color = color;
    }

    public CustomColor getColor() {
        return color;
    }
}
