/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.type.RenderElementType;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class HideCrosshairInCutscenesFeature extends Feature {
    public HideCrosshairInCutscenesFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER)
                .build());
    }

    @SubscribeEvent
    public void onRenderCrosshair(RenderEvent.Pre event) {
        if (event.getType() != RenderElementType.CROSSHAIR) return;
        if (!Models.Cutscene.isCutsceneActive()) return;

        event.setCanceled(true);
    }
}
