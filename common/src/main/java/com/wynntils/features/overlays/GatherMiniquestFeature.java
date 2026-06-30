/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.overlays.gamebars.GatherMiniquestBarOverlay;

public class GatherMiniquestFeature extends Feature {
    @RegisterOverlay
    private final GatherMiniquestBarOverlay gatherMiniquestBarOverlay = new GatherMiniquestBarOverlay();

    public GatherMiniquestFeature() {
        super(ProfileDefault.ENABLED);
    }
}
