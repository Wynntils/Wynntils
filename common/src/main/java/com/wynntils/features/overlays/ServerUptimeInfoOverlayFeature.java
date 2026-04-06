/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.overlays.ServerUptimeInfoOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class ServerUptimeInfoOverlayFeature extends Feature {
    @RegisterOverlay(renderType = RenderElementType.PLAYER_TAB_LIST)
    private final Overlay ServerUptimeInfoOverlay = new ServerUptimeInfoOverlay();

    public ServerUptimeInfoOverlayFeature() {
        super(ProfileDefault.onlyDefault());
    }
}
