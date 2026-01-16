/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
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
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class GameBarsOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final ManaBarOverlay manaBarOverlay = new ManaBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final ManaBankBarOverlay manaBankBarOverlay = new ManaBankBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final FocusBarOverlay focusBarOverlay = new FocusBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final AwakenedProgressBarOverlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final CorruptedBarOverlay corruptedBarOverlay = new CorruptedBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final OphanimBarOverlay ophanimBarOverlay = new OphanimBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final HolyPowerBarOverlay holyPowerBarOverlay = new HolyPowerBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final FocusedMobHealthBarOverlay focusedMobHealthBarOverlay = new FocusedMobHealthBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final CommanderBarOverlay commanderBarOverlay = new CommanderBarOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI, renderAt = RenderState.PRE)
    private final MomentumBarOverlay momentumBarOverlay = new MomentumBarOverlay();

    public GameBarsOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
