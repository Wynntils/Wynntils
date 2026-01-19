/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.TerritoryAttackTimerOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class TerritoryAttackTimerOverlayFeature extends Feature {
    @RegisterOverlay(renderType = RenderElementType.GUI_POST)
    private final Overlay territoryAttackTimerOverlay = new TerritoryAttackTimerOverlay();

    @Persisted
    public final Config<Boolean> displayBeaconBeam = new Config<>(true);

    public TerritoryAttackTimerOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
