/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.entities;

import com.wynntils.utils.colors.CustomColor;

public interface WynntilsCustomGlowEntityProperty {
    CustomColor getGlowColor();

    void setGlowColor(CustomColor color);

    default int getGlowColorInt() {
        return getGlowColor().asInt();
    }
}
