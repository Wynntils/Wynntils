/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.ShamanMaskOverlay;

@ConfigCategory(Category.OVERLAYS)
public class ShamanMaskOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay shamanMaskOverlay = new ShamanMaskOverlay();

    public ShamanMaskOverlayFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }
}
