/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.overlays;

import com.wynntils.core.Reference;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.overlays.objects.OverlayHolder;
import com.wynntils.core.overlays.objects.OverlayPosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Managing class for Features that have an Overlay annotation
 *
 * Used to store all the overlays
 */
public class OverlayManager {
    private static final List<OverlayHolder> OVERLAYS = new ArrayList<>();

    public static void registerOverlay(OverlayHolder holder) {
        if (holder.getType() != OverlayPosition.class) {
            Reference.LOGGER.error(
                    "Overlay annotation can only be on a field with type OverlayPosition, not" + holder.getType());
            return;
        }

        OVERLAYS.add(holder);
        ConfigManager.registerHolder(holder);
    }

    public static List<OverlayHolder> getOverlays() {
        return OVERLAYS;
    }
}
