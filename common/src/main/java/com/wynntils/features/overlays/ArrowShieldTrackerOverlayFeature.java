/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.ArrowShieldTrackerOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class ArrowShieldTrackerOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderElementType.GUI_POST)
    private final Overlay arrowShieldTrackerOverlay = new ArrowShieldTrackerOverlay();

    public ArrowShieldTrackerOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
