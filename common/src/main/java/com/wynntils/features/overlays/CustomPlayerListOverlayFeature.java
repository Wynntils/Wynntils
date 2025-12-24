/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.CustomPlayerListOverlay;

@ConfigCategory(Category.OVERLAYS)
public class CustomPlayerListOverlayFeature extends Feature {
    // This render type is not set to PLAYER_TAB_LIST on purpose,
    // as we need to do additional rendering before and after the player list is rendered (for animations).
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.POST)
    private final Overlay customPlayerListOverlay = new CustomPlayerListOverlay();

    public CustomPlayerListOverlayFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }
}
