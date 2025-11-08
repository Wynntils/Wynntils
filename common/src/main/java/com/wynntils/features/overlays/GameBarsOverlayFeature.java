/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
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
import com.wynntils.overlays.gamebars.CommanderBarOverlay;
import com.wynntils.overlays.gamebars.CorruptedBarOverlay;
import com.wynntils.overlays.gamebars.FocusBarOverlay;
import com.wynntils.overlays.gamebars.FocusedMobHealthBarOverlay;
import com.wynntils.overlays.gamebars.HealthBarOverlay;
import com.wynntils.overlays.gamebars.HolyPowerBarOverlay;
import com.wynntils.overlays.gamebars.ManaBankBarOverlay;
import com.wynntils.overlays.gamebars.ManaBarOverlay;
import com.wynntils.overlays.gamebars.MomentumBarOverlay;
import com.wynntils.overlays.gamebars.OphanimBarOverlay;

@ConfigCategory(Category.OVERLAYS)
public class GameBarsOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.HEALTH_BAR, renderAt = RenderState.REPLACE)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.FOOD_BAR, renderAt = RenderState.REPLACE)
    private final ManaBarOverlay manaBarOverlay = new ManaBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final ManaBankBarOverlay manaBankBarOverlay = new ManaBankBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final FocusBarOverlay focusBarOverlay = new FocusBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final AwakenedProgressBarOverlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final CorruptedBarOverlay corruptedBarOverlay = new CorruptedBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final OphanimBarOverlay ophanimBarOverlay = new OphanimBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final HolyPowerBarOverlay holyPowerBarOverlay = new HolyPowerBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final FocusedMobHealthBarOverlay focusedMobHealthBarOverlay = new FocusedMobHealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final CommanderBarOverlay commanderBarOverlay = new CommanderBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final MomentumBarOverlay momentumBarOverlay = new MomentumBarOverlay();
}
