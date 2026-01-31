/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.HeldItemNameOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class HeldItemNameOverlayFeature extends Feature {
    @RegisterOverlay(renderType = RenderElementType.HOTBAR)
    private final Overlay heldItemNameOverlay = new HeldItemNameOverlay();

    public HeldItemNameOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
