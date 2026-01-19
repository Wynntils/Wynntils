/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.CustomPlayerListOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class CustomPlayerListOverlayFeature extends Feature {
    // This render type is not set to PLAYER_TAB_LIST on purpose,
    // as we need to do additional rendering before and after the player list is rendered (for animations).
    @RegisterOverlay(renderType = RenderElementType.GUI_POST)
    private final Overlay customPlayerListOverlay = new CustomPlayerListOverlay();

    public CustomPlayerListOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
