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
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.HeldItemCooldownOverlay;

@ConfigCategory(Category.OVERLAYS)
public class HeldItemCooldownOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay heldItemCooldownOverlay = new HeldItemCooldownOverlay();

    public HeldItemCooldownOverlayFeature() {
        super(ProfileDefault.DISABLED);
    }
}
