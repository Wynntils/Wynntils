/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.objectives.DailyObjectiveOverlay;
import com.wynntils.overlays.objectives.GuildObjectiveOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class ObjectivesOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderElementType.GUI_POST)
    public final Overlay guildObjectiveOverlay = new GuildObjectiveOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI_POST)
    public final Overlay dailyObjectiveOverlay = new DailyObjectiveOverlay();

    public ObjectivesOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
