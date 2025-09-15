/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.SpellCastMessageOverlay;
import com.wynntils.overlays.SpellInputsOverlay;

@ConfigCategory(Category.OVERLAYS)
public class SpellCastMessageOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay spellCastOverlay = new SpellCastMessageOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay spellInputsOverlay = new SpellInputsOverlay();
}
