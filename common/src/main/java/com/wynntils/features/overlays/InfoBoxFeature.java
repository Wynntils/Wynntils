/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayGroup;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.infobox.InfoBoxOverlay;
import java.util.ArrayList;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends Feature {
    @OverlayGroup(instances = 7, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<InfoBoxOverlay> infoBoxOverlays = new ArrayList<>();

    public InfoBoxFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }
}
