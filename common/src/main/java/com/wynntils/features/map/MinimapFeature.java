/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.minimap.CoordinateOverlay;
import com.wynntils.overlays.minimap.MinimapOverlay;
import com.wynntils.overlays.minimap.TerritoryOverlay;

@ConfigCategory(Category.MAP)
public class MinimapFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    public final MinimapOverlay minimapOverlay = new MinimapOverlay();

    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay coordinatesOverlay = new CoordinateOverlay();

    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay territoryOverlay = new TerritoryOverlay();
}
