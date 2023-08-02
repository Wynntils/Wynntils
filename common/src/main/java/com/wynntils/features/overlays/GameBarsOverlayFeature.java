/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.gamebars.AwakenedProgressBarOverlay;
import com.wynntils.overlays.gamebars.BloodPoolBarOverlay;
import com.wynntils.overlays.gamebars.CorruptedBarOverlay;
import com.wynntils.overlays.gamebars.FocusBarOverlay;
import com.wynntils.overlays.gamebars.HealthBarOverlay;
import com.wynntils.overlays.gamebars.ManaBankBarOverlay;
import com.wynntils.overlays.gamebars.ManaBarOverlay;

@ConfigCategory(Category.OVERLAYS)
public class GameBarsOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.HEALTH_BAR, renderAt = RenderState.REPLACE)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.FOOD_BAR, renderAt = RenderState.REPLACE)
    private final ManaBarOverlay manaBarOverlay = new ManaBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ManaBankBarOverlay manaBankBarOverlay = new ManaBankBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final FocusBarOverlay focusBarOverlay = new FocusBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final AwakenedProgressBarOverlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final CorruptedBarOverlay corruptedBarOverlay = new CorruptedBarOverlay();
}
