/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.SpellCastMessageOverlay;

@ConfigCategory(Category.OVERLAYS)
public class SpellCastMessageOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay spellCastOverlay = new SpellCastMessageOverlay();
}
