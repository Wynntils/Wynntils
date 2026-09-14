/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.TokenBarsOverlay;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class TokenBarsOverlayFeature extends Feature {
    @RegisterOverlay
    private final Overlay tokenBarsOverlay = new TokenBarsOverlay();

    public TokenBarsOverlayFeature() {
        super(
                ProfileDefault.onlyDefault(),
                List.of(ConfigDependency.functionality(Models.Token.trackTokenGatekeepers)));
    }
}
