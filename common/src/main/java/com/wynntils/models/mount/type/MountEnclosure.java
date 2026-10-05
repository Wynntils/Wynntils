/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public enum MountEnclosure {
    /** Horses */
    TERNAVES_RANCH("Ternaves Ranch", CustomColor.fromHexString("A95D30")),
    /** Wyverns */
    BANTISU_QUARTERS("Bantisu Quarters", CommonColors.LIGHT_GRAY),
    /** Adasaurs */
    ALDWELL_SANCTUARY("Aldwell Sanctuary", CustomColor.fromHexString("E0422B"));

    private final String name;
    private final CustomColor color;

    MountEnclosure(String name, CustomColor color)
    {
        this.name = name;
        this.color = color;
    }

    public String getName()
    {
        return name;
    }

    public CustomColor getColor()
    {
        return color;
    }
}