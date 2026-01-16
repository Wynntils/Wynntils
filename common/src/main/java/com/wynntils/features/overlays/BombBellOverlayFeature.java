/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.overlays.BombBellOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class BombBellOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderElementType.GUI_POST)
    public final BombBellOverlay bombBellOverlay = new BombBellOverlay();

    public BombBellOverlayFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }
}
