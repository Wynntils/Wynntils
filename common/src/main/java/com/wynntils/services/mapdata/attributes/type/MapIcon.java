/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import net.minecraft.resources.Identifier;

public interface MapIcon {
    String NO_ICON_ID = "none";

    String getIconId();

    Identifier getIdentifier();

    int getWidth();

    int getHeight();
}
