/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.StatusEffectsOverlay;
import com.wynntils.overlays.StatusIconsOverlay;

@ConfigCategory(Category.OVERLAYS)
public class StatusEffectsOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final StatusEffectsOverlay statusEffectsOverlay = new StatusEffectsOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final StatusIconsOverlay statusIconsOverlay = new StatusIconsOverlay();
}
