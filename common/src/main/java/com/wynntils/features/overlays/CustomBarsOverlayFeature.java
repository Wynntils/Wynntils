/*
 * Copyright © Wynntils 2023-2025.
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
import com.wynntils.overlays.custombars.BubbleTexturedCustomBarOverlay;
import com.wynntils.overlays.custombars.ExperienceTexturedCustomBarOverlay;
import com.wynntils.overlays.custombars.HealthTexturedCustomBarOverlay;
import com.wynntils.overlays.custombars.ManaTexturedCustomBarOverlay;
import com.wynntils.overlays.custombars.UniversalTexturedCustomBarOverlay;
import java.util.ArrayList;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class CustomBarsOverlayFeature extends Feature {
    // If adding a new bar, make sure to update CustomBarSelectionScreen
    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<UniversalTexturedCustomBarOverlay> customUniversalBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<HealthTexturedCustomBarOverlay> customHealthBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<ManaTexturedCustomBarOverlay> customManaBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<ExperienceTexturedCustomBarOverlay> customExperienceBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final List<BubbleTexturedCustomBarOverlay> customBubbleBarOverlays = new ArrayList<>();

    public CustomBarsOverlayFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }
}
