/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import net.minecraft.resources.ResourceLocation;

public interface MapIcon {
    String NO_ICON_ID = "none";

    String getIconId();

    ResourceLocation getResourceLocation();

    int getWidth();

    int getHeight();
}
