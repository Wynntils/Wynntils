/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayGroup;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.infobox.InfoBoxOverlay;
import java.util.ArrayList;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends Feature {
    @OverlayGroup(instances = 7, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<InfoBoxOverlay> infoBoxOverlays = new ArrayList<>();
}
