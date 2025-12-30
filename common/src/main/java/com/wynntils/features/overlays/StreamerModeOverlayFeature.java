/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.StreamerModeOverlay;

@ConfigCategory(Category.OVERLAYS)
public class StreamerModeOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final StreamerModeOverlay streamerModeOverlay = new StreamerModeOverlay();

    public StreamerModeOverlayFeature() {
        super(ProfileDefault.DISABLED);
    }
}
