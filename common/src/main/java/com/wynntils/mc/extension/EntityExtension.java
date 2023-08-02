/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.wynntils.utils.colors.CustomColor;

public interface EntityExtension {
    CustomColor getGlowColor();

    void setGlowColor(CustomColor color);

    default int getGlowColorInt() {
        return getGlowColor().asInt();
    }

    boolean isRendered();

    void setRendered(boolean rendered);
}
