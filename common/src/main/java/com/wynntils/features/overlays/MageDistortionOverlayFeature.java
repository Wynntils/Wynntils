/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.overlays.MageDistortionOverlay;

@ConfigCategory(Category.OVERLAYS)
public class MageDistortionOverlayFeature extends Feature {
    @RegisterOverlay
    private final Overlay distortionOverlay = new MageDistortionOverlay();

    public MageDistortionOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
