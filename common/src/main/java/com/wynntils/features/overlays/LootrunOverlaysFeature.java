/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.overlays.lootrun.LootrunBeaconCountOverlay;
import com.wynntils.overlays.lootrun.LootrunMissionsOverlay;
import com.wynntils.overlays.lootrun.LootrunTaskNameOverlay;
import com.wynntils.overlays.lootrun.LootrunTrialsOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class LootrunOverlaysFeature extends Feature {
    @OverlayInfo(renderType = RenderElementType.GUI)
    private final Overlay lootrunTaskNameOverlay = new LootrunTaskNameOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI)
    private final Overlay lootrunBeaconCountOverlay = new LootrunBeaconCountOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI)
    private final Overlay lootrunMissionOverlay = new LootrunMissionsOverlay();

    @OverlayInfo(renderType = RenderElementType.GUI)
    private final Overlay lootrunTrialOverlay = new LootrunTrialsOverlay();

    public LootrunOverlaysFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }
}
