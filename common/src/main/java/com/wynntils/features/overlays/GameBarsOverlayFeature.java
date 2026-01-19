/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
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
    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final ManaBarOverlay manaBarOverlay = new ManaBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final ManaBankBarOverlay manaBankBarOverlay = new ManaBankBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final FocusBarOverlay focusBarOverlay = new FocusBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final AwakenedProgressBarOverlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final CorruptedBarOverlay corruptedBarOverlay = new CorruptedBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final OphanimBarOverlay ophanimBarOverlay = new OphanimBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final HolyPowerBarOverlay holyPowerBarOverlay = new HolyPowerBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final FocusedMobHealthBarOverlay focusedMobHealthBarOverlay = new FocusedMobHealthBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final CommanderBarOverlay commanderBarOverlay = new CommanderBarOverlay();

    @RegisterOverlay(renderType = RenderElementType.GUI_PRE)
    private final MomentumBarOverlay momentumBarOverlay = new MomentumBarOverlay();

    public GameBarsOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
